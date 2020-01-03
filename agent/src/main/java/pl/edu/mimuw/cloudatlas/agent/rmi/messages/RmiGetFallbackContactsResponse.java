package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.util.Set;

public class RmiGetFallbackContactsResponse extends RmiResponse {
	public final Set<ValueContact> fallbackContacts;

	public RmiGetFallbackContactsResponse(String dest, String src, long requestId, Set<ValueContact> contacts) {
		super(dest, src, requestId);
		this.fallbackContacts = contacts;
	}

	public RmiGetFallbackContactsResponse(RmiGetFallbackContactsRequest request, Set<ValueContact> contacts) {
		this(request.src, request.dest, request.requestId, contacts);
	}
}
