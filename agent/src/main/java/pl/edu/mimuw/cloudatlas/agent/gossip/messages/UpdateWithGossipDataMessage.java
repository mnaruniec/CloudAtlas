package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class UpdateWithGossipDataMessage extends Message {
	public final GossipData gossipData;

	public UpdateWithGossipDataMessage(String dest, String src, GossipData gossipData) {
		super(dest, src);
		this.gossipData = gossipData;
	}
}
