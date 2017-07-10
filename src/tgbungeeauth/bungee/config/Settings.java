package tgbungeeauth.bungee.config;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.auth.security.HashAlgorithm;

public class Settings {

	public static String authserver;
	public static String securekey;
	public static HashAlgorithm hashAlgo;
	public static boolean isKickOnWrongPasswordEnabled;
	public static String nickRegex;
	public static int maxNickLength;
	public static int minNickLength;
	public static long timeout;
	public static long messageInterval;
	public static int maxRegsPerIp;

	public static void loadConfig() throws IOException {
		Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(TGBungeeAuthBungee.getInstance().getDataFolder(), "config.yml"));
		authserver = config.getString("bungeecord.authserver");
		securekey = config.getString("securekey");
		hashAlgo = HashAlgorithm.valueOf(config.getString("bungeecord.login.algo").toUpperCase());
		isKickOnWrongPasswordEnabled = config.getBoolean("bungeecord.login.kickonwrongpwd");
		nickRegex = config.getString("bungeecord.login.nick.regex");
		maxNickLength = config.getInt("bungeecord.login.nick.maxlength");
		minNickLength = config.getInt("bungeecord.login.nick.minlength");
		timeout = config.getLong("bungeecord.login.timeout");
		messageInterval = config.getLong("bungeecord.login.helpmsginterval");
		maxRegsPerIp  = config.getInt("bungeecord.login.maxregsperip");
	}

}
