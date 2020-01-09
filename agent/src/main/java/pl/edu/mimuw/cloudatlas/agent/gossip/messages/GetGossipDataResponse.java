package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class GetGossipDataResponse extends GossipMachineIdMessage {
	public GetGossipDataResponse(String dest, String src, long machineId) {
		super(dest, src, machineId);
	}

	public GetGossipDataResponse(GetGossipDataRequest request) {
		this(request.src, request.dest, request.machineId);
	}
}
