package tgbungeeauth.bungee;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class SecurityDatabase {

	private HashSet<String> onlineMode = new HashSet<>();

	public void addOnlineMode(String name) {
		onlineMode.add(name.toLowerCase());
	}

	public void removeOnlineMode(String name) {
		onlineMode.remove(name.toLowerCase());
	}

	public boolean isOnlineMode(String name) {
		return onlineMode.contains(name.toLowerCase());
	}

	public void load() throws IOException {
		File db = new File(TGBungeeAuthBungee.getInstance().getDataFolder(), "database.yml");
		if (!db.exists()) {
			db.createNewFile();
		}
		Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(db);
	}

	public void save() {
	}

}
