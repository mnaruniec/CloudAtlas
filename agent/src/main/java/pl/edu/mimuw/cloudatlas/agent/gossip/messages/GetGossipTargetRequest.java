package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class GetGossipTargetRequest extends Message {
	public GetGossipTargetRequest(String dest, String src) {
		super(dest, src);
	}
}
