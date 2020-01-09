package pl.edu.mimuw.cloudatlas.agent.gossip.machines;

import pl.edu.mimuw.cloudatlas.agent.comm.messages.InNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.OutNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.NetworkRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.Payload;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetResponse;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

public class OutboundGossipMachine implements GossipStateMachine {
	private enum State {
		ExpectGossipTarget,
		ExpectLocalFreshnessInfo,
		ExpectRemoteFreshnessInfo,
		ExpectLocalData,
		ExpectRemoteData,
		Finished
	}

	private State state = State.ExpectGossipTarget;
	private ValueContact target;
	private FreshnessInfo localFreshnessInfo;

	public final long machineId;
	private Bus bus;

	public OutboundGossipMachine(Bus bus, long machineId) {
		this.bus = bus;
		this.machineId = machineId;
	}

	@Override
	public boolean isFinished() {
		return state == State.Finished;
	}

	@Override
	public long getMachineId() {
		return machineId;
	}

	@Override
	public void handleMessage(Message message) {
		try {
			if (message instanceof GetGossipTargetResponse) {
				handleGetGossipTargetResponse((GetGossipTargetResponse) message);
			} else if (message instanceof GetFreshnessInfoResponse) {
				handleGetFreshnessInfoResponse((GetFreshnessInfoResponse) message);
			} else if (message instanceof InNetworkMessage) {
				handleInNetworkMessage((InNetworkMessage) message);
			} else {
				System.out.println("Outbound state machine got unhandled message type. Ignoring.");
			}
		} catch (Exception e) {
			System.out.println("Unexpected error thrown in OutboundGossipMachine. Finishing gossip.");
			e.printStackTrace();
			finish();
		}
	}

	private void handleInNetworkMessage(InNetworkMessage message) {
		if (message.payload instanceof FreshnessInfoResponsePayload) {
			handleNetworkFreshnessInfoResponse(message);
		} else if (message.payload instanceof DataResponsePayload) {
			handleNetworkDataResponse(message);
		} else {
			System.out.println("OutboundGossipMachine received network message with unhandled payload type. Finishing.");
			finish();
		}
	}

	private void handleGetGossipTargetResponse(GetGossipTargetResponse response) {
		if (state != State.ExpectGossipTarget) {
			// TODO - consider finishing in these checks
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

	private void handleGetFreshnessInfoResponse(GetFreshnessInfoResponse response) {
		if (state != State.ExpectLocalFreshnessInfo) {
			System.out.println("Received local freshness info in state: " + state + ". Ignoring.");
			return;
		}

		localFreshnessInfo = response.freshnessInfo;
		if (localFreshnessInfo == null) {
			System.out.println("Received null as local freshness info. Finishing gossip.");
			finish();
		} else {
			// TODO - schedule resending packets

			state = State.ExpectRemoteFreshnessInfo;
			bus.sendMessage(createNetworkMessage(
					new FreshnessInfoRequestPayload(localFreshnessInfo)
			));
		}
	}

	private void handleNetworkFreshnessInfoResponse(InNetworkMessage message) {
		if (state != State.ExpectRemoteFreshnessInfo) {
			System.out.println("Received remote freshness info in state: " + state + ". Ignoring.");
			return;
		}

		// TODO
	}

	private void handleGetGossipDataResponse(GetGossipDataResponse response) {
		if (state != State.ExpectLocalData) {
			System.out.println("Received local gossip data in state: " + state + ". Ignoring.");
			return;
		}

		// TODO
	}

	private void handleNetworkDataResponse(InNetworkMessage message) {
		if (state != State.ExpectRemoteData) {
			System.out.println("Received remote gossip data in state: " + state + ". Ignoring.");
			return;
		}

		// TODO
	}

	private OutNetworkMessage createNetworkMessage(Payload payload) {
		return new OutNetworkMessage(
				Constants.DEFAULT_COMM_MODULE_NAME,
				Constants.DEFAULT_GOSSIP_MODULE_NAME,
				target.getAddress(),
				payload
		);
	}

	private void finish() {
		if (state != State.Finished) {
			state = State.Finished;
			// TODO - reschedule gossip
		}
	}
}
