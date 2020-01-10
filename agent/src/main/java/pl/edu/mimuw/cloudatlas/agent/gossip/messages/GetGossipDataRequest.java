package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class GetGossipDataRequest extends GossipMachineIdMessage {
	public final FreshnessInfo remoteFreshnessInfo;

	public GetGossipDataRequest(String dest, String src, long machineId, FreshnessInfo remoteFreshnessInfo) {
		super(dest, src, machineId);
		this.remoteFreshnessInfo = remoteFreshnessInfo;
	}
}
