package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;

public final class FreshnessInfoRequestPayload extends FreshnessInfoPayload implements NetworkRequestPayload {
	private FreshnessInfoRequestPayload() {}

	public FreshnessInfoRequestPayload(FreshnessInfo freshnessInfo) {
		super(freshnessInfo);
	}
}
