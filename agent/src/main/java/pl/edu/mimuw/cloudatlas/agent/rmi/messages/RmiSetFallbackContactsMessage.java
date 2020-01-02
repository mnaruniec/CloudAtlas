package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.util.Set;

public class RmiSetFallbackContactsMessage extends RmiMessage {
	public final Set<ValueContact> fallbackContacts;

	public RmiSetFallbackContactsMessage(String dest, String src, long requestId, Set<ValueContact> contacts) {
		super(dest, src, requestId);
		this.fallbackContacts = contacts;
	}
}
