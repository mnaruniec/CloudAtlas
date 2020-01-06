package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class OutboundGossipMachineMessage extends Message {
	public OutboundGossipMachineMessage(String dest, String src) {
		super(dest, src);
	}
}
