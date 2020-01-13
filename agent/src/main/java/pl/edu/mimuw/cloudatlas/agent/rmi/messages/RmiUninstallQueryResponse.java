package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiUninstallQueryResponse extends RmiResponse {
	public RmiUninstallQueryResponse(String dest, String src, long requestId) {
		super(dest, src, requestId);
	}

	public RmiUninstallQueryResponse(RmiMessage request) {
		super(request);
	}
}
