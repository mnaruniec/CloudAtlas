package pl.edu.mimuw.cloudatlas.agent.gossip;

import pl.edu.mimuw.cloudatlas.agent.AgentConfig;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.InNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.NetworkRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.NetworkResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.gossip.machines.GossipStateMachine;
import pl.edu.mimuw.cloudatlas.agent.gossip.machines.InboundGossipMachine;
import pl.edu.mimuw.cloudatlas.agent.gossip.machines.OutboundGossipMachine;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipMachineIdMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.InitiateGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.OutboundGossipMachineMessage;
import pl.edu.mimuw.cloudatlas.agent.timer.SetTimeoutMessage;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class GossipModule extends Module {
	private long nextMachineId = 0;

	private OutboundGossipMachine outboundMachine;
	private Map<InetAddress, InboundGossipMachine> inboundMachineMap = new HashMap<>();
	private Map<Long, GossipStateMachine> machineIdMap = new HashMap<>();

	private PathName localPathName;
	private long gossipIntervalMs;

	public GossipModule(Bus bus, AgentConfig config) {
		super(bus);
		this.localPathName = config.getPathname();
		this.gossipIntervalMs = config.getGossipIntervalMs();
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_GOSSIP_MODULE_NAME;
	}

	@Override
	public void init() {
		Runnable callback = () -> {
			bus.sendMessage(new InitiateGossipMessage(
					Constants.DEFAULT_GOSSIP_MODULE_NAME,
					Constants.DEFAULT_TIMER_MODULE_NAME
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				Constants.DEFAULT_TIMER_MODULE_NAME,
				Constants.DEFAULT_GOSSIP_MODULE_NAME,
				callback,
				gossipIntervalMs
		));
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof GossipMachineIdMessage) {
			handleGossipMachineIdMessage((GossipMachineIdMessage) message);
		} else if (message instanceof InNetworkMessage) {
			handleInNetworkMessage((InNetworkMessage) message);
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
			passToMachine(machine, message);
		}
	}

	private void handleInNetworkMessage(InNetworkMessage message) {
		if (message.payload instanceof NetworkResponsePayload) {
			passToOutboundMachine(message);
		} else if (message.payload instanceof NetworkRequestPayload) {
			passToInboundMachine(message);
		} else {
			System.out.println(
					"Gossip module received unhandled InNetworkMessage. Ignoring."
			);
		}
	}

	private void handleOutboundGossipMachineMessage(OutboundGossipMachineMessage message) {
		passToOutboundMachine(message);
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
		outboundMachine = new OutboundGossipMachine(bus, localPathName, getNextMachineId(), gossipIntervalMs);
		machineIdMap.put(outboundMachine.machineId, outboundMachine);

		bus.sendMessage(new GetGossipTargetRequest(
				Constants.DEFAULT_DATA_MODULE_NAME, getDefaultName()
		));
	}

	private void passToOutboundMachine(Message message) {
		if (outboundMachine == null) {
			System.out.println("Gossip module received message for outbound machine, but it does not exist. Ignoring.");
		} else {
			passToMachine(outboundMachine, message);
		}
	}

	private void passToInboundMachine(InNetworkMessage message) {
		InetAddress srcAddress = message.srcAddress;
		InboundGossipMachine machine = inboundMachineMap.get(srcAddress);
		if (machine == null) {
			machine = createInboundMachine(message);
		}
		passToMachine(machine, message);
	}

	private void passToMachine(GossipStateMachine machine, Message message) {
		machine.handleMessage(message);
		checkMachineFinished(machine);
	}

	private InboundGossipMachine createInboundMachine(InNetworkMessage message) {
		InboundGossipMachine machine = new InboundGossipMachine(bus, getNextMachineId(), message.srcAddress);
		machineIdMap.put(machine.getMachineId(), machine);
		inboundMachineMap.put(machine.srcAddress, machine);
		return machine;
	}

	private void checkMachineFinished(GossipStateMachine machine) {
		if (!machine.isFinished()) {
			return;
		}

		if (machine.getMachineId() == outboundMachine.getMachineId()) {
			outboundMachine = null;
		} else {
			inboundMachineMap.remove(((InboundGossipMachine) machine).srcAddress);
		}
		machineIdMap.remove(machine.getMachineId());
	}

	private long getNextMachineId() {
		return ++nextMachineId;
	}
}
