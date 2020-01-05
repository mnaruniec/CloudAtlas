package pl.edu.mimuw.cloudatlas.agent.gossip;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.gossip.machines.GossipStateMachine;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipMachineIdMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.InitiateGossipMessage;

import java.util.HashMap;
import java.util.Map;

public class GossipModule extends Module {
	private long nextMachineId = 0;

	private Map<Long, GossipStateMachine> machineIdMap = new HashMap<>();

	public GossipModule(Bus bus) {
		super(bus);
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_GOSSIP_MODULE_NAME;
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof GossipMachineIdMessage) {
			handleGossipMachineIdMessage((GossipMachineIdMessage) message);
		} else if (message instanceof InitiateGossipMessage) {
			handleInitiateGossipMessage((InitiateGossipMessage) message);
		} else if (message instanceof GetGossipTargetResponse) {
			handleGetGossipTargetResponse((GetGossipTargetResponse) message);
		} else {
			System.out.println("Received unexpected type of message in gossip module. Ignoring");
		}
	}

	public void handleGossipMachineIdMessage(GossipMachineIdMessage message) {
		long machineId = message.machineId;
		GossipStateMachine machine = machineIdMap.get(machineId);
		if (machine == null) {
			System.out.println(
					"Gossip module received message for non-existing state machine id: "
					+ machineId + ". Ignoring."
			);
		} else {
			machine.handleMessage(message);
		}
	}

	private void handleInitiateGossipMessage(InitiateGossipMessage message) {
		// create state machine
		// pick a node to gossip
	}

	private void handleGetGossipTargetResponse(GetGossipTargetResponse response) {
		// set target or finish if null
		// ask for freshness info
	}
}
