package pl.edu.mimuw.cloudatlas.serialization.serializers;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.InstallationPayload;

public class SignedInstallationSerializer extends Serializer<SignedInstallation> {
	@Override
	public void write(Kryo kryo, Output output, SignedInstallation object) {
		byte[] signature = object.getSignature();
		kryo.writeObject(output, object.getPayload());
		output.writeInt(signature.length);
		output.writeBytes(signature);
	}

	@Override
	public SignedInstallation read(Kryo kryo, Input input, Class<? extends SignedInstallation> type) {
		InstallationPayload payload = kryo.readObject(input, InstallationPayload.class);
		int len = input.readInt();
		byte[] signature = input.readBytes(len);
		return new SignedInstallation(payload, signature);
	}
}
