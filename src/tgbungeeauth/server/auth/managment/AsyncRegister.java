package tgbungeeauth.server.auth.managment;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.bukkit.entity.Player;

import tgbungeeauth.server.auth.config.Messages;
import tgbungeeauth.server.auth.config.Settings;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.security.PasswordSecurity;

public class AsyncRegister implements Runnable {

	protected final DataSource database;

	protected final Player player;
	protected final String name;
	protected final String password;

	public AsyncRegister(DataSource datasource, Player player, String password) {
		this.database = datasource;
		this.player = player;
		this.password = password;
		this.name = player.getName().toLowerCase();
	}

	@Override
	public void run() {
		try {
			SharedManagement.finish(database, player, PasswordSecurity.getHash(Settings.hashAlgo, password, name), true);
		} catch (NoSuchAlgorithmException | IOException e) {
			player.sendMessage(Messages.registerError);
		}
	}

}
