package pl.edu.mimuw.cloudatlas.signing.outputs;

import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.InstallationPayload;

public final class SignedInstallation extends SignedObject<InstallationPayload> {
	private SignedInstallation() {}

	public SignedInstallation(InstallationPayload payload, byte[] signature) {
		super(payload, signature);
	}
}
