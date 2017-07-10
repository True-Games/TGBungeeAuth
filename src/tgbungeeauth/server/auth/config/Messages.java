package tgbungeeauth.server.auth.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import tgbungeeauth.server.TGBungeeAuthBukkit;

public class Messages {

	public static String registerAlready;
	public static String registerError;
	public static String registerSuccess;
	public static String registerHelp;
	public static String registerMax;
	public static String loginSuccess;
	public static String loginError;
	public static String loginWrongPassword;
	public static String loginHelp;
	public static String passwordChangeSuccess;
	public static String timedOut;
	public static String unknownUser;
	public static String noPerms;
	public static String restrictionNameLength;
	public static String restrictionRegex;
	public static String restrictionInvalidCase;
	public static String restrictionAlreadyPlaying;

	public static void loadConfig() {
		FileConfiguration config = TGBungeeAuthBukkit.getInstance().getConfig();
		registerAlready = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.register.already"));
		registerError = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.register.error"));
		registerSuccess = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.register.success"));
		registerHelp = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.register.help"));
		registerMax = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.register.max"));
		loginSuccess = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.login.success"));
		loginError = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.login.error"));
		loginWrongPassword = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.login.wrongpwd"));
		loginHelp = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.login.help"));
		passwordChangeSuccess = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.passwordchange.success"));
		timedOut = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.timedout"));
		unknownUser = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.unknownuser"));
		noPerms = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.noperms"));
		restrictionNameLength = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.restriction.namelength"));
		restrictionRegex = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.restriction.regex"));
		restrictionInvalidCase = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.restriction.invalidcase"));
		restrictionAlreadyPlaying = ChatColor.translateAlternateColorCodes('&', config.getString("authserver.login.messages.restriction.alreadyplaying"));
	}

}
