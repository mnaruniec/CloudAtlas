package pl.edu.mimuw.cloudatlas.signing;

import pl.edu.mimuw.cloudatlas.signing.outputs.DigestGenerator;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.SignedObjectPayload;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public final class PayloadSigner extends DigestGenerator {
	private Cipher cipher = Cipher.getInstance(CryptoConstants.ENCRYPTION_ALGORITHM);

	public PayloadSigner(PrivateKey privateKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
	}

	public byte[] sign(SignedObjectPayload payload) {
		byte[] serialized = serializePayload(payload);
		byte[] digest = generateDigest(serialized);
		return encrypt(digest);
	}

	private byte[] encrypt(byte[] buffer) {
		try {
			return cipher.doFinal(buffer);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Exception in PayloadSigner when encrypting digest.");
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}
}
