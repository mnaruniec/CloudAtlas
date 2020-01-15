package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;

public final class DataRequestPayload extends DataPayload implements NetworkRequestPayload {
	private long t2a;
	private long t2b;

	private DataRequestPayload() {}

	public DataRequestPayload(GossipData gossipData, long t2a, long t2b) {
		super(gossipData);
		this.t2a = t2a;
		this.t2b = t2b;
	}

	public long getT2a() {
		return t2a;
	}

	public long getT2b() {
		return t2b;
	}
}
