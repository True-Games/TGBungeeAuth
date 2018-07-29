package tgbungeeauth.auth.db;

public class PlayerAuth {

	private final String realnickname;
	private String hash;
	private String ip = "198.18.0.1";
	private long lastLogin = 0;
	private boolean onlinemode = false;
	private String hostname = "";

	public PlayerAuth(String realnickname, String hash) {
		this.realnickname = realnickname;
		this.hash = hash;
	}

	public PlayerAuth(String realnickname, String hash, String ip, long lastlogin, boolean onlinemode, String hostname) {
		this.realnickname = realnickname;
		this.hash = hash;
		this.ip = ip;
		this.lastLogin = lastlogin;
		this.onlinemode = onlinemode;
		this.hostname = hostname;
	}

	public String getIp() {
		return ip;
	}

	public String getRealNickname() {
		return realnickname;
	}

	public String getHash() {
		return hash;
	}

	public long getLastLogin() {
		return lastLogin;
	}

	public boolean isOnlineMode() {
		return onlinemode;
	}

	public String getHostname() {
		return this.hostname;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}

	public void setOnlineMode(boolean onlinemode) {
		this.onlinemode = onlinemode;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

}
