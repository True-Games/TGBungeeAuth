package tgbungeeauth.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import tgbungeeauth.TGBungeeAuthBungee;
import tgbungeeauth.auth.managment.AsyncRegister;

public class RegisterCommand extends Command {

	public RegisterCommand() {
		super("register", null, "reg");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}

		if (args.length == 1) {
			ProxyServer.getInstance().getScheduler().runAsync(TGBungeeAuthBungee.getInstance(), new AsyncRegister((ProxiedPlayer) sender, args[0]));
		}
	}

}
