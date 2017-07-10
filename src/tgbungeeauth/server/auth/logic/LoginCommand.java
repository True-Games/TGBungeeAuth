package tgbungeeauth.server.auth.logic;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tgbungeeauth.server.TGBungeeAuthBukkit;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.managment.AsyncLogin;

public class LoginCommand implements CommandExecutor {

	private final DataSource datasource;
	public LoginCommand(DataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmnd, String label, final String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}

		final Player player = (Player) sender;

		if (args.length == 1) {
			Bukkit.getScheduler().runTaskAsynchronously(TGBungeeAuthBukkit.getInstance(), new AsyncLogin(datasource, player, args[0], false));
			return true;
		}

		return false;
	}

}
