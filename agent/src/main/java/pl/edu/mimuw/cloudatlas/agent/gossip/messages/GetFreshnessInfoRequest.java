package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.model.PathName;

public class GetFreshnessInfoRequest extends GossipMachineIdMessage {
	public final PathName pathName;

	public GetFreshnessInfoRequest(String dest, String src, long machineId, PathName pathName) {
		super(dest, src, machineId);
		this.pathName = pathName;
	}
}
