package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class GetFreshnessInfoResponse extends GossipMachineIdMessage {
	public GetFreshnessInfoResponse(String dest, String src, long machineId) {
		super(dest, src, machineId);
	}
}
