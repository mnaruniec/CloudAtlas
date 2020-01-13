package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;

public class RmiUninstallQueryRequest extends RmiMessage {
	public final SignedUninstallation signedUninstallation;

	public RmiUninstallQueryRequest(String dest, String src, long requestId,
									SignedUninstallation signedUninstallation) {
		super(dest, src, requestId);
		this.signedUninstallation = signedUninstallation;
	}
}
