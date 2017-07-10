package tgbungeeauth.server.auth.logic;

import java.security.NoSuchAlgorithmException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import tgbungeeauth.server.auth.config.Messages;
import tgbungeeauth.server.auth.config.Settings;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.db.PlayerAuth;
import tgbungeeauth.server.auth.security.PasswordSecurity;

public class AdminCommand implements CommandExecutor {

	private final DataSource datasource;
	public AdminCommand(DataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (!sender.hasPermission("tgbungeeauth.admin")) {
			sender.sendMessage(Messages.noPerms);
			return true;
		}

		if (args[0].equalsIgnoreCase("purge")) {
			if (args.length != 2) {
				return false;
			}
			try {
				long days = Long.parseLong(args[1]) * 86400000;
				long until = System.currentTimeMillis() - days;
				int cleared = datasource.purgeDatabase(until);
				sender.sendMessage("Deleted " + cleared + " user accounts");
			} catch (NumberFormatException e) {
			}
			return true;
		} else if (args[0].equalsIgnoreCase("register") || args[0].equalsIgnoreCase("reg")) {
			if (args.length != 3) {
				return false;
			}
			try {
				String name = args[1].toLowerCase();
				if (datasource.isAuthAvailable(name)) {
					sender.sendMessage(Messages.registerAlready);
					return true;
				}
				String hash = PasswordSecurity.getHash(Settings.hashAlgo, args[2], name);
				PlayerAuth auth = new PlayerAuth(name, args[1], hash, "198.18.0.1", System.currentTimeMillis());
				datasource.saveAuth(auth);
				sender.sendMessage(Messages.registerSuccess);
			} catch (NoSuchAlgorithmException ex) {
				sender.sendMessage(Messages.registerError);
			}
			return true;
		} else if (args[0].equalsIgnoreCase("changepassword") || args[0].equalsIgnoreCase("cp")) {
			if (args.length != 3) {
				return false;
			}
			try {
				String name = args[1].toLowerCase();
				String hash = PasswordSecurity.getHash(Settings.hashAlgo, args[2], name);
				PlayerAuth auth = null;
				if (datasource.isAuthAvailable(name)) {
					auth = datasource.getAuth(name);
				} else {
					sender.sendMessage(Messages.unknownUser);
					return true;
				}
				auth.setHash(hash);
				datasource.updatePassword(auth);
				sender.sendMessage(Messages.passwordChangeSuccess);
			} catch (NoSuchAlgorithmException ex) {
			}
			return true;
		} else if (args[0].equalsIgnoreCase("unregister") || args[0].equalsIgnoreCase("unreg") || args[0].equalsIgnoreCase("del")) {
			if (args.length != 2) {
				return false;
			}
			String name = args[1].toLowerCase();
			datasource.removeAuth(name);
			sender.sendMessage("unregistered");
			return true;
		} else if (args[0].equalsIgnoreCase("reloadsettings")) {
			Settings.loadConfig();
			Messages.loadConfig();
			sender.sendMessage("Settings reloaded");
			return true;
		}
		return false;
	}

}
