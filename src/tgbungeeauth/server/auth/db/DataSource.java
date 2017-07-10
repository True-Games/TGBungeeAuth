package tgbungeeauth.server.auth.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import tgbungeeauth.server.TGBungeeAuthBukkit;

public class DataSource {

	private final FileDataBackend source;
	private final int autosaveinterval;

	private final HashMap<String, PlayerAuth> authCache = new HashMap<>();
	private final HashMap<String, HashSet<String>> ipCache = new HashMap<>();

	private BukkitTask autosavetask = null;

	public DataSource(FileDataBackend databackend, int autosaveinterval) {
		this.source = databackend;
		this.autosaveinterval = autosaveinterval;
		cacheAllAuths();
		scheduleAutoSaveTask();
	}

	public synchronized boolean isAuthAvailable(String user) {
		return authCache.containsKey(user);
	}

	public synchronized PlayerAuth getAuth(String user) {
		return authCache.get(user);
	}

	public synchronized void saveAuth(PlayerAuth auth) {
		cacheAuth(auth);
	}

	public synchronized void removeAuth(String user) {
		clearAuth(user);
	}

	public synchronized void updatePassword(PlayerAuth auth) {
		authCache.get(auth.getNickname()).setHash(auth.getHash());
	}

	public synchronized void updateSession(PlayerAuth auth) {
		authCache.get(auth.getNickname()).setIp(auth.getIp());
		authCache.get(auth.getNickname()).setLastLogin(auth.getLastLogin());
	}

	public synchronized List<String> getAllAuthsByIp(String ip) {
		if (ipCache.containsKey(ip)) {
			return new ArrayList<>(ipCache.get(ip));
		} else {
			return new ArrayList<>();
		}
	}

	public synchronized int purgeDatabase(long until) {
		int cleared = 0;
		for (PlayerAuth auth : new LinkedList<>(authCache.values())) {
			if (auth.getLastLogin() < until) {
				clearAuth(auth.getNickname());
				cleared++;
			}
		}
		return cleared;
	}

	public synchronized void saveDatabase() {
		source.dumpAuths(authCache.values());
	}

	public synchronized void reload() {
		if (autosavetask != null) {
			autosavetask.cancel();
		}
		scheduleAutoSaveTask();
		authCache.clear();
		ipCache.clear();
		cacheAllAuths();
	}

	private void cacheAllAuths() {
		List<PlayerAuth> auths = source.getAllAuths();
		for (PlayerAuth auth : auths) {
			cacheAuth(auth);
		}
	}

	private void cacheAuth(PlayerAuth auth) {
		String nick = auth.getNickname();
		authCache.put(nick, auth);
		String ip = auth.getIp();
		if (!ipCache.containsKey(ip)) {
			ipCache.put(ip, new HashSet<String>());
		}
		ipCache.get(ip).add(nick);
	}

	private void clearAuth(String nick) {
		PlayerAuth auth = authCache.get(nick);
		if (auth == null) {
			return;
		}
		authCache.remove(nick);
		String ip = auth.getIp();
		if (ipCache.containsKey(ip)) {
			ipCache.get(ip).remove(nick);
			if (ipCache.get(ip).isEmpty()) {
				ipCache.remove(ip);
			}
		}
	}

	private void scheduleAutoSaveTask() {
		autosavetask = Bukkit.getScheduler().runTaskTimerAsynchronously(TGBungeeAuthBukkit.getInstance(), new Runnable() {
			@Override
			public void run() {
				saveDatabase();
			}
		}, 20 * autosaveinterval, 20 * autosaveinterval);
	}

}
