package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

public class GetFreshnessInfoResponse extends GossipMachineIdMessage {
	public final FreshnessInfo freshnessInfo;

	public GetFreshnessInfoResponse(String dest, String src, long machineId, FreshnessInfo freshnessInfo) {
		super(dest, src, machineId);
		this.freshnessInfo = freshnessInfo;
	}

	public GetFreshnessInfoResponse(GetFreshnessInfoRequest request, FreshnessInfo freshnessInfo) {
		this(request.src, request.dest, request.machineId, freshnessInfo);
	}
}
