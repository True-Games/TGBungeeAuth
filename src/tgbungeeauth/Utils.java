package tgbungeeauth;

import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;

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

}
