package pl.edu.mimuw.cloudatlas.agent.comm.messages;

import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.Payload;
import pl.edu.mimuw.cloudatlas.agent.common.Message;

public abstract class NetworkMessage extends Message {
	public final Payload payload;

	public NetworkMessage(String dest, String src, Payload payload) {
		super(dest, src);
		this.payload = payload;
	}
}
