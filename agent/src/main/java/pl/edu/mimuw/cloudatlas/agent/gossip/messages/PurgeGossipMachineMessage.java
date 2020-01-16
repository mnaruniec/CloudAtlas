package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class PurgeGossipMachineMessage extends GossipMachineIdMessage {
	public PurgeGossipMachineMessage(String dest, String src, long machineId) {
		super(dest, src, machineId);
	}
}
