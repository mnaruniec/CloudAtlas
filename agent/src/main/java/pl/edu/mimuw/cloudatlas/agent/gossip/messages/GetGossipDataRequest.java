package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class GetGossipDataRequest extends GossipMachineIdMessage {
	public GetGossipDataRequest(String dest, String src, long machineId) {
		super(dest, src, machineId);
	}
}
