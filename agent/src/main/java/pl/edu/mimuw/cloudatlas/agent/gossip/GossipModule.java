package pl.edu.mimuw.cloudatlas.agent.gossip;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.gossip.machines.GossipStateMachine;
import pl.edu.mimuw.cloudatlas.agent.gossip.machines.OutboundGossipMachine;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipMachineIdMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.InitiateGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.OutboundGossipMachineMessage;

import java.util.HashMap;
import java.util.Map;

public class GossipModule extends Module {
	private long nextMachineId = 0;

	private OutboundGossipMachine outboundMachine;
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
		} else if (message instanceof OutboundGossipMachineMessage) {
			handleOutboundGossipMachineMessage((OutboundGossipMachineMessage) message);
		} else if (message instanceof InitiateGossipMessage) {
			handleInitiateGossipMessage((InitiateGossipMessage) message);
		} else {
			System.out.println("Received unexpected type of message in gossip module. Ignoring");
		}
	}

	private void handleGossipMachineIdMessage(GossipMachineIdMessage message) {
		long machineId = message.machineId;
		GossipStateMachine machine = machineIdMap.get(machineId);
		if (machine == null) {
			System.out.println(
					"Gossip module received message for non-existing state machine id: "
					+ machineId + ". Ignoring."
			);
		} else {
			machine.handleMessage(message);
			checkMachineFinished(machine);
		}
	}

	private void handleOutboundGossipMachineMessage(OutboundGossipMachineMessage message) {
		if (outboundMachine == null) {
			System.out.println("Gossip module received message for outbound machine, but it does not exist.");
		} else {
			outboundMachine.handleMessage(message);
			checkMachineFinished(outboundMachine);
		}
	}

	private void handleInitiateGossipMessage(InitiateGossipMessage message) {
		if (outboundMachine != null) {
			if (outboundMachine.isFinished()) {
				machineIdMap.remove(outboundMachine.machineId);
			} else {
				System.out.println(
					"Gossip module received InitiateGossipMessage with outbound gossip already existing. Ignoring."
				);
				return;
			}
		}
		outboundMachine = new OutboundGossipMachine(bus, getNextMachineId());
		machineIdMap.put(outboundMachine.machineId, outboundMachine);

		bus.sendMessage(new GetGossipTargetRequest(
				Constants.DEFAULT_DATA_MODULE_NAME, getDefaultName()
		));
	}

	private void checkMachineFinished(GossipStateMachine machine) {
		if (!machine.isFinished()) {
			return;
		}

		if (machine.getMachineId() == outboundMachine.getMachineId()) {
			outboundMachine = null;
		}
		machineIdMap.remove(machine.getMachineId());
	}

	private long getNextMachineId() {
		return ++nextMachineId;
	}
}
