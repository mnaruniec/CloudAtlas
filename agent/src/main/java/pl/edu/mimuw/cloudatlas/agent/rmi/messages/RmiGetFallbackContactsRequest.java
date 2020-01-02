package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiGetFallbackContactsRequest extends RmiMessage {
	public RmiGetFallbackContactsRequest(String dest, String src, long requestId) {
		super(dest, src, requestId);
	}
}
