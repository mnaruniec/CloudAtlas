package pl.edu.mimuw.cloudatlas.agent.gossip.machines;

import pl.edu.mimuw.cloudatlas.agent.comm.messages.InNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.OutNetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.comm.messages.payloads.DataPayload;
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
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.UpdateWithGossipDataMessage;
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
	private GossipData localGossipData;
	private GossipData remoteGossipData;

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
		// TODO - remove debug prints
		System.out.println("in1");
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
		System.out.println("in2");
		if (state != State.ExpectLocalFreshnessInfo) {
			System.out.println("Received local freshness info in state: " + state + ". Ignoring.");
			return;
		}

		localFreshnessInfo = response.freshnessInfo;
		if (localFreshnessInfo == null) {
			System.out.println("Received null as local freshness info. Finishing gossip.");
			finish();
		} else {
			System.out.println("InLocal ZMI freshness: " + localFreshnessInfo.getZmiTimestamps());
			System.out.println("InRemote ZMI freshness: " + remoteFreshnessInfo.getZmiTimestamps());

			System.out.println("InLocal query freshness: " + localFreshnessInfo.getQueryTimestamps());
			System.out.println("InRemote query freshness: " + remoteFreshnessInfo.getQueryTimestamps());

			state = State.ExpectRemoteData;
			bus.sendMessage(createNetworkMessage(
					new FreshnessInfoResponsePayload(localFreshnessInfo)
			));
		}
	}

	private void handleNetworkDataRequest(InNetworkMessage message) {
		System.out.println("in3");

		if (state != State.ExpectRemoteData) {
			System.out.println("Received remote data in state: " + state + ". Ignoring.");
			return;
		}

		remoteGossipData = ((DataPayload) message.payload).getGossipData();
		if (remoteGossipData == null) {
			System.out.println("Received null as remote gossip data. Finishing gossip.");
			finish();
		} else {
			state = State.ExpectLocalData;
			bus.sendMessage(new GetGossipDataRequest(
					Constants.DEFAULT_DATA_MODULE_NAME,
					Constants.DEFAULT_GOSSIP_MODULE_NAME,
					getMachineId(),
					srcPathName,
					remoteFreshnessInfo
			));
		}
	}

	private void handleGetGossipDataResponse(GetGossipDataResponse response) {
		System.out.println("in4");

		if (state != State.ExpectLocalData) {
			System.out.println("Received local data in state: " + state + ". Ignoring.");
			return;
		}

		bus.sendMessage(new UpdateWithGossipDataMessage(
				Constants.DEFAULT_DATA_MODULE_NAME,
				Constants.DEFAULT_GOSSIP_MODULE_NAME,
				remoteGossipData
		));

		localGossipData = response.gossipData;
		if (localGossipData == null) {
			System.out.println("Received null as local gossip data. Finishing gossip.");
			finish();
		} else {
			System.out.println("InLocal ZMI data: " + localGossipData.getZmiMap());
			System.out.println("InRemote ZMI data: " + remoteGossipData.getZmiMap());

			System.out.println("InLocal query data: " + localGossipData.getQueryList());
			System.out.println("InRemote query data: " + remoteGossipData.getQueryList());

			finish();
			bus.sendMessage(createNetworkMessage(
					new DataResponsePayload(localGossipData)
			));
		}
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
