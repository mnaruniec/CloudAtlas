package pl.edu.mimuw.cloudatlas.agent.comm.messages;

import java.net.InetAddress;

public class InNetworkMessage extends NetworkMessage {
	public final InetAddress srcAddress;

	public InNetworkMessage(String dest, String src, InetAddress srcAddress, Payload payload) {
		super(dest, src, payload);
		this.srcAddress = srcAddress;
	}
}
