package pl.edu.mimuw.cloudatlas.agent.data;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.FreshnessInfo;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetFreshnessInfoResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipDataResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetRequest;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GetGossipTargetResponse;
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.GossipData;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiSetFallbackContactsMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUpsertZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataModule extends Module {
	private DataModel model = new DataModel();

	public DataModule(Bus bus) {
		super(bus);
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_DATA_MODULE_NAME;
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof RmiMessage) {
			handleRmiMessage((RmiMessage) message);
		} else if (message instanceof GetGossipTargetRequest) {
			handleGetGossipTargetRequest((GetGossipTargetRequest) message);
		} else if (message instanceof GetFreshnessInfoRequest) {
			handleGetFreshnessInfoRequest((GetFreshnessInfoRequest) message);
		} else if (message instanceof GetGossipDataRequest) {
			handleGetGossipDataRequest((GetGossipDataRequest) message);
		} else {
			System.out.println("Received unexpected type of message in data module. Ignoring.");
		}
	}

	private void handleGetGossipTargetRequest(GetGossipTargetRequest request) {
		// TODO
		try {
			bus.sendMessage(new GetGossipTargetResponse(
					request,
					new ValueContact(new PathName("/uw/violet07"), InetAddress.getByName("127.0.0.1"))
			));
		} catch (UnknownHostException e) {
			System.out.println("Weird.");
			e.printStackTrace();
		}
	}

	private void handleGetFreshnessInfoRequest(GetFreshnessInfoRequest request) {
		FreshnessInfo freshnessInfo = null;

		try {
			Map<String, ZMI> zmiMap = getRelevantZMIs(request.pathName);

			Map<String, Long> zmiTimestamps = new HashMap<>();
			for (Map.Entry<String, ZMI> entry: zmiMap.entrySet()) {
				zmiTimestamps.put(entry.getKey(), entry.getValue().getTimestamp());
			}

			freshnessInfo = new FreshnessInfo(zmiTimestamps);
		} catch (Exception e) {
			System.out.println("Exception occured in data module when collecting freshness info. Returning null.");
			e.printStackTrace();
		}

		bus.sendMessage(new GetFreshnessInfoResponse(
				request,
				freshnessInfo
		));
	}

	private void handleGetGossipDataRequest(GetGossipDataRequest request) {
		GossipData gossipData = null;

		try {
			Map<String, ZMI> zmiMap = getRelevantZMIs(request.pathName);
			Map<String, Long> zmiTimestamps = request.remoteFreshnessInfo.getZmiTimestamps();

			Map<String, AttributesMap> attributes = new HashMap<>();
			for (Map.Entry<String, ZMI> entry: zmiMap.entrySet()) {
				String pathName = entry.getKey();
				ZMI zmi = entry.getValue();
				long timestamp = zmiTimestamps.get(pathName);

				// TODO - remove true
				if (timestamp < zmi.getTimestamp() || true) {
					attributes.put(pathName, zmi.getAttributes().clone());
				}
			}

			gossipData = new GossipData(attributes);
		} catch (Exception e) {
			System.out.println("Exception occured in data module when collecting gossip data. Returning null.");
			e.printStackTrace();
		}

		bus.sendMessage(new GetGossipDataResponse(
				request,
				gossipData
		));
	}

	private Map<String, ZMI> getRelevantZMIs(PathName target) {
		Map<String, ZMI> zmiMap = new HashMap<>();
		if (model.root != null) {
			getRelevantZMIs(model.root, zmiMap, new LinkedList<>(), new ArrayList<>(target.getComponents()));
		}
		return zmiMap;
	}

	private void getRelevantZMIs(ZMI zmi, Map<String, ZMI> zmiMap, Deque<String> path, List<String> targetPath) {
		int depth = path.size();
		if (depth >= targetPath.size()) {
			return;
		}
		for (ZMI son: zmi.getSons()) {
			String sonName = son.getName();
			path.addLast(sonName);
			// TODO - consider going back to omitting target zone
			if (targetPath.get(depth).equals(sonName) && path.size() < targetPath.size()) {
				getRelevantZMIs(son, zmiMap, path, targetPath);
			} else {
				zmiMap.put(new PathName(path).getName(), son);
			}
			path.removeLast();
		}
	}

	private void handleRmiMessage(RmiMessage message) {
		try {
			if (message instanceof RmiGetFallbackContactsRequest) {
				handleRmiGetFallbackContactsRequest((RmiGetFallbackContactsRequest) message);
			} else if (message instanceof RmiSetFallbackContactsMessage) {
				handleRmiSetFallbackContactsMessage((RmiSetFallbackContactsMessage) message);
			} else if (message instanceof RmiGetStoredZonesRequest) {
				handleRmiGetStoredZonesRequest((RmiGetStoredZonesRequest) message);
			} else if (message instanceof RmiGetZoneAttributesRequest) {
				handleRmiGetZoneAttributesRequest((RmiGetZoneAttributesRequest) message);
			} else if (message instanceof RmiUpsertZoneAttributesRequest) {
				handleRmiUpsertZoneAttributesRequest((RmiUpsertZoneAttributesRequest) message);
			} else {
				System.out.println("Received unexpected type of RMI message in data module. Ignoring.");
			}
		} catch (RuntimeException e) {
			System.out.println(
					"Data module caught unhandled RuntimeException when handling RmiMessage." +
					"Sending to rmi module."
			);
			bus.sendMessage(new RmiResponse(message, e));
		}
	}

	private void handleRmiGetStoredZonesRequest(RmiGetStoredZonesRequest request) {
		bus.sendMessage(new RmiGetStoredZonesResponse(
				request, new HashSet<>(model.zmiIndex.keySet())
		));
	}

	private void handleRmiGetZoneAttributesRequest(RmiGetZoneAttributesRequest request) {
		RmiResponse response;
		try {
			ZMI zmi = model.zmiIndex.get(new PathName(request.zone).getName());
			if (zmi == null) {
				response = new RmiResponse(
						request,
						new IllegalArgumentException("Zone '" + request.zone + "' not found.")
				);
			} else {
				response = new RmiGetZoneAttributesResponse(
						request,
						zmi.getAttributes().toMap(true)
				);
			}
		} catch (IllegalArgumentException e) {
			response = new RmiResponse(
					request,
					new IllegalArgumentException("'" + request.zone + "' is not a proper zone name.")
			);
		}

		bus.sendMessage(response);
	}

	private void handleRmiUpsertZoneAttributesRequest(RmiUpsertZoneAttributesRequest request) {
		// TODO
	}

	private void handleRmiGetFallbackContactsRequest(RmiGetFallbackContactsRequest request) {
		bus.sendMessage(new RmiGetFallbackContactsResponse(
				request, new HashSet<>(model.fallbackContacts)
		));
	}

	private void handleRmiSetFallbackContactsMessage(RmiSetFallbackContactsMessage message) {
		model.fallbackContacts = message.fallbackContacts;
	}
}
