package tgbungeeauth.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.auth.managment.AsyncLogin;

public class LoginCommand extends Command {

	public LoginCommand() {
		super("login", null, "l");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}

		if (args.length == 1) {
			ProxyServer.getInstance().getScheduler().runAsync(TGBungeeAuthBungee.getInstance(), new AsyncLogin((ProxiedPlayer) sender, args[0], false));
		}
	}

}
