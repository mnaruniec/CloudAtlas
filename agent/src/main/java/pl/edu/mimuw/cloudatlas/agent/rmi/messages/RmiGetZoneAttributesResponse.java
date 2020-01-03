package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

import pl.edu.mimuw.cloudatlas.model.Value;

import java.util.Map;

public class RmiGetZoneAttributesResponse extends RmiResponse {
	public final Map<String, Value> attributes;

	public RmiGetZoneAttributesResponse(String dest, String src, long requestId,
										Map<String, Value> attributes
	) {
		super(dest, src, requestId);
		this.attributes = attributes;
	}

	public RmiGetZoneAttributesResponse(RmiGetZoneAttributesRequest request,
										Map<String, Value> attributes) {
		this(request.src, request.dest, request.requestId, attributes);
	}
}
