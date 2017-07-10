package tgbungeeauth.server.auth.logic;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tgbungeeauth.server.TGBungeeAuthBukkit;
import tgbungeeauth.server.auth.config.Messages;
import tgbungeeauth.server.auth.config.Settings;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.managment.AsyncRegister;

public class RegisterCommand implements CommandExecutor {

	private final DataSource datasource;
	public RegisterCommand(DataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		final Player player = (Player) sender;

		if (datasource.isAuthAvailable(player.getName().toLowerCase())) {
			player.sendMessage(Messages.registerAlready);
			return true;
		}

		if (datasource.getAllAuthsByIp(player.getAddress().getHostString()).size() > Settings.maxRegsPerIp) {
			player.sendMessage(Messages.registerMax);
			return true;
		}

		if (args.length == 1) {
			Bukkit.getScheduler().runTaskAsynchronously(TGBungeeAuthBukkit.getInstance(), new AsyncRegister(datasource, player, args[0]));
			return true;
		}

		return false;
	}

}
