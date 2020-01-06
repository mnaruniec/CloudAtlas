package pl.edu.mimuw.cloudatlas.agent.gossip.machines;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetResponse;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class OutboundGossipMachine implements GossipStateMachine {
	private enum State {
		ExpectGossipTarget,
		ExpectLocalFreshnessInfo,
		ExpectRemoteFreshnessInfo,
		ExpectOwnData,
		ExpectRemoteData,
		Finished
	}

	private State state = State.ExpectGossipTarget;
	private ValueContact target;

	public final long machineId;
	private Bus bus;

	public OutboundGossipMachine(Bus bus, long machineId) {
		this.bus = bus;
		this.machineId = machineId;
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof GetGossipTargetResponse) {
			handleGetGossipTargetResponse((GetGossipTargetResponse) message);
		} else {
			System.out.println("Outbound state machine got unhandled message type. Ignoring.");
		}
	}

	@Override
	public boolean isFinished() {
		return state == State.Finished;
	}

	@Override
	public long getMachineId() {
		return machineId;
	}

	private void handleGetGossipTargetResponse(GetGossipTargetResponse response) {
		if (state != State.ExpectGossipTarget) {
			System.out.println("Received gossip target in state: " + state + ". Ignoring.");
			return;
		}

		if (response.contact == null) {
			System.out.println("Received null contact as gossip target. Finishing gossip.");
			finish();
		} else {
			target = response.contact;
			state = State.ExpectLocalFreshnessInfo;
			bus.sendMessage(new GetFreshnessInfoRequest(
					Constants.DEFAULT_DATA_MODULE_NAME,
					Constants.DEFAULT_GOSSIP_MODULE_NAME,
					machineId,
					target.getName()
			));
		}
	}

	private void finish() {
		state = State.Finished;
		// TODO - reschedule gossip
	}
}
