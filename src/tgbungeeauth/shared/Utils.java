package tgbungeeauth.shared;

import java.io.DataOutputStream;
import java.io.IOException;

public class Utils {

	@FunctionalInterface
	public static interface DataOutputStreamWriter {
		public void write(DataOutputStream stream) throws IOException;
	}

}
