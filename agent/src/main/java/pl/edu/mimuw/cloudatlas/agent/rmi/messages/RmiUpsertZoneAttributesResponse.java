package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiUpsertZoneAttributesResponse extends RmiMessage {
	public enum ErrorType {
		NoSuchZone,
		IllegalAttribute,
	}

	public final ErrorType errorType;
	public final String attribute;

	public RmiUpsertZoneAttributesResponse(String dest, String src, long requestId,
										   ErrorType errorType, String attribute) {
		super(dest, src, requestId);
		this.errorType = errorType;
		this.attribute = attribute;
	}

	public RmiUpsertZoneAttributesResponse(RmiUpsertZoneAttributesMessage request,
										   ErrorType errorType, String attribute) {
		this(request.src, request.dest, request.requestId, errorType, attribute);
	}
}
