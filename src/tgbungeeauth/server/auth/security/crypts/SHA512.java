package tgbungeeauth.server.auth.security.crypts;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA512 implements EncryptionMethod {

	@Override
	public String getHash(String password, String salt) throws NoSuchAlgorithmException {
		MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
		sha512.reset();
		sha512.update(password.getBytes());
		byte[] digest = sha512.digest();
		return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
	}

	@Override
	public boolean comparePassword(String hash, String password, String playerName) throws NoSuchAlgorithmException {
		return hash.equals(getHash(password, ""));
	}

}
