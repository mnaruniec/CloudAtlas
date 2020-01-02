package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiGetZoneAttributesRequest extends RmiMessage {
	public final String zone;

	public RmiGetZoneAttributesRequest(String dest, String src, long requestId, String zone) {
		super(dest, src, requestId);
		this.zone = zone;
	}
}
