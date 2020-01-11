package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class GetGossipDataResponse extends GossipMachineIdMessage {
	public final GossipData gossipData;

	public GetGossipDataResponse(String dest, String src, long machineId, GossipData gossipData) {
		super(dest, src, machineId);
		this.gossipData = gossipData;
	}

	public GetGossipDataResponse(GetGossipDataRequest request, GossipData gossipData) {
		this(request.src, request.dest, request.machineId, gossipData);
	}
}
