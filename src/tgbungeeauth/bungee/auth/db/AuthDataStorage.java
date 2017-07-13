package tgbungeeauth.bungee.auth.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import tgbungeeauth.bungee.TGBungeeAuthBungee;

public class AuthDataStorage {

	private static final String hashKey = "hash";
	private static final String ipKey = "ip";
	private static final String lastLoginKey = "lastlogin";
	private static final String onlinemodeKey = "onlinemode";
	private static final String hostnameKey = "hostname";

	/*
	 * file layout:
	 * YAML:
	 * realplayername(String):
	 *   hash: String (required)
	 *   ip: String (required)
	 *   lastlogin: long (required)
	 *   onlinemode: boolean (optional, default: false)
	 *   hostname: String (optional, default: empty string)
	 *
	 * Old but compatible:
	 * DBVER$2:PLAYERNAME:REALPLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS
	 * DBVER$1:PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS
	 */

	private File getFile() {
		return new File(TGBungeeAuthBungee.getInstance().getDataFolder(), "auths.yml");
	}

	public List<PlayerAuth> getAllAuths() throws IOException {
		convertDatabase();
		if (!getFile().exists()) {
			return Collections.emptyList();
		}
		List<PlayerAuth> auths = new ArrayList<>();
		Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getFile());
		for (String playername : config.getKeys()) {
			Configuration playerconf = config.getSection(playername);
			auths.add(new PlayerAuth(
				playername, playerconf.getString(hashKey),
				playerconf.getString(ipKey), playerconf.getLong(lastLoginKey),
				playerconf.getBoolean(onlinemodeKey), playerconf.getString(hostnameKey)
			));
		}
		return auths;
	}

	public void dumpAuths(Collection<PlayerAuth> auths) {
		File tmpfile = new File(getFile().getParentFile(), "___tmpdb");
		try {
			Configuration config = new Configuration();
			for (PlayerAuth auth : auths) {
				Configuration playerconf = new Configuration();
				playerconf.set(hashKey, auth.getHash());
				playerconf.set(ipKey, auth.getIp());
				playerconf.set(lastLoginKey, auth.getLastLogin());
				playerconf.set(onlinemodeKey, auth.isOnlineMode());
				playerconf.set(hostnameKey, auth.getHostname());
				config.set(auth.getRealNickname(), playerconf);
			}
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, tmpfile);
			try {
				Files.move(tmpfile.toPath(), getFile().toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(tmpfile.toPath(), getFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void convertDatabase() throws IOException {
		File legacydb = new File(TGBungeeAuthBungee.getInstance().getDataFolder(), "auths.db");
		if (!legacydb.exists()) {
			return;
		}
		List<PlayerAuth> auths = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(legacydb));
		String line;
		while ((line = br.readLine()) != null) {
			auths.add(parseOldAuth(line));
		}
		br.close();
		dumpAuths(auths);
		if (!legacydb.delete()) {
			throw new IOException("Unable to delete legacy db after convert");
		}
	}

	private PlayerAuth parseOldAuth(String line) {
		String[] args = line.split(":");
		if (line.startsWith("DBVER$")) {
			if (args[0].equalsIgnoreCase("DBVER$2")) {
				return new PlayerAuth(args[2], args[3], args[4], Long.parseLong(args[5]), false, "");
			}
			if (args[0].equalsIgnoreCase("DBVER$1")) {
				return new PlayerAuth(args[1], args[2], args[3], Long.parseLong(args[4]), false, "");
			}
		}
		throw new IllegalArgumentException("Unknown dbstring format: " + line);
	}

}
