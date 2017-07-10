package tgbungeeauth.server;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.entity.Player;

import tgbungeeauth.shared.ChannelNames;
import tgbungeeauth.shared.Utils.DataOutputStreamWriter;

public class MessageWriter {

	public static void writeMessage(Player player, String subchannel, DataOutputStreamWriter writer) throws IOException {
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bstream);
		stream.writeUTF(ChannelNames.TGAUTH_BUNGEE_SUBCHANNEL);
		stream.writeUTF(subchannel);
		writer.write(stream);
		player.sendPluginMessage(TGBungeeAuthBukkit.getInstance(), ChannelNames.BUNGEE_CHANNEL, bstream.toByteArray());
	}

}
