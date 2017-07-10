package tgbungeeauth.bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.connection.Server;
import tgbungeeauth.shared.ChannelNames;
import tgbungeeauth.shared.Utils.DataOutputStreamWriter;

public class MessageWriter {

	public static void writeMessage(Server server, String subchannel, DataOutputStreamWriter writer) throws IOException {
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bstream);
		stream.writeUTF(ChannelNames.TGAUTH_BUNGEE_SUBCHANNEL);
		stream.writeUTF(subchannel);
		writer.write(stream);
		server.sendData(ChannelNames.BUNGEE_CHANNEL, bstream.toByteArray());
	}

}
