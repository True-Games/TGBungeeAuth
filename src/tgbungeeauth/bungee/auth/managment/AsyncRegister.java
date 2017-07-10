package tgbungeeauth.bungee.auth.managment;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.auth.db.AuthDatabase;
import tgbungeeauth.bungee.auth.security.PasswordSecurity;
import tgbungeeauth.bungee.config.Messages;
import tgbungeeauth.bungee.config.Settings;

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
			SharedManagement.finish(player, PasswordSecurity.getHash(Settings.hashAlgo, password), true);
		} catch (NoSuchAlgorithmException | IOException e) {
			player.sendMessage(new TextComponent(Messages.registerError));
		}
	}

}
