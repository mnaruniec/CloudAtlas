package pl.edu.mimuw.cloudatlas.signing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyReader {
	private KeyReader() {}

	public static PrivateKey readPrivate(String filename)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] buffer = Files.readAllBytes(Paths.get(filename));
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(buffer);
		return getKeyFactory().generatePrivate(spec);
	}

	public static PublicKey readPublic(String filename)
			throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
		byte[] buffer = Files.readAllBytes(Paths.get(filename));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(buffer);
		return getKeyFactory().generatePublic(spec);
	}

	private static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
		return KeyFactory.getInstance(PayloadSigner.ENCRYPTION_ALGORITHM);
	}
}
