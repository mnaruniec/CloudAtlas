package pl.edu.mimuw.cloudatlas.signing;

import pl.edu.mimuw.cloudatlas.signing.outputs.DigestGenerator;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

public final class SignatureVerifier extends DigestGenerator {
	private Cipher cipher = Cipher.getInstance(CryptoConstants.ENCRYPTION_ALGORITHM);

	public SignatureVerifier(PublicKey publicKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
	}

	public void verify(SignedObject signedObject) {
		byte[] signature = signedObject.getSignature();
		byte[] remoteDigest = decrypt(signature);

		byte[] serialized = serializePayload(signedObject.getPayload());
		byte[] localDigest = generateDigest(serialized);

		if (!Arrays.equals(remoteDigest, localDigest)) {
			throw new IllegalArgumentException("SignedObject verification failed.");
		}
	}

	private byte[] decrypt(byte[] buffer) {
		try {
			return cipher.doFinal(buffer);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Exception in Signature verifier when decrypting digest.");
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
	}
}
