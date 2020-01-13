package pl.edu.mimuw.cloudatlas.signing.outputs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.serialization.KryoInitializer;
import pl.edu.mimuw.cloudatlas.signing.CryptoConstants;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.SignedObjectPayload;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestGenerator {
	private Kryo kryo = new Kryo();
	private MessageDigest digestGenerator = MessageDigest.getInstance(CryptoConstants.DIGEST_ALGORITHM);

	public DigestGenerator() throws NoSuchAlgorithmException {
		KryoInitializer.initializeKryo(kryo);
	}

	protected byte[] serializePayload(SignedObjectPayload payload) {
		Output output = new Output(512, -1);
		kryo.writeClassAndObject(output, payload);
		return output.toBytes();
	}

	protected byte[] generateDigest(byte[] buffer) {
		return digestGenerator.digest(buffer);
	}
}
