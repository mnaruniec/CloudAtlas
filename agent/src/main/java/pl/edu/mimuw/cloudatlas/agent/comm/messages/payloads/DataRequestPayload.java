package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;

public final class DataRequestPayload extends DataPayload implements NetworkRequestPayload {
	private DataRequestPayload() {}

	public DataRequestPayload(GossipData gossipData) {
		super(gossipData);
	}
}
