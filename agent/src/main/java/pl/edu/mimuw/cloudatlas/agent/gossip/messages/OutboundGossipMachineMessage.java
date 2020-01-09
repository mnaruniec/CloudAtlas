package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public abstract class OutboundGossipMachineMessage extends Message {
	// TODO - is this class useful?
	public OutboundGossipMachineMessage(String dest, String src) {
		super(dest, src);
	}
}
