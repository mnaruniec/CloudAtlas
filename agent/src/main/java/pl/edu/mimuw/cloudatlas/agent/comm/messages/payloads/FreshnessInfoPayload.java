package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;

public abstract class FreshnessInfoPayload extends Payload {
	private FreshnessInfo freshnessInfo;

	protected FreshnessInfoPayload() {}

	public FreshnessInfoPayload(FreshnessInfo freshnessInfo) {
		this.freshnessInfo = freshnessInfo;
	}

	public FreshnessInfo getFreshnessInfo() {
		return freshnessInfo;
	}
}
