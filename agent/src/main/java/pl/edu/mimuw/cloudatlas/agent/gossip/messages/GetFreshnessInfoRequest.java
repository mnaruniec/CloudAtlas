package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class GetFreshnessInfoRequest extends GossipMachineIdMessage {
	public GetFreshnessInfoRequest(String dest, String src, long machineId) {
		super(dest, src, machineId);
	}
}
