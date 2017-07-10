package tgbungeeauth.server.auth;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import protocolsupport.api.Connection.PacketReceiveListener;
import protocolsupport.api.events.PlayerLoginFinishEvent;
import tgbungeeauth.server.ServerLogic;
import tgbungeeauth.server.TGBungeeAuthBukkit;
import tgbungeeauth.server.auth.config.Messages;
import tgbungeeauth.server.auth.config.Settings;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.db.FileDataBackend;
import tgbungeeauth.server.auth.logic.AdminCommand;
import tgbungeeauth.server.auth.logic.LoginCommand;
import tgbungeeauth.server.auth.logic.LoginListener;
import tgbungeeauth.server.auth.logic.RegisterCommand;
import tgbungeeauth.server.auth.managment.SharedManagement;
import tgbungeeauth.shared.ChannelNames;

public class AuthServerLogic implements ServerLogic, Listener {

	private String authKey;
	private String authKeyTimeoutMessage;
	private String authKeyMissmatchMessage;

	private DataSource datasource;

	@Override
	public void start() throws IOException {
		authKey = TGBungeeAuthBukkit.getInstance().getConfig().getString("authserverkey");
		authKeyTimeoutMessage = ChatColor.translateAlternateColorCodes('&', TGBungeeAuthBukkit.getInstance().getConfig().getString("authserver.messages.timeout"));
		authKeyMissmatchMessage = ChatColor.translateAlternateColorCodes('&', TGBungeeAuthBukkit.getInstance().getConfig().getString("authserver.messages.missmatch"));
		Bukkit.getPluginManager().registerEvents(this, TGBungeeAuthBukkit.getInstance());
		Bukkit.getMessenger().registerOutgoingPluginChannel(TGBungeeAuthBukkit.getInstance(), ChannelNames.BUNGEE_CHANNEL);

		Settings.loadConfig();
		Messages.loadConfig();
		datasource = new DataSource(new FileDataBackend("auths.db"), 20);
		Bukkit.getPluginManager().registerEvents(new LoginListener(datasource), TGBungeeAuthBukkit.getInstance());
		TGBungeeAuthBukkit.getInstance().getCommand("login").setExecutor(new LoginCommand(datasource));
		TGBungeeAuthBukkit.getInstance().getCommand("register").setExecutor(new RegisterCommand(datasource));
		TGBungeeAuthBukkit.getInstance().getCommand("tgbungeeauthauthserveradmin").setExecutor(new AdminCommand(datasource));
	}

	@Override
	public void stop() {
		datasource.saveDatabase();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerConnect(PlayerLoginFinishEvent event) {
		if (authKey.equals("ChangeThisKeyAfterInstall")) {
			event.denyLogin(ChatColor.DARK_RED + "Using default secure key is not allowed. Please notify server administrator about this.");
			return;
		}
		CountDownLatch latch = new CountDownLatch(1);
		event.getConnection().addPacketReceiveListener(new PacketReceiveListener() {
			@Override
			public boolean onPacketReceiving(Object packet) {
				PacketContainer container = PacketContainer.fromPacket(packet);
				if (container.getType() == PacketType.Play.Client.CUSTOM_PAYLOAD) {
					if (container.getStrings().read(0).equals(ChannelNames.BUNGEE_CHANNEL)) {
						try (DataInputStream stream = new DataInputStream(new ByteBufInputStream((ByteBuf) container.getModifier().read(1)))) {
							if (stream.readUTF().equals(ChannelNames.TGAUTH_BUNGEE_SUBCHANNEL) && stream.readUTF().equals(ChannelNames.AUTHKEY_SUBCHANNEL)) {
								if (!stream.readUTF().equals(authKey)) {
									event.denyLogin(authKeyMissmatchMessage);
								}
								if (stream.readBoolean()) {
									SharedManagement.setAuthed(event.getConnection());
								}
								if (stream.readBoolean()) {
									SharedManagement.setForceAuth(event.getConnection());
								}
							}
						} catch (IOException e) {
							event.denyLogin(ChatColor.DARK_RED + "Exception while decoding auth key");
						}
						event.getConnection().removePacketReceiveListener(this);
						latch.countDown();
						return false;
					}
				}
				return true;
			}
		});
		try {
			boolean confirmed = latch.await(10, TimeUnit.SECONDS);
			if (!confirmed) {
				event.denyLogin(authKeyTimeoutMessage);
			}
		} catch (InterruptedException e) {
			event.denyLogin(ChatColor.DARK_RED + "Exception while waiting for auth key");
		}
	}

}
