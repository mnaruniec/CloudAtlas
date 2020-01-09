package pl.edu.mimuw.cloudatlas.agent.gossip.machines;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Message;

import java.net.InetAddress;

public class InboundGossipMachine implements GossipStateMachine {
	public final long machineId;
	private Bus bus;

	public final InetAddress srcAddress;

	public InboundGossipMachine(Bus bus, long machineId, InetAddress srcAddress) {
		this.bus = bus;
		this.machineId = machineId;
		this.srcAddress = srcAddress;
	}

	@Override
	public void handleMessage(Message message) {
		// TODO
	}

	@Override
	public boolean isFinished() {
		return false;
	}

	@Override
	public long getMachineId() {
		return machineId;
	}
}
