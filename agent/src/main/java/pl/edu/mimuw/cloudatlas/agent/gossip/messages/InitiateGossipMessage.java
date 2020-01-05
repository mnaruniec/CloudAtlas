package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class InitiateGossipMessage extends Message {
	public InitiateGossipMessage(String dest, String src) {
		super(dest, src);
	}
}
