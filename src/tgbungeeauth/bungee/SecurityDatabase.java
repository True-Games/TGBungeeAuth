package tgbungeeauth.bungee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class SecurityDatabase {

	private HashSet<String> license = new HashSet<>();

	public void addOnlineMode(String name) {
		license.add(name.toLowerCase());
	}

	public void removeOnlineMode(String name) {
		license.remove(name.toLowerCase());
	}

	public boolean isOnlineMode(String name) {
		return license.contains(name.toLowerCase());
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
		license.addAll(configuration.getStringList("license"));
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
