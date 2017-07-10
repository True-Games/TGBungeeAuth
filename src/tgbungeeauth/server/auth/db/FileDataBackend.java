package tgbungeeauth.server.auth.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tgbungeeauth.server.TGBungeeAuthBukkit;

public class FileDataBackend {

	/*
	 * file layout:
	 *
	 * DBVER$2:PLAYERNAME:REALPLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS
	 *
	 * Old but compatible:
	 * DBVER$1:PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS
	 * PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS:LASTPOSX:LASTPOSY:LASTPOSZ:LASTPOSWORLD
	 * PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS:LASTPOSX:LASTPOSY:LASTPOSZ
	 * PLAYERNAME:HASHSUM:IP:LOGININMILLIESECONDS
	 * PLAYERNAME:HASHSUM:IP
	 * PLAYERNAME:HASHSUM
	 */
	private final File source;

	private final int dbvers = 2;

	public FileDataBackend(String file) throws IOException {
		source = new File(TGBungeeAuthBukkit.getInstance().getDataFolder(), file);
		source.createNewFile();
	}

	public List<PlayerAuth> getAllAuths() {
		convertDatabase();
		List<PlayerAuth> auths = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(source));
			String line;
			while ((line = br.readLine()) != null) {
				auths.add(convertDBStringToAuth(line));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return auths;
	}

	public void dumpAuths(Collection<PlayerAuth> auths) {
		File newsource = new File(source.getParentFile(), "___tmpdb");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(newsource, false));
			for (PlayerAuth auth : auths) {
				writer.write(convertAuthToDBString(auth));
			}
			writer.close();
			try {
				Files.move(newsource.toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(newsource.toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String convertAuthToDBString(PlayerAuth auth) {
		StringBuilder sb = new StringBuilder();
		sb.append("DBVER$");
		sb.append(dbvers);
		sb.append(":");
		sb.append(auth.getNickname());
		sb.append(":");
		sb.append(auth.getRealNickname());
		sb.append(":");
		sb.append(auth.getHash());
		sb.append(":");
		sb.append(auth.getIp());
		sb.append(":");
		sb.append(auth.getLastLogin());
		sb.append("\n");
		return sb.toString();
	}

	private PlayerAuth convertDBStringToAuth(String dbstring) {
		String[] args = dbstring.split(":");
		return new PlayerAuth(args[1], args[2], args[3], args[4], Long.parseLong(args[5]));
	}

	private void convertDatabase() {
		List<PlayerAuth> auths = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(source));
			String line;
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("DBVER$" + dbvers)) {
					auths.add(parseOldAuth(line));
				} else {
					auths.add(convertDBStringToAuth(line));
				}
			}
			br.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(source, false));
			for (PlayerAuth auth : auths) {
				try {
					writer.write(convertAuthToDBString(auth));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private PlayerAuth parseOldAuth(String line) {
		PlayerAuth auth = null;
		String[] args = line.split(":");
		if (line.startsWith("DBVER$")) {
			if (args[0].equalsIgnoreCase("DBVER$1")) {
				auth = new PlayerAuth(args[1], "", args[2], args[3], Long.parseLong(args[4]));
			}
		} else {
			switch (args.length) {
				case 9:
				case 8:
				case 7:
				case 4: {
					auth = new PlayerAuth(args[0], "", args[1], args[2], Long.parseLong(args[3]));
					break;
				}
				case 3: {
					auth = new PlayerAuth(args[0], "", args[1], args[2], 0);
					break;
				}
				case 2: {
					auth = new PlayerAuth(args[0], "", args[1], "198.18.0.1", 0);
					break;
				}
			}
		}
		return auth;
	}

}
