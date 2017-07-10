package tgbungeeauth.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tgbungeeauth.bungee.SecurityDatabase;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.config.Messages;

public class LicenseCommand extends Command {

	public LicenseCommand() {
		super("license");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			String name = player.getName();
			SecurityDatabase sdatabase = TGBungeeAuthBungee.getInstance().getSecDatabase();
			if (sdatabase.isOnlineMode(name)) {
				sdatabase.removeOnlineMode(name);
				player.sendMessage(new TextComponent(Messages.licenseMessageRemoved));
			} else {
				sdatabase.addOnlineMode(name);
				player.sendMessage(new TextComponent(Messages.licenseMessageAdded));
			}
		}
	}

}
