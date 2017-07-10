package tgbungeeauth.server.auth.managment;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import tgbungeeauth.server.TGBungeeAuthBukkit;
import tgbungeeauth.server.auth.config.Messages;
import tgbungeeauth.server.auth.config.Settings;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.db.PlayerAuth;
import tgbungeeauth.server.auth.security.PasswordSecurity;


public class AsyncLogin implements Runnable {

	private final DataSource database;

	private final Player player;
	private final String name;
	private final String password;
	private final boolean forceLogin;

	public AsyncLogin(DataSource datasource, Player player, String password, boolean forceLogin) {
		this.database = datasource;
		this.player = player;
		this.password = password;
		this.name = player.getName().toLowerCase();
		this.forceLogin = forceLogin;
	}

	protected PlayerAuth preAuth() {
		PlayerAuth pAuth = database.getAuth(name);
		if (pAuth == null) {
			player.sendMessage(Messages.unknownUser);
			return null;
		}
		return pAuth;
	}

	@Override
	public void run() {
		PlayerAuth pAuth = preAuth();
		if (pAuth == null) {
			return;
		}

		String hash = pAuth.getHash();
		boolean passwordVerified = forceLogin || PasswordSecurity.comparePasswordWithHash(password, hash, name);
		if (passwordVerified) {
			try {
				SharedManagement.finish(database, player, hash, false);
			} catch (IOException e) {
				player.sendMessage(Messages.loginError);
			}
		} else {
			if (Settings.isKickOnWrongPasswordEnabled) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(TGBungeeAuthBukkit.getInstance(), new Runnable() {
					@Override
					public void run() {
						player.kickPlayer(Messages.loginWrongPassword);
					}
				});
			} else {
				player.sendMessage(Messages.loginWrongPassword);
				return;
			}
		}
	}

}
