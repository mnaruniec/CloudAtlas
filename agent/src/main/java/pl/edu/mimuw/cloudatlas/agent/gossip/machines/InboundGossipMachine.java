package pl.edu.mimuw.cloudatlas.agent.gossip.machines;

import pl.edu.mimuw.cloudatlas.agent.comm.messages.InNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.OutNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoRequestPayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.FreshnessInfoResponsePayload;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.Payload;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataResponse;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.net.InetAddress;

public class InboundGossipMachine implements GossipStateMachine {
	private enum State {
		ExpectRemoteFreshnessInfo,
		ExpectLocalFreshnessInfo,
		ExpectRemoteData,
		ExpectLocalData,
		Finished
	}

	private State state = State.ExpectRemoteFreshnessInfo;

	private FreshnessInfo localFreshnessInfo;
	private FreshnessInfo remoteFreshnessInfo;

	public final long machineId;
	private Bus bus;

	public final InetAddress srcAddress;
	private PathName srcPathName;

	public InboundGossipMachine(Bus bus, long machineId, InetAddress srcAddress) {
		this.bus = bus;
		this.machineId = machineId;
		this.srcAddress = srcAddress;
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
			if (message instanceof GetFreshnessInfoResponse) {
				handleGetFreshnessInfoResponse((GetFreshnessInfoResponse) message);
			} else if (message instanceof GetGossipDataResponse) {
				handleGetGossipDataResponse((GetGossipDataResponse) message);
			} else if (message instanceof InNetworkMessage) {
				handleInNetworkMessage((InNetworkMessage) message);
			} else {
				System.out.println("Inbound state machine got unhandled message type. Ignoring.");
			}
		} catch (Exception e) {
			System.out.println("Unexpected error thrown in InboundGossipMachine. Finishing gossip.");
			e.printStackTrace();
			finish();
		}
	}

	private void handleInNetworkMessage(InNetworkMessage message) {
		if (message.payload instanceof FreshnessInfoRequestPayload) {
			handleNetworkFreshnessInfoRequest(message);
		} else if (message.payload instanceof DataRequestPayload) {
			handleNetworkDataRequest(message);
		} else {
			System.out.println("InboundGossipMachine received network message with unhandled payload type. Finishing.");
			finish();
		}
	}

	private void handleNetworkFreshnessInfoRequest(InNetworkMessage message) {
		if (state != State.ExpectRemoteFreshnessInfo) {
			System.out.println("Received remote freshness info in state: " + state + ". Ignoring.");
			return;
		}

		remoteFreshnessInfo = ((FreshnessInfoPayload) message.payload).getFreshnessInfo();
		String pathNameStr = ((FreshnessInfoRequestPayload) message.payload).getPathName();
		try {
			srcPathName = new PathName(pathNameStr);
		} catch (Exception e) {
			System.out.println("Received incorrect initiator pathName '" + srcPathName + "'. Finishing.");
			finish();
			return;
		}

		if (remoteFreshnessInfo == null) {
			System.out.println("Received null as remote freshness info. Finishing gossip.");
			finish();
		} else {
			state = State.ExpectLocalFreshnessInfo;
			bus.sendMessage(new GetFreshnessInfoRequest(
					Constants.DEFAULT_DATA_MODULE_NAME,
					Constants.DEFAULT_GOSSIP_MODULE_NAME,
					getMachineId(),
					srcPathName
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
			// TODO - consider resending packets

			state = State.ExpectRemoteData;
			bus.sendMessage(createNetworkMessage(
					new FreshnessInfoResponsePayload(localFreshnessInfo)
			));
		}
	}

	private void handleNetworkDataRequest(InNetworkMessage message) {
		// TODO
	}

	private void handleGetGossipDataResponse(GetGossipDataResponse response) {
		// TODO
	}


	private OutNetworkMessage createNetworkMessage(Payload payload) {
		return new OutNetworkMessage(
				Constants.DEFAULT_COMM_MODULE_NAME,
				Constants.DEFAULT_GOSSIP_MODULE_NAME,
				srcAddress,
				payload
		);
	}

	private void finish() {
		state = State.Finished;
	}
}
