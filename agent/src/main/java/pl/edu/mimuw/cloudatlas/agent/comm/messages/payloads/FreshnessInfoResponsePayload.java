package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;

public final class FreshnessInfoResponsePayload extends FreshnessInfoPayload implements NetworkResponsePayload {
	private long t1b;
	private long t1a;

	private FreshnessInfoResponsePayload() {}

	public FreshnessInfoResponsePayload(FreshnessInfo freshnessInfo, long t1b, long t1a) {
		super(freshnessInfo);
		this.t1b = t1b;
		this.t1a = t1a;
	}

	public long getT1b() {
		return t1b;
	}

	public long getT1a() {
		return t1a;
	}
}
