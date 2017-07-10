package tgbungeeauth.bungee.auth.managment;

import java.io.IOException;

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
	private final boolean forceLogin;

	public AsyncLogin(ProxiedPlayer player, String password, boolean forceLogin) {
		this.player = player;
		this.password = password;
		this.name = player.getName().toLowerCase();
		this.forceLogin = forceLogin;
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

		String hash = pAuth.getHash();
		boolean passwordVerified = forceLogin || PasswordSecurity.comparePasswordWithHash(password, hash, name);
		if (passwordVerified) {
			try {
				SharedManagement.finish(player, hash, false);
			} catch (IOException e) {
				player.sendMessage(new TextComponent(Messages.loginError));
			}
		} else {
			if (Settings.isKickOnWrongPasswordEnabled) {
				player.disconnect(new TextComponent(Messages.loginWrongPassword));
			} else {
				player.sendMessage(new TextComponent(Messages.loginWrongPassword));
				return;
			}
		}
	}

}
