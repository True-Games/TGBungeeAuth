package tgbungeeauth.server.auth.config;

import org.bukkit.configuration.file.FileConfiguration;

import tgbungeeauth.server.TGBungeeAuthBukkit;
import tgbungeeauth.server.auth.security.HashAlgorithm;

public class Settings {

	public static HashAlgorithm hashAlgo;
	public static boolean isKickOnWrongPasswordEnabled;
	public static String nickRegex;
	public static int maxNickLength;
	public static int minNickLength;
	public static long timeout;
	public static long messageInterval;
	public static int maxRegsPerIp;

	public static void loadConfig() {
		FileConfiguration config = TGBungeeAuthBukkit.getInstance().getConfig();
		hashAlgo = HashAlgorithm.valueOf(config.getString("authserver.login.algo").toUpperCase());
		isKickOnWrongPasswordEnabled = config.getBoolean("authserver.login.kickonwrongpwd");
		nickRegex = config.getString("authserver.login.nick.regex");
		maxNickLength = config.getInt("authserver.login.nick.maxlength");
		minNickLength = config.getInt("authserver.login.nick.minlength");
		timeout = config.getLong("authserver.login.timeout");
		messageInterval = config.getLong("authserver.login.helpmsginterval");
		maxRegsPerIp  = config.getInt("authserver.login.maxregsperip");
	}

}
