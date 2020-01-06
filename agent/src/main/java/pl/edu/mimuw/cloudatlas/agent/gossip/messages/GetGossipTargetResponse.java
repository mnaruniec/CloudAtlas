package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class GetGossipTargetResponse extends OutboundGossipMachineMessage {
	public final ValueContact contact;

	public GetGossipTargetResponse(String dest, String src, ValueContact contact) {
		super(dest, src);
		this.contact = contact;
	}

	public GetGossipTargetResponse(GetGossipTargetRequest request, ValueContact contact) {
		this(request.src, request.dest, contact);
	}
}
