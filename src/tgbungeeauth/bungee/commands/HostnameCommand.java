package tgbungeeauth.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.auth.db.PlayerAuth;
import tgbungeeauth.bungee.config.Messages;

public class HostnameCommand extends Command {

	public HostnameCommand() {
		super("hostname");
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

			if (args.length == 0) {
				auth.setHostname(null);
				player.sendMessage(new TextComponent(Messages.optsecurityHostnameRemoved));
			} else {
				auth.setHostname(args[0]);
				player.sendMessage(new TextComponent(Messages.optsecurityHostnameAdded));
			}
		}
	}

}
