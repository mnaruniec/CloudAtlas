package pl.edu.mimuw.cloudatlas.agent.comm.messages;

import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.Payload;

import java.net.InetAddress;

public class InNetworkMessage extends NetworkMessage {
	public final InetAddress srcAddress;
	public final long receiveTimestamp;
	public final long sendTimestamp;

	public InNetworkMessage(String dest, String src, InetAddress srcAddress,
							Payload payload, long receiveTimestamp, long sendTimestamp) {
		super(dest, src, payload);
		this.srcAddress = srcAddress;
		this.receiveTimestamp = receiveTimestamp;
		this.sendTimestamp = sendTimestamp;
	}
}
