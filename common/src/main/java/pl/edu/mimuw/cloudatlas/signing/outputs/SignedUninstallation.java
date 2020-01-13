package pl.edu.mimuw.cloudatlas.signing.outputs;

import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.UninstallationPayload;

public final class SignedUninstallation extends SignedObject<UninstallationPayload> {
	private SignedUninstallation() {}

	public SignedUninstallation(UninstallationPayload payload, byte[] signature) {
		super(payload, signature);
	}
}
