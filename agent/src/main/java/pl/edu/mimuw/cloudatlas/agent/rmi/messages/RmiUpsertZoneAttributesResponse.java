package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiUpsertZoneAttributesResponse extends RmiResponse {
	public RmiUpsertZoneAttributesResponse(String dest, String src, long requestId) {
		super(dest, src, requestId);
	}

	public RmiUpsertZoneAttributesResponse(RmiMessage request) {
		super(request);
	}
}
