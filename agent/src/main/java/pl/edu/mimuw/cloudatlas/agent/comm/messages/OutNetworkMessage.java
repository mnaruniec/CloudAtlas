package pl.edu.mimuw.cloudatlas.agent.comm.messages;

import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.Payload;

import java.net.InetAddress;

public class OutNetworkMessage extends NetworkMessage {
	public final InetAddress destAddress;

	public OutNetworkMessage(String dest, String src, InetAddress destAddress, Payload payload) {
		super(dest, src, payload);
		this.destAddress = destAddress;
	}
}
