package tgbungeeauth.commands;

import java.security.NoSuchAlgorithmException;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tgbungeeauth.TGBungeeAuthBungee;
import tgbungeeauth.auth.db.PlayerAuth;
import tgbungeeauth.auth.security.PasswordSecurity;
import tgbungeeauth.config.Messages;
import tgbungeeauth.config.Settings;

public class ChangePasswordCommand extends Command {

	public ChangePasswordCommand() {
		super("changepassword");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;

			TGBungeeAuthBungee plugin = TGBungeeAuthBungee.getInstance();
			if (!plugin.isAuthed(player)) {
				player.sendMessage(new TextComponent(Messages.notloggedin));
				return;
			}

			PlayerAuth auth = plugin.getAuthDatabase().getAuth(player.getName());
			if (auth == null) {
				player.sendMessage(new TextComponent(Messages.unknownUser));
				return;
			}

			if (args.length == 2) {
				if (PasswordSecurity.comparePasswordWithHash(args[0], auth.getHash())) {
					try {
						auth.setHash(PasswordSecurity.getHash(Settings.hashAlgo, args[1]));
						player.sendMessage(new TextComponent(Messages.passwordChangeSuccess));
					} catch (NoSuchAlgorithmException e) {
						player.sendMessage(new TextComponent(Messages.passwordChangeError));
					}
				} else {
					player.sendMessage(new TextComponent(Messages.passwordChangeWrongPassword));
				}
			}
		}
	}

}
