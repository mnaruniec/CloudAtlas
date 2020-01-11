package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;

public final class DataResponsePayload extends DataPayload implements NetworkResponsePayload {
	private DataResponsePayload() {}

	public DataResponsePayload(GossipData gossipData) {
		super(gossipData);
	}
}
