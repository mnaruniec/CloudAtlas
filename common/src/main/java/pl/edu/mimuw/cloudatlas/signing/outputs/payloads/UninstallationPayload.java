package pl.edu.mimuw.cloudatlas.signing.outputs.payloads;

import pl.edu.mimuw.cloudatlas.model.Attribute;

public final class UninstallationPayload extends SignedObjectPayload {
	private UninstallationPayload() {}

	public UninstallationPayload(long timestamp, Attribute name) {
		super(timestamp, name);
	}
}
