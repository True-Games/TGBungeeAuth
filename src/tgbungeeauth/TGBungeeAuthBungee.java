package tgbungeeauth;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.events.PlayerLoginStartEvent;
import protocolsupport.api.events.PlayerProfileCompleteEvent;
import protocolsupport.api.utils.Profile;
import tgbungeeauth.auth.db.AuthDataStorage;
import tgbungeeauth.auth.db.AuthDatabase;
import tgbungeeauth.auth.db.PlayerAuth;
import tgbungeeauth.auth.managment.AsyncLogin;
import tgbungeeauth.commands.AdminCommand;
import tgbungeeauth.commands.ChangePasswordCommand;
import tgbungeeauth.commands.HostnameCommand;
import tgbungeeauth.commands.LicenseCommand;
import tgbungeeauth.commands.LoginCommand;
import tgbungeeauth.commands.RegisterCommand;
import tgbungeeauth.config.Messages;
import tgbungeeauth.config.Settings;

public class TGBungeeAuthBungee extends Plugin implements Listener {

	private static TGBungeeAuthBungee instance;
	public static TGBungeeAuthBungee getInstance() {
		return instance;
	}

	public TGBungeeAuthBungee() {
		instance = this;
	}

	private final AuthDatabase authdatabase = new AuthDatabase(new AuthDataStorage(), 60);

	public AuthDatabase getAuthDatabase() {
		return authdatabase;
	}

	@Override
	public void onEnable() {
		getDataFolder().mkdir();

		File configfile = new File(getDataFolder(), "config.yml");
		if (!configfile.exists()) {
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, configfile.toPath());
			} catch (IOException e) {
			}
		} else {
			File defconfigfile = new File(getDataFolder(), "defconfig.yml");
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, defconfigfile.toPath());
			} catch (IOException e) {
			}
			ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
			try {
				provider.save(Utils.copyDefaultOptions(provider.load(configfile), provider.load(defconfigfile)), configfile);
			} catch (IOException e) {
			}
			defconfigfile.delete();
		}

		try {
			Settings.loadConfig();
			Messages.loadConfig();
		} catch (IOException e) {
			getLogger().severe("Unable to load config");
			ProxyServer.getInstance().stop();
		}

		try {
			authdatabase.load();
		} catch (Exception e) {
			getLogger().severe("Unable to load database");
			ProxyServer.getInstance().stop();
		}

		PluginManager pm = ProxyServer.getInstance().getPluginManager();
		pm.registerCommand(this, new LicenseCommand());
		pm.registerCommand(this, new AdminCommand());
		pm.registerCommand(this, new LoginCommand());
		pm.registerCommand(this, new RegisterCommand());
		pm.registerCommand(this, new HostnameCommand());
		pm.registerCommand(this, new ChangePasswordCommand());
		pm.registerListener(this, this);
	}

	@Override
	public void onDisable() {
		authdatabase.save();
	}

	protected static final String ADDITIONAL_SECURITY_VALID_METADATA_KEY = "TGAuthBungee:SecurityValid";
	protected static final String LOGGED_IN_METADATA_KEY = "TGAuthBungee:LoggedIn";
	protected static final String TARGET_SERVER_METADATA_KEY = "TGAuthBungee:TargetServer";

	public boolean isAuthed(ProxiedPlayer player) {
		return Utils.getConnection(player).hasMetadata(LOGGED_IN_METADATA_KEY);
	}

	public void finishAuth(ProxiedPlayer player) {
		Connection connection = Utils.getConnection(player);
		connection.addMetadata(LOGGED_IN_METADATA_KEY, Boolean.TRUE);
		ServerInfo target = (ServerInfo) connection.getMetadata(TARGET_SERVER_METADATA_KEY);
		if (target != null) {
			player.connect(target);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PlayerLoginStartEvent event) {
		try {

			Connection connection = event.getConnection();

			String name = connection.getProfile().getName();

			//validate name
			String regex = Settings.nickRegex;
			if ((name.length() > Settings.maxNickLength) || (name.length() < Settings.minNickLength)) {
				event.denyLogin(Messages.restrictionNameLength);
				return;
			}
			if (!name.matches(regex)) {
				event.denyLogin(Messages.restrictionRegex.replace("REG_EX", regex));
				return;
			}

			//check additional security
			PlayerAuth auth = authdatabase.getAuth(name);
			if (auth != null) {
				if (!auth.getHostname().isEmpty()) {
					if (event.getHostname().startsWith(auth.getHostname())) {
						connection.addMetadata(ADDITIONAL_SECURITY_VALID_METADATA_KEY, Boolean.TRUE);
					} else {
						event.denyLogin(Messages.optsecurityHostnameWrong);
						return;
					}
				}

				if (auth.isOnlineMode()) {
					event.setOnlineMode(true);
					connection.addMetadata(ADDITIONAL_SECURITY_VALID_METADATA_KEY, Boolean.TRUE);
				}
			}

		} catch (Throwable t) {
			event.denyLogin(ChatColor.DARK_RED + "Error while logging in, please try again");
			getLogger().log(Level.SEVERE, t, () -> "Error while processing login start");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onProfileResolve(PlayerProfileCompleteEvent event) {
		try {
			Profile profile = event.getConnection().getProfile();
			//fix name case if possible
			PlayerAuth auth = authdatabase.getAuth(profile.getOriginalName());
			if (auth != null) {
				event.setForcedName(auth.getRealNickname());
			}
			String name = Utils.ternaryNotNull(event.getForcedName(), profile::getName);
			UUID uuid = Profile.generateOfflineModeUUID(name);
			//force offline mode uuid based on name
			event.setForcedUUID(uuid);
			//dont allow logging in twice
			ProxiedPlayer oplayer = Utils.ternaryNotNull(ProxyServer.getInstance().getPlayer(name), () -> ProxyServer.getInstance().getPlayer(uuid));
			if ((oplayer != null) && (isAuthed(oplayer) || !oplayer.getAddress().getAddress().getHostAddress().equals(event.getConnection().getAddress().getHostString()))) {
				event.denyLogin(Messages.restrictionAlreadyPlaying);
			}
		} catch (Throwable t) {
			event.denyLogin(ChatColor.DARK_RED + "Error while logging in, please try again");
			getLogger().log(Level.SEVERE, t, () -> "Error while processing profile complete");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PostLoginEvent event) {
		try {

			ProxiedPlayer player = event.getPlayer();

			Connection connection = ProtocolSupportAPI.getConnection(player);
			//Don't allow player to use completely different name, or auth will break //TODO: make all auth queries use original name instead
			Profile profile = Utils.getConnection(player).getProfile();
			if (!profile.getOriginalName().equalsIgnoreCase(profile.getName())) {
				player.disconnect(new TextComponent("Internal error: Different original and forced names"));
				return;
			}

			//autologin if additional security check passed
			if (connection.hasMetadata(ADDITIONAL_SECURITY_VALID_METADATA_KEY)) {
				AsyncLogin.login(player);
				return;
			}

			//schedule timeout kick
			long timeout = Settings.timeout;
			if (timeout != 0) {
				ProxyServer.getInstance().getScheduler().schedule(TGBungeeAuthBungee.this, () -> {
					if (player.isConnected() && !isAuthed(player)) {
						player.disconnect(new TextComponent(Messages.timedOut));
					}
				}, timeout, TimeUnit.SECONDS);
			}

			//schedule help login/register message
			String helpmsg = authdatabase.isAuthAvailable(player.getName()) ? Messages.loginHelp : Messages.registerHelp;
			Runnable msgtask = new Runnable() {
				@Override
				public void run() {
					if (player.isConnected() && !isAuthed(player)) {
						player.sendMessage(new TextComponent(helpmsg));
						ProxyServer.getInstance().getScheduler().schedule(TGBungeeAuthBungee.this, this, Settings.messageInterval, TimeUnit.SECONDS);
					}
				}
			};
			msgtask.run();

		} catch (Throwable t) {
			event.getPlayer().disconnect(new TextComponent(ChatColor.DARK_RED + "Error while logging in, please try again"));
			getLogger().log(Level.SEVERE, t, () -> "Error while processing login finish");
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerConnect(ServerConnectEvent event) {
		try {
			ProxiedPlayer player = event.getPlayer();
			if (!isAuthed(player)) {
				ServerInfo authserveri = ProxyServer.getInstance().getServerInfo(Settings.authserver);
				if (authserveri == null) {
					player.disconnect(new TextComponent(ChatColor.RED + "Auth server not found"));
					return;
				}
				Utils.getConnection(player).addMetadata(TARGET_SERVER_METADATA_KEY, event.getTarget());
				event.setTarget(authserveri);
			}
		} catch (Throwable t) {
			event.getPlayer().disconnect(new TextComponent(ChatColor.DARK_RED + "Error while logging in, please try again"));
			getLogger().log(Level.SEVERE, t, () -> "Error while processing server connect");
		}
	}


	protected final HashSet<String> allowedCommands = new HashSet<>(Arrays.asList("/l", "/login", "/reg", "/register"));
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(ChatEvent event) {
		net.md_5.bungee.api.connection.Connection sender = event.getSender();
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if (!isAuthed(player)) {
				String[] split = event.getMessage().split("\\s+");
				if (!allowedCommands.contains(split[0])) {
					event.setCancelled(true);
					player.sendMessage(new TextComponent(Messages.notloggedin));
				}
			}
		}
	}

}
