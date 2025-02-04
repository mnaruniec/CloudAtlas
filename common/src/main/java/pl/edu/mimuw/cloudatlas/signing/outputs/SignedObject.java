package pl.edu.mimuw.cloudatlas.signing.outputs;

import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.SignedObjectPayload;

import java.io.Serializable;

public abstract class SignedObject<T extends SignedObjectPayload> implements Serializable {
	private T payload;
	private byte[] signature;

	protected SignedObject() {}

	public SignedObject(T payload, byte[] signature) {
		this.payload = payload;
		this.signature = signature.clone();
	}

	public byte[] getSignature() {
		return signature.clone();
	}

	public T getPayload() {
		return payload;
	}

	public long getTimestamp() {
		return payload.getTimestamp();
	}
}
