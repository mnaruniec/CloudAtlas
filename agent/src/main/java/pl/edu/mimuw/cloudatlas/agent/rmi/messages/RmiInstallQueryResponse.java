package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiInstallQueryResponse extends RmiResponse {
	public RmiInstallQueryResponse(String dest, String src, long requestId) {
		super(dest, src, requestId);
	}

	public RmiInstallQueryResponse(RmiMessage request) {
		super(request);
	}
}
