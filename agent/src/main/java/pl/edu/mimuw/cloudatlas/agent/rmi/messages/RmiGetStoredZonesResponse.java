package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

import java.util.Set;

public class RmiGetStoredZonesResponse extends RmiResponse {
	public final Set<String> zones;

	public RmiGetStoredZonesResponse(String dest, String src, long requestId, Set<String> zones) {
		super(dest, src, requestId);
		this.zones = zones;
	}
	public RmiGetStoredZonesResponse(RmiGetStoredZonesRequest request, Set<String> zones) {
		this(request.src, request.dest, request.requestId, zones);
	}
}
