package tgbungeeauth.bungee;

import java.util.HashSet;

public class Database {

	private HashSet<String> onlineMode = new HashSet<>();

	public void addOnlineMode(String name) {
		onlineMode.add(name.toLowerCase());
	}

	public void removeOnlineMode(String name) {
		onlineMode.remove(name.toLowerCase());
	}

	public boolean isOnlineMode(String name) {
		return onlineMode.contains(name.toLowerCase());
	}

	public void load() {
	}

	public void save() {
	}

}
