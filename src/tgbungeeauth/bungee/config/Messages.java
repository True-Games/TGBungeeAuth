package tgbungeeauth.bungee.config;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import tgbungeeauth.bungee.TGBungeeAuthBungee;

public class Messages {

	public static String notloggedin;
	public static String licenseMessageAdded;
	public static String licenseMessageRemoved;
	public static String registerAlready;
	public static String registerError;
	public static String registerSuccess;
	public static String registerHelp;
	public static String registerMax;
	public static String loginAlready;
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

	public static void loadConfig() throws IOException {
		Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(TGBungeeAuthBungee.getInstance().getDataFolder(), "config.yml"));
		notloggedin = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.messages.notloggedin"));
		licenseMessageAdded = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.license.enabled"));
		licenseMessageRemoved = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.license.disabled"));
		registerAlready = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.register.already"));
		registerError = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.register.error"));
		registerSuccess = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.register.success"));
		registerHelp = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.register.help"));
		registerMax = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.register.max"));
		loginAlready = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.login.already"));
		loginSuccess = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.login.success"));
		loginError = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.login.error"));
		loginWrongPassword = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.login.wrongpwd"));
		loginHelp = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.login.help"));
		passwordChangeSuccess = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.passwordchange.success"));
		timedOut = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.timedout"));
		unknownUser = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.unknownuser"));
		noPerms = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.noperms"));
		restrictionNameLength = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.restriction.namelength"));
		restrictionRegex = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.restriction.regex"));
		restrictionInvalidCase = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.restriction.invalidcase"));
		restrictionAlreadyPlaying = ChatColor.translateAlternateColorCodes('&', config.getString("bungeecord.login.messages.restriction.alreadyplaying"));
	}

}
