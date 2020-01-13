package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;

public class RmiInstallQueryRequest extends RmiMessage {
	public final SignedInstallation signedInstallation;

	public RmiInstallQueryRequest(String dest, String src, long requestId,
								  SignedInstallation signedInstallation) {
		super(dest, src, requestId);
		this.signedInstallation = signedInstallation;
	}
}
