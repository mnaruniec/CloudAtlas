package pl.edu.mimuw.cloudatlas.agent.rmi.messages;

public class RmiGetStoredZonesRequest extends RmiMessage {
	public RmiGetStoredZonesRequest(String dest, String src, long requestId) {
		super(dest, src, requestId);
	}
}
