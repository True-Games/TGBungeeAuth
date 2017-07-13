package tgbungeeauth.bungee;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.config.Configuration;
import tgbungeeauth.shared.ChannelNames;
import tgbungeeauth.shared.Utils.DataOutputStreamWriter;

public class BungeeUtils {

	public static void writePluginMessage(Server server, String subchannel, DataOutputStreamWriter writer) throws IOException {
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bstream);
		stream.writeUTF(ChannelNames.TGAUTH_BUNGEE_SUBCHANNEL);
		stream.writeUTF(subchannel);
		writer.write(stream);
		server.sendData(ChannelNames.BUNGEE_CHANNEL, bstream.toByteArray());
	}

	public static Configuration copyDefaultOptions(Configuration config, Configuration defaults) {
		for (String key : defaults.getKeys()) {
			Object defval = defaults.get(key);
			Object confgival = config.get(key);
			if (confgival == null) {
				config.set(key, defval);
			} else if (defval instanceof Configuration) {
				copyDefaultOptions((Configuration) confgival, (Configuration) defval);
			}
		}
		return config;
	}

}
