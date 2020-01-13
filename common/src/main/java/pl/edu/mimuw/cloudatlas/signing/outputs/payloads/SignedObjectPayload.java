package pl.edu.mimuw.cloudatlas.signing.outputs.payloads;

import pl.edu.mimuw.cloudatlas.model.Attribute;

import java.io.Serializable;

public abstract class SignedObjectPayload implements Serializable {
	private long timestamp;
	private Attribute name;

	protected SignedObjectPayload() {}

	public SignedObjectPayload(long timestamp, Attribute name) {
		this.timestamp = timestamp;
		this.name = name;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Attribute getName() {
		return name;
	}
}
