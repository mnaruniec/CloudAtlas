package pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads;

import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;

public abstract class DataPayload extends Payload {
	private GossipData gossipData;

	protected DataPayload() {}

	public DataPayload(GossipData gossipData) {
		this.gossipData = gossipData;
	}

	public GossipData getGossipData() {
		return gossipData;
	}
}
