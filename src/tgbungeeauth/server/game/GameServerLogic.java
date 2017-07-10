package tgbungeeauth.server.game;

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
import tgbungeeauth.shared.ChannelNames;

public class GameServerLogic implements Listener, ServerLogic {

	private String secureKey;
	private String secureKeyTimeoutMessage;
	private String secureKeyMissmatchMessage;

	@Override
	public void start() {
		secureKey = TGBungeeAuthBukkit.getInstance().getConfig().getString("securekey");
		secureKeyTimeoutMessage = ChatColor.translateAlternateColorCodes('&', TGBungeeAuthBukkit.getInstance().getConfig().getString("gameserver.messages.timeout"));
		secureKeyMissmatchMessage = ChatColor.translateAlternateColorCodes('&', TGBungeeAuthBukkit.getInstance().getConfig().getString("gameserver.messages.missmatch"));
		Bukkit.getPluginManager().registerEvents(this, TGBungeeAuthBukkit.getInstance());
	}

	@Override
	public void stop() {
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerConnect(PlayerLoginFinishEvent event) {
		if (secureKey.equals("ChangeThisKeyAfterInstall")) {
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
							if (stream.readUTF().equals(ChannelNames.TGAUTH_BUNGEE_SUBCHANNEL) && stream.readUTF().equals(ChannelNames.SECUREKEY_SUBCHANNEL)) {
								if (!stream.readUTF().equals(secureKey)) {
									event.denyLogin(secureKeyMissmatchMessage);
								}
							}
						} catch (IOException e) {
							event.denyLogin(ChatColor.DARK_RED + "Exception while decoding secure key");
						}
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
				event.denyLogin(secureKeyTimeoutMessage);
			}
		} catch (InterruptedException e) {
			event.denyLogin(ChatColor.DARK_RED + "Exception while waiting for secure key");
		}
	}

}
