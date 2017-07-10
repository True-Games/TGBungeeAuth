package tgbungeeauth.server.auth.managment;

import java.io.IOException;

import org.bukkit.entity.Player;

import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;
import tgbungeeauth.server.MessageWriter;
import tgbungeeauth.server.auth.config.Messages;
import tgbungeeauth.server.auth.db.DataSource;
import tgbungeeauth.server.auth.db.PlayerAuth;
import tgbungeeauth.shared.ChannelNames;

public class SharedManagement {

	public static void finish(DataSource database, Player player, String hash, boolean isRegister) throws IOException {
		PlayerAuth auth = new PlayerAuth(player.getName().toLowerCase(), player.getName(), hash, player.getAddress().getHostString(), System.currentTimeMillis());
		if (isRegister) {
			database.saveAuth(auth);
		} else {
			database.updateSession(auth);
		}

		setAuthed(player);

		player.sendMessage(isRegister ? Messages.registerSuccess : Messages.loginSuccess);

		MessageWriter.writeMessage(player, ChannelNames.AUTHSUCCESS_SUBCHANNEL, stream -> {});
	}

	private static final String AUTHED_METADATA = "tgbungeeauth.authed";

	public static void setAuthed(Player player) {
		Connection connection = ProtocolSupportAPI.getConnection(player);
		if (connection != null) {
			setAuthed(connection);
		}
	}

	public static void setAuthed(Connection connection) {
		connection.addMetadata(AUTHED_METADATA, Boolean.TRUE);
	}

	public static boolean isAuthed(Player player) {
		Connection connection = ProtocolSupportAPI.getConnection(player);
		if (connection != null) {
			return connection.hasMetadata(AUTHED_METADATA);
		}
		return false;
	}

	private static final String FORCEAUTH_METADATA = "tgbungeeauth.forceath";

	public static void setForceAuth(Connection connection) {
		connection.addMetadata(FORCEAUTH_METADATA, Boolean.TRUE);
	}

	public static boolean isForceAuth(Player player) {
		Connection connection = ProtocolSupportAPI.getConnection(player);
		if (connection != null) {
			return connection.hasMetadata(FORCEAUTH_METADATA);
		}
		return false;
	}

}
