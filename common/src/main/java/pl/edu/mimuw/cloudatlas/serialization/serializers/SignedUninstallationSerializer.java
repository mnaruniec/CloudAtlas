package pl.edu.mimuw.cloudatlas.serialization.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.UninstallationPayload;

public class SignedUninstallationSerializer extends Serializer<SignedUninstallation> {
	@Override
	public void write(Kryo kryo, Output output, SignedUninstallation object) {
		byte[] signature = object.getSignature();
		kryo.writeObject(output, object.getPayload());
		output.writeInt(signature.length);
		output.writeBytes(signature);
	}

	@Override
	public SignedUninstallation read(Kryo kryo, Input input, Class<? extends SignedUninstallation> type) {
		UninstallationPayload payload = kryo.readObject(input, UninstallationPayload.class);
		int len = input.readInt();
		byte[] signature = input.readBytes(len);
		return new SignedUninstallation(payload, signature);
	}
}
