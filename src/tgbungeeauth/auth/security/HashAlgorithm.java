package tgbungeeauth.auth.security;

import tgbungeeauth.auth.security.crypts.DOUBLEMD5;
import tgbungeeauth.auth.security.crypts.MD5;
import tgbungeeauth.auth.security.crypts.SHA1;
import tgbungeeauth.auth.security.crypts.SHA256;
import tgbungeeauth.auth.security.crypts.SHA512;
import tgbungeeauth.auth.security.crypts.WHIRLPOOL;
import tgbungeeauth.auth.security.crypts.XAUTH;

public enum HashAlgorithm {

	MD5(MD5.class),
	SHA1(SHA1.class),
	SHA256(SHA256.class),
	WHIRLPOOL(WHIRLPOOL.class),
	XAUTH(XAUTH.class),
	SHA512(SHA512.class),
	DOUBLEMD5(DOUBLEMD5.class);

	private Class<?> implClass;

	HashAlgorithm(Class<?> classe) {
		this.implClass = classe;
	}

	public Class<?> getImplClass() {
		return implClass;
	}

}