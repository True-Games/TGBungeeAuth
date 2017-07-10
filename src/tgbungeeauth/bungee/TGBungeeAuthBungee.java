package tgbungeeauth.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerPreConnectedEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import tgbungeeauth.shared.ChannelNames;

public class TGBungeeAuthBungee extends Plugin implements Listener {

	private String authserver;
	private String authserverkey;
	private String authserverfailconnectmessage;
	private String securekey;

	private String licenseMessageAdded;
	private String licenseMessageRemoved;
	private String licenseMessageLoginFirst;

	private final Map<UUID, ServerInfo> targetservers = new ConcurrentHashMap<>();
	private final Set<UUID> succauth = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private final Database database = new Database();

	@Override
	public void onEnable() {
		getDataFolder().mkdir();

		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			try (InputStream in = getResourceAsStream("config.yml")) {
				Files.copy(in, file.toPath());
			} catch (IOException e) {
			}
		}

		try {
			Class.forName(ServerPreConnectedEvent.class.getName());
		} catch (Throwable t) {
			getLogger().severe("Unable to find special event");
			ProxyServer.getInstance().stop();
		}

		loadConfig();

		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Command("tgauthbungeebungeeadmin") {
			@Override
			public void execute(CommandSender sender, String[] args) {
				if (!sender.hasPermission("tgbungeeauth.admin")) {
					sender.sendMessage(new TextComponent(ChatColor.RED + "No perms"));
					return;
				}

				if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
					loadConfig();
					sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Config reloaded"));
				}
			}
		});

		database.load();

		ProxyServer.getInstance().getPluginManager().registerCommand(this, new Command("license") {
			@Override
			public void execute(CommandSender sender, String[] args) {
				if (sender instanceof ProxiedPlayer) {
					ProxiedPlayer player = (ProxiedPlayer) sender;
					if (succauth.contains(player.getUniqueId())) {
						String name = player.getName();
						if (database.isOnlineMode(name)) {
							database.removeOnlineMode(name);
							player.sendMessage(new TextComponent(licenseMessageRemoved));
						} else {
							database.addOnlineMode(name);
							player.sendMessage(new TextComponent(licenseMessageAdded));
						}
					} else {
						player.sendMessage(new TextComponent(licenseMessageLoginFirst));
					}
				}
			}
		});
		ProxyServer.getInstance().getPluginManager().registerListener(this, this);
	}

	private void loadConfig() {
		try {
			Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			authserver = configuration.getString("bungeecord.authserver");
			authserverkey = configuration.getString("authserverkey");
			authserverfailconnectmessage = ChatColor.translateAlternateColorCodes('&', configuration.getString("bungeecord.messages.noconnection"));
			securekey = configuration.getString("securekey");
			licenseMessageAdded = ChatColor.translateAlternateColorCodes('&', configuration.getString("bungeecord.license.messages.enabled"));
			licenseMessageRemoved = ChatColor.translateAlternateColorCodes('&', configuration.getString("bungeecord.license.messages.disabled"));
			licenseMessageLoginFirst = ChatColor.translateAlternateColorCodes('&', configuration.getString("bungeecord.license.messages.notloggedin"));
		} catch (IOException e) {
			getLogger().severe("Unable to load config");
			ProxyServer.getInstance().stop();
		}
	}

	@Override
	public void onDisable() {
		database.save();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onHS(PreLoginEvent event) {
		if (database.isOnlineMode(event.getConnection().getName())) {
			event.getConnection().setOnlineMode(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		succauth.remove(player.getUniqueId());
		targetservers.remove(player.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		targetservers.put(player.getUniqueId(), findServer(player.getPendingConnection().getVirtualHost()));
		ServerInfo info = ProxyServer.getInstance().getServerInfo(authserver);
		if (info == null) {
			player.disconnect(new TextComponent(ChatColor.RED + "Auth server not found"));
			return;
		}
		player.connect(info, new Callback<Boolean>() {
			@Override
			public void done(Boolean s, Throwable t) {
				if (!s) {
					player.disconnect(new TextComponent(authserverfailconnectmessage));
				}
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void inPluginMessage(PluginMessageEvent event) throws IOException {
		if (!(event.getSender() instanceof Server)) {
			return;
		}
		if (!(event.getReceiver() instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = ((ProxiedPlayer) event.getReceiver());
		if (event.getTag().equals(ChannelNames.BUNGEE_CHANNEL)) {
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(event.getData()));
			if (stream.readUTF().equals(ChannelNames.TGAUTH_BUNGEE_SUBCHANNEL) && stream.readUTF().equals(ChannelNames.AUTHSUCCESS_SUBCHANNEL)) {
				event.setCancelled(true);
				succauth.add(player.getUniqueId());
				player.connect(targetservers.get(player.getUniqueId()));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreConnect(ServerPreConnectedEvent event) throws IOException {
		ProxiedPlayer player = event.getPlayer();
		Server server = event.getServer();
		ServerInfo info = server.getInfo();
		boolean authed = succauth.contains(player.getUniqueId());
		if (info.getName().equals(authserver)) {
			MessageWriter.writeMessage(server, ChannelNames.AUTHKEY_SUBCHANNEL, stream -> {
				stream.writeUTF(authserverkey);
				stream.writeBoolean(authed);
				stream.writeBoolean(database.isOnlineMode(player.getName()));
			});
		} else if (authed) {
			MessageWriter.writeMessage(server, ChannelNames.SECUREKEY_SUBCHANNEL, stream -> {
				stream.writeUTF(securekey);
			});
		}
	}

	private static ServerInfo findServer(InetSocketAddress address) {
		for (ServerInfo serverinfo : ProxyServer.getInstance().getServers().values()) {
			if (serverinfo.getAddress().equals(address)) {
				return serverinfo;
			}
		}
		return ProxyServer.getInstance().getServers().values().iterator().next();
	}

}
