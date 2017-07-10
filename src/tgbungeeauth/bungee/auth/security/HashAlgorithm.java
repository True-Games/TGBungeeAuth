package tgbungeeauth.bungee.auth.security;

import tgbungeeauth.bungee.auth.security.crypts.DOUBLEMD5;
import tgbungeeauth.bungee.auth.security.crypts.MD5;
import tgbungeeauth.bungee.auth.security.crypts.SHA1;
import tgbungeeauth.bungee.auth.security.crypts.SHA256;
import tgbungeeauth.bungee.auth.security.crypts.SHA512;
import tgbungeeauth.bungee.auth.security.crypts.WHIRLPOOL;
import tgbungeeauth.bungee.auth.security.crypts.XAUTH;

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