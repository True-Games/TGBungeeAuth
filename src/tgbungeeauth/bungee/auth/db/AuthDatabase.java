package tgbungeeauth.bungee.auth.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import tgbungeeauth.bungee.TGBungeeAuthBungee;

public class AuthDatabase {

	private final FileDataBackend source;
	private final int autosaveinterval;

	private final HashMap<String, PlayerAuth> authCache = new HashMap<>();
	private final HashMap<String, HashSet<String>> ipCache = new HashMap<>();

	private ScheduledTask autosavetask = null;

	public AuthDatabase(FileDataBackend databackend, int autosaveinterval) {
		this.source = databackend;
		this.autosaveinterval = autosaveinterval;
	}

	public synchronized void load() {
		cacheAllAuths();
		scheduleAutoSaveTask();
	}

	public synchronized boolean isAuthAvailable(String user) {
		return authCache.containsKey(user.toLowerCase());
	}

	public synchronized PlayerAuth getAuth(String user) {
		return authCache.get(user.toLowerCase());
	}

	public synchronized void saveAuth(PlayerAuth auth) {
		cacheAuth(auth);
	}

	public synchronized void removeAuth(String user) {
		clearAuth(user.toLowerCase());
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

	public synchronized void save() {
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
		autosavetask = ProxyServer.getInstance().getScheduler().schedule(TGBungeeAuthBungee.getInstance(), new Runnable() {
			@Override
			public void run() {
				save();
			}
		}, autosaveinterval, autosaveinterval, TimeUnit.SECONDS);
	}

}
