package tgbungeeauth.bungee.auth.db;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import tgbungeeauth.bungee.TGBungeeAuthBungee;

public class SecutiryDataStorage {

	private final HashSet<String> license = new HashSet<>();
	private final HashMap<String, String> hostname = new HashMap<>();

	public void addOnlineMode(String name) {
		license.add(name.toLowerCase());
	}

	public void removeOnlineMode(String name) {
		license.remove(name.toLowerCase());
	}

	public boolean isOnlineMode(String name) {
		return license.contains(name.toLowerCase());
	}

	public void addHostname(String playername, String name) {
		hostname.put(playername.toLowerCase(), name);
	}

	public void removeHostname(String playername) {
		hostname.remove(playername.toLowerCase());
	}

	public String getHostname(String playername) {
		return hostname.remove(playername.toLowerCase());
	}

	private File getFile() {
		return new File(TGBungeeAuthBungee.getInstance().getDataFolder(), "database.yml");
	}

	public void load() throws IOException {
		File db = getFile();
		if (!db.exists()) {
			db.createNewFile();
		}
		Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(db);
		AuthDatabase adatabase = TGBungeeAuthBungee.getInstance().getAuthDatabase();
		for (String user : configuration.getStringList("license")) {
			if (adatabase.isAuthAvailable(user)) {
				license.add(user);
			}
		}
	}

	public void save() {
		Configuration configuration = new Configuration();
		configuration.set("license", new ArrayList<>(license));
		try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, getFile());
		} catch (IOException e) {
		}
	}

}
