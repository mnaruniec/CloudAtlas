package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public abstract class GossipMachineIdMessage extends Message {
	public final long machineId;

	public GossipMachineIdMessage(String dest, String src, long machineId) {
		super(dest, src);
		this.machineId = machineId;
	}
}
