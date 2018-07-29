package tgbungeeauth.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tgbungeeauth.TGBungeeAuthBungee;
import tgbungeeauth.auth.db.PlayerAuth;
import tgbungeeauth.config.Messages;

public class LicenseCommand extends Command {

	public LicenseCommand() {
		super("license");
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

			if (auth.isOnlineMode()) {
				auth.setOnlineMode(false);
				player.sendMessage(new TextComponent(Messages.optsecurityLicenseRemoved));
			} else {
				auth.setOnlineMode(true);
				player.sendMessage(new TextComponent(Messages.optsecurityLicenseAdded));
			}
		}
	}

}
