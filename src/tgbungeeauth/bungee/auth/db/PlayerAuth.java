package tgbungeeauth.bungee.auth.db;

public class PlayerAuth {

	private String nickname = "";
	private String realnickname = "";
	private String hash = "";
	private String ip = "198.18.0.1";
	private long lastLogin = 0;

	public PlayerAuth(String nickname, String realnickname, String hash, String ip, long lastLogin) {
		this.nickname = nickname;
		this.realnickname = realnickname;
		this.hash = hash;
		this.ip = ip;
		this.lastLogin = lastLogin;
	}

	public String getIp() {
		return ip;
	}

	public String getNickname() {
		return nickname;
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

	public void setHash(String hash) {
		this.hash = hash;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setLastLogin(long lastLogin) {
		this.lastLogin = lastLogin;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PlayerAuth)) {
			return false;
		}
		PlayerAuth other = (PlayerAuth) obj;
		return other.getIp().equals(this.ip) && other.getNickname().equals(this.nickname);
	}

	@Override
	public int hashCode() {
		int hashCode = 7;
		hashCode = (71 * hashCode) + (this.nickname != null ? this.nickname.hashCode() : 0);
		hashCode = (71 * hashCode) + (this.ip != null ? this.ip.hashCode() : 0);
		return hashCode;
	}

	@Override
	public String toString() {
		return "Player : " + nickname + " ! IP : " + ip + " ! LastLogin : " + lastLogin + " ! Hash : " + hash;
	}

}
