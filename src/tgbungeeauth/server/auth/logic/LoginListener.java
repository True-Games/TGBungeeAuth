package tgbungeeauth.server.auth.logic;

import java.util.regex.PatternSyntaxException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import tgbungeeauth.server.TGBungeeAuthBukkit;
import tgbungeeauth.server.auth.config.Messages;
import tgbungeeauth.server.auth.config.Settings;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.db.PlayerAuth;
import tgbungeeauth.server.auth.managment.AsyncLogin;
import tgbungeeauth.server.auth.managment.SharedManagement;

public class LoginListener implements Listener {

	private final DataSource datasource;
	public LoginListener(DataSource data) {
		this.datasource = data;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {

		String name = event.getName();
		String lcname = name.toLowerCase();

		String regex = Settings.nickRegex;

		// check length
		if ((lcname.length() > Settings.maxNickLength) || (lcname.length() < Settings.minNickLength)) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.restrictionNameLength);
			return;
		}

		// check regex
		try {
			if (!name.matches(regex)) {
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.restrictionRegex.replace("REG_EX", regex));
				return;
			}
		} catch (PatternSyntaxException pse) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Invalid regex configured. Please norify administrator about this");
			return;
		}

		// check name case
		if (datasource.isAuthAvailable(lcname)) {
			PlayerAuth auth = datasource.getAuth(lcname);
			String realnickname = auth.getRealNickname();
			if (!realnickname.isEmpty() && !name.equals(realnickname)) {
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.restrictionInvalidCase.replace("REALNAME", realnickname));
				return;
			}
		}

		// check other single session
		Player oplayer = null;
		try {
			oplayer = Bukkit.getPlayerExact(name);
		} catch (Throwable t) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Error while logging in, please try again");
		}
		if ((oplayer != null) && !oplayer.getAddress().getAddress().getHostAddress().equals(event.getAddress().getHostAddress())) {
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.restrictionAlreadyPlaying);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		player.setNoDamageTicks(Integer.MAX_VALUE);

		if (SharedManagement.isAuthed(player)) {
			return;
		}

		if (SharedManagement.isForceAuth(player)) {
			Bukkit.getScheduler().runTaskAsynchronously(TGBungeeAuthBukkit.getInstance(), new AsyncLogin(datasource, player, "forcelogi", true));
		} else {
			String msg = datasource.isAuthAvailable(player.getName().toLowerCase()) ? Messages.loginHelp : Messages.registerHelp;
			long time = Settings.timeout * 20;
			if (time != 0) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(TGBungeeAuthBukkit.getInstance(), () -> {
					if (player.isOnline() && !SharedManagement.isAuthed(player)) {
						player.kickPlayer(Messages.timedOut);
					}
				}, time);
			}
	
			Runnable msgtask = new Runnable() {
				@Override
				public void run() {
					if (player.isOnline() && !SharedManagement.isAuthed(player)) {
						player.sendMessage(msg);
						Bukkit.getScheduler().scheduleSyncDelayedTask(TGBungeeAuthBukkit.getInstance(), this, Settings.messageInterval * 20);
					}
				}
			};
			msgtask.run();
		}
	}

}
