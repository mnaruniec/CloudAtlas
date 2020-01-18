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
import pl.edu.mimuw.cloudatlas.agent.common.ModuleNames;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.PurgeGossipMachineMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.UpdateWithGossipDataMessage;
import pl.edu.mimuw.cloudatlas.agent.timer.SetTimeoutMessage;
import pl.edu.mimuw.cloudatlas.gtp.GtpUtils;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

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
	private long t3b, t3a, t2a, t2b, t1b, t1a;  // GTP timestamps
	private long rtd;
	// says how much remote clock is ahead of local clock
	private long dT;

	public final long machineId;
	private Bus bus;

	public final InetAddress srcAddress;
	private PathName srcPathName;
	private ValueContact srcContact;

	private long purgeTimeoutMs;

	public InboundGossipMachine(Bus bus, long machineId, InetAddress srcAddress, long purgeTimeoutMs) {
		this.bus = bus;
		this.machineId = machineId;
		this.srcAddress = srcAddress;
		this.purgeTimeoutMs = purgeTimeoutMs;
		schedulePurge();
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
			} else if (message instanceof PurgeGossipMachineMessage) {
				handlePurgeGossipMachineMessage((PurgeGossipMachineMessage) message);
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

		srcContact = new ValueContact(srcPathName, srcAddress);
		System.out.println("Starting inbound gossip with contact: " + srcContact);

		if (remoteFreshnessInfo == null) {
			System.out.println("Received null as remote freshness info. Finishing gossip.");
			finish();
		} else {
			t1b = message.receiveTimestamp;
			t1a = message.sendTimestamp;

			state = State.ExpectLocalFreshnessInfo;
			bus.sendMessage(new GetFreshnessInfoRequest(
					ModuleNames.DATA_MODULE_NAME,
					ModuleNames.GOSSIP_MODULE_NAME,
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
			state = State.ExpectRemoteData;
			bus.sendMessage(createNetworkMessage(
					new FreshnessInfoResponsePayload(
							localFreshnessInfo,
							t1b,
							t1a
					)
			));
		}
	}

	private void handleNetworkDataRequest(InNetworkMessage message) {
		if (state != State.ExpectRemoteData) {
			System.out.println("Received remote data in state: " + state + ". Ignoring.");
			return;
		}

		remoteGossipData = ((DataPayload) message.payload).getGossipData();
		if (remoteGossipData == null) {
			System.out.println("Received null as remote gossip data. Finishing gossip.");
			finish();
		} else {
			t3b = message.receiveTimestamp;
			t3a = message.sendTimestamp;
			t2a = ((DataRequestPayload) message.payload).getT2a();
			t2b = ((DataRequestPayload) message.payload).getT2b();
			rtd = GtpUtils.getRoundTripDelay(t3b, t3a, t2a, t2b);
			dT = GtpUtils.getTimeOffset(t3b, t3a, rtd);

			remoteFreshnessInfo.adjustRemoteTimestamps(dT);

			state = State.ExpectLocalData;
			bus.sendMessage(new GetGossipDataRequest(
					ModuleNames.DATA_MODULE_NAME,
					ModuleNames.GOSSIP_MODULE_NAME,
					getMachineId(),
					srcPathName,
					remoteFreshnessInfo
			));
		}
	}

	private void handleGetGossipDataResponse(GetGossipDataResponse response) {
		if (state != State.ExpectLocalData) {
			System.out.println("Received local data in state: " + state + ". Ignoring.");
			return;
		}

		try {
			remoteGossipData.adjustRemoteTimestamps(dT);
		} catch (Exception e) {
			System.out.println("Failed to adjust remote ZMI timestamps. Finishing gossip.");
			e.printStackTrace();
			finish();
			return;
		}

		bus.sendMessage(new UpdateWithGossipDataMessage(
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.GOSSIP_MODULE_NAME,
				remoteGossipData
		));

		localGossipData = response.gossipData;
		if (localGossipData == null) {
			System.out.println("Received null as local gossip data. Finishing gossip.");
			finish();
		} else {
			finish();
			bus.sendMessage(createNetworkMessage(
					new DataResponsePayload(localGossipData)
			));
		}
	}

	private void handlePurgeGossipMachineMessage(PurgeGossipMachineMessage message) {
		System.out.println("Purging InboundGossipMachine " + machineId + ".");
		finish();
	}

	private void schedulePurge() {
		Runnable callback = () -> {
			bus.sendMessage(new PurgeGossipMachineMessage(
					ModuleNames.GOSSIP_MODULE_NAME,
					ModuleNames.TIMER_MODULE_NAME,
					machineId
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				ModuleNames.TIMER_MODULE_NAME,
				ModuleNames.GOSSIP_MODULE_NAME,
				callback,
				purgeTimeoutMs
		));
	}

	private OutNetworkMessage createNetworkMessage(Payload payload) {
		return new OutNetworkMessage(
				ModuleNames.COMM_MODULE_NAME,
				ModuleNames.GOSSIP_MODULE_NAME,
				srcAddress,
				payload
		);
	}

	private void finish() {
		state = State.Finished;
	}
}
