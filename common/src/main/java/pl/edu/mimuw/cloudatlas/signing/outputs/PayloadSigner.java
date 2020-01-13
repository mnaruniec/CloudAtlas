package pl.edu.mimuw.cloudatlas.signing.outputs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.serialization.KryoInitializer;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.SignedObjectPayload;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

public final class PayloadSigner {
	public static final String DIGEST_ALGORITHM = "SHA-1";
	public static final String ENCRYPTION_ALGORITHM = "RSA";

	private Kryo kryo = new Kryo();
	private MessageDigest digestGenerator = MessageDigest.getInstance(DIGEST_ALGORITHM);
	private Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

	public PayloadSigner(PrivateKey privateKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		KryoInitializer.initializeKryo(kryo);
	}

	public byte[] sign(SignedObjectPayload payload) {
		byte[] serialized = serializePayload(payload);
		byte[] digest = generateDigest(serialized);
		return encrypt(digest);
	}

	private byte[] serializePayload(SignedObjectPayload payload) {
		Output output = new Output(512, -1);
		kryo.writeClassAndObject(output, payload);
		return output.toBytes();
	}

	private byte[] generateDigest(byte[] buffer) {
		return digestGenerator.digest(buffer);
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
