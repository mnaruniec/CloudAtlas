package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

import pl.edu.mimuw.cloudatlas.model.Value;

import java.util.Map;

public class RmiUpsertZoneAttributesRequest extends RmiMessage {
	public final String zone;
	public final Map<String, Value> attributes;

	public RmiUpsertZoneAttributesRequest(String dest, String src, long requestId, String zone, Map<String, Value> attributes) {
		super(dest, src, requestId);
		this.zone = zone;
		this.attributes = attributes;
	}
}
