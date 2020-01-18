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
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.InitiateGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.PurgeGossipMachineMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.RetryGossipMessage;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.UpdateWithGossipDataMessage;
import pl.edu.mimuw.cloudatlas.agent.timer.SetTimeoutMessage;
import pl.edu.mimuw.cloudatlas.gtp.GtpUtils;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.util.Date;

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
	private FreshnessInfo remoteFreshnessInfo;
	private GossipData localGossipData;
	private GossipData remoteGossipData;
	private long t2a, t2b, t1b, t1a;  // GTP timestamps
	private long rtd;
	// says how much remote clock is ahead of local clock
	private long dT;

	private long creationTimestamp = new Date().getTime();
	public final long machineId;
	public final PathName localPathName;
	private Bus bus;

	public final long gossipIntervalMs;
	public final long purgeTimeoutMs;
	public final long retryIntervalMs;
	public final int retryLimit;

	public OutboundGossipMachine(Bus bus, PathName localPathName, long machineId,
								 long gossipIntervalMs, long purgeTimeoutMs, long retryIntervalMs, int retryLimit) {
		this.bus = bus;
		this.localPathName = localPathName;
		this.machineId = machineId;
		this.gossipIntervalMs = gossipIntervalMs;
		this.purgeTimeoutMs = purgeTimeoutMs;
		this.retryIntervalMs = retryIntervalMs;
		this.retryLimit = retryLimit;
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
			if (message instanceof GetGossipTargetResponse) {
				handleGetGossipTargetResponse((GetGossipTargetResponse) message);
			} else if (message instanceof GetFreshnessInfoResponse) {
				handleGetFreshnessInfoResponse((GetFreshnessInfoResponse) message);
			} else if (message instanceof GetGossipDataResponse) {
				handleGetGossipDataResponse((GetGossipDataResponse) message);
			} else if (message instanceof PurgeGossipMachineMessage) {
				handlePurgeGossipMachineMessage((PurgeGossipMachineMessage) message);
			} else if (message instanceof RetryGossipMessage) {
				handleRetryGossipMessage((RetryGossipMessage) message);
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
		if (target == null || !message.srcAddress.equals(target.getAddress())) {
			System.out.println("OutboundGossipMachine received network message from unexpected address. Ignoring.");
			return;
		}

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
			System.out.println("Received gossip target in state: " + state + ". Ignoring.");
			return;
		}

		if (response.contact == null) {
			System.out.println("Received null contact as gossip target. Finishing gossip.");
			finish();
		} else {
			System.out.println("Starting outbound gossip with contact: " + response.contact);

			target = response.contact;
			state = State.ExpectLocalFreshnessInfo;
			bus.sendMessage(new GetFreshnessInfoRequest(
					ModuleNames.DATA_MODULE_NAME,
					ModuleNames.GOSSIP_MODULE_NAME,
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
			scheduleRetry(retryLimit);

			state = State.ExpectRemoteFreshnessInfo;
			sendLocalFreshnessInfo();
		}
	}

	private void handleNetworkFreshnessInfoResponse(InNetworkMessage message) {
		if (state != State.ExpectRemoteFreshnessInfo) {
			System.out.println("Received remote freshness info in state: " + state + ". Ignoring.");
			return;
		}

		remoteFreshnessInfo = ((FreshnessInfoPayload) message.payload).getFreshnessInfo();
		if (remoteFreshnessInfo == null) {
			System.out.println("Received null as remote freshness info. Finishing gossip.");
			finish();
		} else {
			t2a = message.receiveTimestamp;
			t2b = message.sendTimestamp;
			t1b = ((FreshnessInfoResponsePayload) message.payload).getT1b();
			t1a = ((FreshnessInfoResponsePayload) message.payload).getT1a();
			rtd = GtpUtils.getRoundTripDelay(t2a, t2b, t1b, t1a);
			dT = GtpUtils.getTimeOffset(t2a, t2b, rtd);

			remoteFreshnessInfo.adjustRemoteTimestamps(dT);

			state = State.ExpectLocalData;
			bus.sendMessage(new GetGossipDataRequest(
					ModuleNames.DATA_MODULE_NAME,
					ModuleNames.GOSSIP_MODULE_NAME,
					getMachineId(),
					target.getName(),
					remoteFreshnessInfo
			));
		}
	}

	private void handleGetGossipDataResponse(GetGossipDataResponse response) {
		if (state != State.ExpectLocalData) {
			System.out.println("Received local gossip data in state: " + state + ". Ignoring.");
			return;
		}

		localGossipData = response.gossipData;
		if (localGossipData == null) {
			System.out.println("Received null as local gossip data. Finishing gossip.");
			finish();
		} else {
			state = State.ExpectRemoteData;
			bus.sendMessage(createNetworkMessage(
					new DataRequestPayload(
							localGossipData,
							t2a,
							t2b
					)
			));
		}
	}

	private void handleNetworkDataResponse(InNetworkMessage message) {
		if (state != State.ExpectRemoteData) {
			System.out.println("Received remote gossip data in state: " + state + ". Ignoring.");
			return;
		}

		remoteGossipData = ((DataPayload) message.payload).getGossipData();
		if (remoteGossipData == null) {
			System.out.println("Received null as remote gossip data. Finishing gossip.");
			finish();
		} else {
			try {
				remoteGossipData.adjustRemoteTimestamps(dT);
			} catch (Exception e) {
				System.out.println("Failed to adjust remote ZMI timestamps. Finishing gossip.");
				e.printStackTrace();
				finish();
				return;
			}

			finish();

			bus.sendMessage(new UpdateWithGossipDataMessage(
					ModuleNames.DATA_MODULE_NAME,
					ModuleNames.GOSSIP_MODULE_NAME,
					remoteGossipData
			));
		}
	}

	private void handlePurgeGossipMachineMessage(PurgeGossipMachineMessage message) {
		System.out.println("Purging OutboundGossipMachine " + machineId + ".");
		finish();
	}

	private void handleRetryGossipMessage(RetryGossipMessage message) {
		if (state != State.ExpectRemoteFreshnessInfo) {
			return;
		}

		System.out.println("Retrying sending initial gossip datagram.");
		scheduleRetry(message.left);
		sendLocalFreshnessInfo();
	}

	private void sendLocalFreshnessInfo() {
		bus.sendMessage(createNetworkMessage(
				new FreshnessInfoRequestPayload(localPathName, localFreshnessInfo)
		));
	}

	private OutNetworkMessage createNetworkMessage(Payload payload) {
		return new OutNetworkMessage(
				ModuleNames.COMM_MODULE_NAME,
				ModuleNames.GOSSIP_MODULE_NAME,
				target.getAddress(),
				payload
		);
	}

	private void finish() {
		if (state != State.Finished) {
			state = State.Finished;
			rescheduleGossip();
		}
	}

	private void rescheduleGossip() {
		Runnable callback = () -> {
			bus.sendMessage(new InitiateGossipMessage(
					ModuleNames.GOSSIP_MODULE_NAME,
					ModuleNames.TIMER_MODULE_NAME
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				ModuleNames.TIMER_MODULE_NAME,
				ModuleNames.GOSSIP_MODULE_NAME,
				callback,
				gossipIntervalMs,
				creationTimestamp
		));
	}

	private void scheduleRetry(int left) {
		if (left < 1) {
			return;
		}

		Runnable callback = () -> {
			bus.sendMessage(new RetryGossipMessage(
					ModuleNames.GOSSIP_MODULE_NAME,
					ModuleNames.TIMER_MODULE_NAME,
					machineId,
					left - 1
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				ModuleNames.TIMER_MODULE_NAME,
				ModuleNames.GOSSIP_MODULE_NAME,
				callback,
				retryIntervalMs
		));
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
}
