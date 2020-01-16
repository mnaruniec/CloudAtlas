package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class RetryGossipMessage extends GossipMachineIdMessage {
	public final int left;

	public RetryGossipMessage(String dest, String src, long machineId, int left) {
		super(dest, src, machineId);
		this.left = left;
	}
}
