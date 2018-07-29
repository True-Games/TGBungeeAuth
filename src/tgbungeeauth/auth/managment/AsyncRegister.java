package tgbungeeauth.auth.managment;

import java.security.NoSuchAlgorithmException;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tgbungeeauth.TGBungeeAuthBungee;
import tgbungeeauth.auth.db.AuthDatabase;
import tgbungeeauth.auth.db.PlayerAuth;
import tgbungeeauth.auth.security.PasswordSecurity;
import tgbungeeauth.config.Messages;
import tgbungeeauth.config.Settings;

public class AsyncRegister implements Runnable {

	protected final ProxiedPlayer player;
	protected final String password;

	public AsyncRegister(ProxiedPlayer player, String password) {
		this.player = player;
		this.password = password;
	}

	@Override
	public void run() {
		AuthDatabase adatabase = TGBungeeAuthBungee.getInstance().getAuthDatabase();

		if (adatabase.isAuthAvailable(player.getName())) {
			player.sendMessage(new TextComponent(Messages.registerAlready));
			return;
		}

		if (adatabase.getAllAuthsByIp(player.getAddress().getHostString()).size() > Settings.maxRegsPerIp) {
			player.sendMessage(new TextComponent(Messages.registerMax));
			return;
		}

		try {
			PlayerAuth auth = new PlayerAuth(player.getName(), PasswordSecurity.getHash(Settings.hashAlgo, password));
			adatabase.saveAuth(auth);

			player.sendMessage(new TextComponent(Messages.registerSuccess));

			AsyncLogin.login(player);
		} catch (NoSuchAlgorithmException e) {
			player.sendMessage(new TextComponent(Messages.registerError));
		}
	}

}
