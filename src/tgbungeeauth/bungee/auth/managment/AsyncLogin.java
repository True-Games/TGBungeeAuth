package tgbungeeauth.bungee.auth.managment;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.auth.db.PlayerAuth;
import tgbungeeauth.bungee.auth.security.PasswordSecurity;
import tgbungeeauth.bungee.config.Messages;
import tgbungeeauth.bungee.config.Settings;

public class AsyncLogin implements Runnable {

	private final ProxiedPlayer player;
	private final String name;
	private final String password;

	public AsyncLogin(ProxiedPlayer player, String password) {
		this.player = player;
		this.password = password;
		this.name = player.getName().toLowerCase();
	}

	@Override
	public void run() {
		TGBungeeAuthBungee plugin = TGBungeeAuthBungee.getInstance();
		if (plugin.isAuthed(player)) {
			player.sendMessage(new TextComponent(Messages.loginAlready));
			return;
		}

		PlayerAuth pAuth = plugin.getAuthDatabase().getAuth(name);
		if (pAuth == null) {
			player.sendMessage(new TextComponent(Messages.unknownUser));
			return;
		}

		if (PasswordSecurity.comparePasswordWithHash(password, pAuth.getHash(), name)) {
			login(player);
		} else {
			if (Settings.isKickOnWrongPasswordEnabled) {
				player.disconnect(new TextComponent(Messages.loginWrongPassword));
			} else {
				player.sendMessage(new TextComponent(Messages.loginWrongPassword));
			}
		}
	}

	public static void login(ProxiedPlayer player) {
		TGBungeeAuthBungee plugin = TGBungeeAuthBungee.getInstance();

		plugin.getAuthDatabase().updateSession(player.getName(), player.getAddress().getHostString(), System.currentTimeMillis());

		player.sendMessage(new TextComponent(Messages.loginSuccess));

		plugin.finishAuth(player);
	}

}
