package tgbungeeauth.server.auth.security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import tgbungeeauth.server.auth.security.crypts.EncryptionMethod;

public class PasswordSecurity {

	private static final Random rnd = new Random();

	private static String createSalt(int length) throws NoSuchAlgorithmException {
		byte[] msg = new byte[40];
		rnd.nextBytes(msg);
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		sha1.reset();
		byte[] digest = sha1.digest(msg);
		return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).substring(0, length);
	}

	public static String getHash(HashAlgorithm alg, String password, String playerName) throws NoSuchAlgorithmException {
		EncryptionMethod method;
		try {
			method = (EncryptionMethod) alg.getImplClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new NoSuchAlgorithmException("Problem with this hash algorithm");
		}
		String salt = "";
		switch (alg) {
			case SHA256:
				salt = createSalt(16);
				break;
			case XAUTH:
				salt = createSalt(12);
				break;
			case MD5:
			case SHA1:
			case WHIRLPOOL:
			case SHA512:
			case DOUBLEMD5:
				break;
			default:
				throw new NoSuchAlgorithmException("Unknown hash algorithm");
		}
		return method.getHash(password, salt);
	}

	public static boolean comparePasswordWithHash(String password, String hash, String playerName) {
		for (HashAlgorithm algo : HashAlgorithm.values()) {
			try {
				EncryptionMethod method = (EncryptionMethod) algo.getImplClass().newInstance();
				if (method.comparePassword(hash, password, playerName)) {
					return true;
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

}
