package tgbungeeauth.server;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import tgbungeeauth.server.auth.AuthServerLogic;
import tgbungeeauth.server.game.GameServerLogic;

public class TGBungeeAuthBukkit extends JavaPlugin {

	private static TGBungeeAuthBukkit instance;

	public static TGBungeeAuthBukkit getInstance() {
		return instance;
	}

	public TGBungeeAuthBukkit() {
		instance = this;
	}

	private ServerLogic logic;

	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
			TGBungeeAuthBukkit.getInstance().getLogger().severe("Missing ProtocolLib");
			Bukkit.shutdown();
		}
		if (!Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")) {
			TGBungeeAuthBukkit.getInstance().getLogger().severe("Missing ProtocolSupport");
			Bukkit.shutdown();
		}
		TGBungeeAuthBukkit.getInstance().getConfig().options().copyDefaults(true);
		TGBungeeAuthBukkit.getInstance().saveConfig();
		TGBungeeAuthBukkit.getInstance().reloadConfig();

		logic = System.getProperty("tgbungeeauth.authserver") != null ? new AuthServerLogic() : new GameServerLogic();
		try {
			logic.start();
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.shutdown();
		}
	}

	@Override
	public void onDisable() {
		try {
			logic.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
