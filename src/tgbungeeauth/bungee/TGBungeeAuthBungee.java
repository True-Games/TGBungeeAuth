package tgbungeeauth.bungee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.PatternSyntaxException;

import com.google.common.base.Charsets;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerPreConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import tgbungeeauth.bungee.auth.db.AuthDataStorage;
import tgbungeeauth.bungee.auth.db.AuthDatabase;
import tgbungeeauth.bungee.auth.db.PlayerAuth;
import tgbungeeauth.bungee.auth.managment.AsyncLogin;
import tgbungeeauth.bungee.commands.AdminCommand;
import tgbungeeauth.bungee.commands.ChangePasswordCommand;
import tgbungeeauth.bungee.commands.HostnameCommand;
import tgbungeeauth.bungee.commands.LicenseCommand;
import tgbungeeauth.bungee.commands.LoginCommand;
import tgbungeeauth.bungee.commands.RegisterCommand;
import tgbungeeauth.bungee.config.Messages;
import tgbungeeauth.bungee.config.Settings;
import tgbungeeauth.shared.ChannelNames;

public class TGBungeeAuthBungee extends Plugin implements Listener {

	private static TGBungeeAuthBungee instance;
	public static TGBungeeAuthBungee getInstance() {
		return instance;
	}

	public TGBungeeAuthBungee() {
		instance = this;
	}

	private final Set<UUID> succauth = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<UUID, ServerInfo> targetservers = new ConcurrentHashMap<>();

	public boolean isAuthed(ProxiedPlayer player) {
		return succauth.contains(player.getUniqueId());
	}

	public void finishAuth(ProxiedPlayer player) {
		succauth.add(player.getUniqueId());
		ServerInfo target = targetservers.get(player.getUniqueId());
		if (target != null) {
			player.connect(target);
		}
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
				provider.save(BungeeUtils.copyDefaultOptions(provider.load(configfile), provider.load(defconfigfile)), configfile);
			} catch (IOException e) {
			}
			defconfigfile.delete();
		}

		try {
			Class.forName(ServerPreConnectedEvent.class.getName());
		} catch (Throwable t) {
			getLogger().severe("Unable to find special event");
			ProxyServer.getInstance().stop();
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

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {

		String name = event.getConnection().getName();

		String regex = Settings.nickRegex;

		if ((name.length() > Settings.maxNickLength) || (name.length() < Settings.minNickLength)) {
			event.setCancelReason(new TextComponent(Messages.restrictionNameLength));
			return;
		}

		try {
			if (!name.matches(regex)) {
				event.setCancelled(true);
				event.setCancelReason(new TextComponent(Messages.restrictionRegex.replace("REG_EX", regex)));
				return;
			}
		} catch (PatternSyntaxException pse) {
			event.setCancelled(true);
			event.setCancelReason(new TextComponent("Invalid regex configured. Please norify administrator about this"));
			return;
		}

		ProxiedPlayer oplayer = null;
		try {
			oplayer = ProxyServer.getInstance().getPlayer(name);
		} catch (Throwable t) {
			event.setCancelled(true);
			event.setCancelReason(new TextComponent("Error while logging in, please try again"));
		}
		if ((oplayer != null) && !oplayer.getAddress().getAddress().getHostAddress().equals(event.getConnection().getAddress().getHostString())) {
			event.setCancelled(true);
			event.setCancelReason(new TextComponent(Messages.restrictionAlreadyPlaying));
			return;
		}

		PlayerAuth auth = authdatabase.getAuth(name);
		if (auth != null) {
			String realnickname = auth.getRealNickname();
			if (!name.equals(realnickname)) {
				event.setCancelled(true);
				event.setCancelReason(new TextComponent(Messages.restrictionInvalidCase.replace("REALNAME", realnickname)));
				return;
			}

			if (!auth.getHostname().isEmpty()) {
				if (!event.getConnection().getVirtualHost().getHostString().startsWith(auth.getHostname())) {
					event.setCancelled(true);
					event.setCancelReason(new TextComponent(Messages.optsecurityHostnameWrong));
					return;
				}
			}

			if (auth.isOnlineMode()) {
				event.getConnection().setOnlineMode(true);
				event.getConnection().setForcedUniqueId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8)));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		PlayerAuth auth = authdatabase.getAuth(player.getName());
		if (auth != null && (auth.isOnlineMode() || !auth.getHostname().isEmpty())) {
			AsyncLogin.login(player);
			return;
		}

		long timeout = Settings.timeout;
		if (timeout != 0) {
			ProxyServer.getInstance().getScheduler().schedule(TGBungeeAuthBungee.this, () -> {
				if (player.isConnected() && !isAuthed(player)) {
					player.disconnect(new TextComponent(Messages.timedOut));
				}
			}, timeout, TimeUnit.SECONDS);
		}

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
	}

	private final HashSet<String> allowedCommands = new HashSet<>(Arrays.asList("/l", "/login", "/reg", "/register"));
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(ChatEvent event) {
		Connection sender = event.getSender();
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

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		succauth.remove(player.getUniqueId());
		targetservers.remove(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerConnect(ServerConnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		if (!isAuthed(player)) {
			targetservers.put(player.getUniqueId(), event.getTarget());
			ServerInfo authserveri = ProxyServer.getInstance().getServerInfo(Settings.authserver);
			if (authserveri == null) {
				player.disconnect(new TextComponent(ChatColor.RED + "Auth server not found"));
				return;
			}
			event.setTarget(authserveri);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreConnect(ServerPreConnectedEvent event) throws IOException {
		ProxiedPlayer player = event.getPlayer();
		Server server = event.getServer();
		if (server.getInfo().getName().equals(Settings.authserver) && succauth.contains(player.getUniqueId())) {
			BungeeUtils.writePluginMessage(server, ChannelNames.SECUREKEY_SUBCHANNEL, stream -> {
				stream.writeUTF(Settings.securekey);
			});
		}
	}

}
