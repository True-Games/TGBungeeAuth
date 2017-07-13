package tgbungeeauth.bungee.commands;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.auth.db.AuthDatabase;
import tgbungeeauth.bungee.auth.db.PlayerAuth;
import tgbungeeauth.bungee.auth.security.PasswordSecurity;
import tgbungeeauth.bungee.config.Messages;
import tgbungeeauth.bungee.config.Settings;

public class AdminCommand extends Command {

	public AdminCommand() {
		super("tgbungeeauthadmin");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("tgbungeeauth.admin")) {
			sender.sendMessage(new TextComponent(Messages.noPerms));
			return;
		}

		AuthDatabase adatabase = TGBungeeAuthBungee.getInstance().getAuthDatabase();

		if (args[0].equalsIgnoreCase("purge")) {
			if (args.length != 2) {
				return;
			}
			try {
				long days = Long.parseLong(args[1]) * 86400000;
				long until = System.currentTimeMillis() - days;
				int cleared = adatabase.purgeDatabase(until);
				sender.sendMessage(new TextComponent("Deleted " + cleared + " user accounts"));
			} catch (NumberFormatException e) {
			}
		} else if (args[0].equalsIgnoreCase("register") || args[0].equalsIgnoreCase("reg")) {
			if (args.length != 3) {
				return;
			}
			try {
				if (adatabase.isAuthAvailable(args[1])) {
					sender.sendMessage(new TextComponent(Messages.registerAlready));
					return;
				}
				String hash = PasswordSecurity.getHash(Settings.hashAlgo, args[2]);
				PlayerAuth auth = new PlayerAuth(args[1], hash);
				adatabase.saveAuth(auth);
				sender.sendMessage(new TextComponent(Messages.registerSuccess));
			} catch (NoSuchAlgorithmException ex) {
				sender.sendMessage(new TextComponent(Messages.registerError));
			}
		} else if (args[0].equalsIgnoreCase("changepassword") || args[0].equalsIgnoreCase("cp")) {
			if (args.length != 3) {
				return;
			}
			try {
				PlayerAuth auth = adatabase.getAuth(args[1]);
				if (auth != null) {
					auth.setHash(PasswordSecurity.getHash(Settings.hashAlgo, args[2]));
					sender.sendMessage(new TextComponent(Messages.passwordChangeSuccess));
				} else {
					sender.sendMessage(new TextComponent(Messages.unknownUser));
				}
			} catch (NoSuchAlgorithmException ex) {
				sender.sendMessage(new TextComponent(Messages.passwordChangeError));
			}
		} else if (args[0].equalsIgnoreCase("unregister") || args[0].equalsIgnoreCase("unreg") || args[0].equalsIgnoreCase("del")) {
			if (args.length != 2) {
				return;
			}
			String name = args[1].toLowerCase();
			adatabase.removeAuth(name);
			sender.sendMessage(new TextComponent("Unregistered"));
		} else if (args[0].equalsIgnoreCase("reloadsettings")) {
			try {
				Settings.loadConfig();
				Messages.loadConfig();
			} catch (IOException e) {
			}
			sender.sendMessage(new TextComponent("Settings reloaded"));
		}
	}

}
