package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;

public final class FreshnessInfoResponsePayload extends FreshnessInfoPayload implements NetworkResponsePayload {
	private FreshnessInfoResponsePayload() {}

	public FreshnessInfoResponsePayload(FreshnessInfo freshnessInfo) {
		super(freshnessInfo);
	}
}
