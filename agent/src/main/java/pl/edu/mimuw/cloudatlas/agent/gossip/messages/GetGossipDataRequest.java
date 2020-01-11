package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.model.PathName;

public class GetGossipDataRequest extends GossipMachineIdMessage {
	public final PathName pathName;
	public final FreshnessInfo remoteFreshnessInfo;

	public GetGossipDataRequest(String dest, String src, long machineId,
								PathName pathName, FreshnessInfo remoteFreshnessInfo) {
		super(dest, src, machineId);
		this.pathName = pathName;
		this.remoteFreshnessInfo = remoteFreshnessInfo;
	}
}
