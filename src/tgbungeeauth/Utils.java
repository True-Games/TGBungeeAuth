package tgbungeeauth;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.function.Supplier;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolSupportAPI;

public class Utils {

	@FunctionalInterface
	public static interface DataOutputStreamWriter {
		public void write(DataOutputStream stream) throws IOException;
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

	public static <T> T ternaryNotNull(T val1, Supplier<T> val2) {
		return val1 != null ? val1 : val2.get();
	}

	public static Connection getConnection(ProxiedPlayer player) {
		Connection connection = ProtocolSupportAPI.getConnection(player);
		if (connection == null) {
			throw new IllegalArgumentException("Internal error: Null ps connection");
		}
		return connection;
	}

}
