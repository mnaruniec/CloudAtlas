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
import pl.edu.mimuw.cloudatlas.agent.gossip.messages.UpdateWithGossipDataMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiInstallQueryRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiInstallQueryResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiSetFallbackContactsMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUninstallQueryRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUninstallQueryResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUpsertZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUpsertZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.agent.task.messages.PurgeOldZonesMessage;
import pl.edu.mimuw.cloudatlas.agent.task.messages.RefreshAttributeValuesMessage;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.ProtectedAttributesHelper;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.Program;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.signing.KeyReader;
import pl.edu.mimuw.cloudatlas.signing.SignatureVerifier;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedObject;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.InstallationPayload;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataModule extends Module {
	private DataModel model = new DataModel();

	private SignatureVerifier signatureVerifier;

	private PathName localPathName;
	private InetAddress localAddress;

	public DataModule(Bus bus, PathName localPathName, InetAddress localAddress, String publicKeyPath)
			throws InvalidKeySpecException, NoSuchAlgorithmException,
			IOException, InvalidKeyException, NoSuchPaddingException {
		super(bus);
		// TODO - config
		this.localPathName = localPathName;
		this.localAddress = localAddress;
		this.signatureVerifier = new SignatureVerifier(KeyReader.readPublic(publicKeyPath));
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
		} else if (message instanceof UpdateWithGossipDataMessage) {
			handleUpdateWithGossipDataMessage((UpdateWithGossipDataMessage) message);
		} else if (message instanceof RefreshAttributeValuesMessage) {
			handleRefreshAttributeValuesMessage((RefreshAttributeValuesMessage) message);
		} else if (message instanceof PurgeOldZonesMessage) {
			handlePurgeOldZonesMessage((PurgeOldZonesMessage) message);
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

	private void handleUpdateWithGossipDataMessage(UpdateWithGossipDataMessage message) {
		// TODO - remove debug prints
		System.out.println("Updating data.");
		Map<String, AttributesMap> zmiMap = message.gossipData.getZmiMap();
		Map<PathName, AttributesMap> newZmiMap = new HashMap<>();
		Map<PathName, AttributesMap> existingZmiMap = new HashMap<>();

		try {
			for (Map.Entry<String, AttributesMap> entry : zmiMap.entrySet()) {
				PathName pathName = new PathName(entry.getKey());
				verifyAttributesMap(pathName, entry.getValue());
				if (model.zmiIndex.containsKey(pathName.getName())) {
					existingZmiMap.put(pathName, entry.getValue());
				} else {
					newZmiMap.put(pathName, entry.getValue());
				}
			}
		} catch (Exception e) {
			System.out.println("Exception caught when verifying received gossip data. Leaving unchanged.");
			e.printStackTrace();
			return;
		}

		for (Map.Entry<PathName, AttributesMap> entry: existingZmiMap.entrySet()) {
			substituteZMIIfFresher(entry.getKey(), entry.getValue());
		}

		for (Map.Entry<PathName, AttributesMap> entry: newZmiMap.entrySet()) {
			PathName pathName = entry.getKey();
			createZMIPath(pathName);
			substituteZMI(pathName, entry.getValue());
		}

		System.out.println("Updated data.");
	}

	private void verifyAttributesMap(PathName pathName, AttributesMap attributesMap) {
		String name = ((ValueString) attributesMap.get(ZMI.NAME_ATTR)).getValue();
		if (!name.equals(pathName.getSingletonName())) {
			throw new IllegalArgumentException("Name is not compatible with pathName.");
		}

		String owner = ((ValueString) attributesMap.get(ZMI.OWNER_ATTR)).getValue();
		if (owner == null) {
			throw new NullPointerException("Owner is null.");
		}
		new PathName(owner);

		if (pathName.getComponents().size() != ((ValueInt) attributesMap.get(ZMI.LEVEL_ATTR)).getValue()) {
			throw new IllegalArgumentException("Level is not compatible with pathName.");
		}

		if (((ValueTime) attributesMap.get(ZMI.TIMESTAMP_ATTR)).getValue() == null) {
			throw new NullPointerException("Timestamp is null.");
		}

		if (((ValueInt) attributesMap.get(ZMI.CARDINALITY_ATTR)).getValue() == null) {
			throw new NullPointerException("Cardinality is null.");
		}

		Set<Value> contacts = ((ValueSet) attributesMap.get(ZMI.CONTACTS_ATTR)).getValue();
		for (Value contact: contacts) {
			if (((ValueContact) contact).getAddress() == null) {
				throw new NullPointerException("Contact's address is null.");
			}
			new PathName(((ValueContact) contact).getName().getName());
		}
	}

	private ZMI createZMIPath(PathName pathName) {
		ZMI zmi = model.zmiIndex.get(pathName.getName());
		if (zmi == null) {
			zmi = createInitializedZMI(pathName);
			model.zmiIndex.put(pathName.getName(), zmi);

			if (pathName.equals(PathName.ROOT)) {
				model.root = zmi;
			} else {
				createZMIPath(pathName.levelUp()).addSon(zmi);
			}
		}
		return zmi;
	}

	private ZMI createInitializedZMI(PathName pathName) {
		// TODO - verify if no queries
		ZMI zmi = new ZMI();
		AttributesMap attrMap = zmi.getAttributes();
		attrMap.add(ZMI.NAME_ATTR, new ValueString(pathName.getSingletonName()));
		attrMap.add(ZMI.LEVEL_ATTR, new ValueInt((long) pathName.getComponents().size()));
		// TODO - consider better initialization
		attrMap.add(ZMI.CARDINALITY_ATTR, new ValueInt(1L));
		attrMap.add(ZMI.OWNER_ATTR, new ValueString(pathName.toString()));
		refreshTimestamp(zmi);

		// TODO - consider adding as contact to all prefixes
		Set<Value> valueSet = new HashSet<>();
		if (pathName.equals(localPathName)) {
			valueSet.add(new ValueContact(
					localPathName,
					localAddress
			));
		}
		attrMap.add(ZMI.CONTACTS_ATTR, new ValueSet(valueSet, TypePrimitive.CONTACT));

		return zmi;
	}

	private void substituteZMIIfFresher(PathName pathName, AttributesMap attributesMap) {
		long localTimestamp = model.zmiIndex.get(pathName.getName()).getTimestamp();
		long remoteTimestamp = ((ValueTime) attributesMap.get(ZMI.TIMESTAMP_ATTR)).getValue();
		if (localTimestamp < remoteTimestamp) {
			substituteZMI(pathName, attributesMap);
		}
	}

	private void substituteZMI(PathName pathName, AttributesMap attributesMap) {
		// get old info
		String pathNameStr = pathName.getName();
		ZMI oldZMI = model.zmiIndex.get(pathNameStr);
		ZMI father = oldZMI.getFather();
		List<ZMI> sons = oldZMI.getSons();

		// create new ZMI
		ZMI newZMI = new ZMI(father);
		newZMI.getAttributes().add(attributesMap);
		for (ZMI son: sons) {
			newZMI.addSon(son);
		}

		// unlink old ZMI
		if (father != null) {
			father.removeSon(oldZMI);
		}

		// link new ZMI
		model.zmiIndex.put(pathNameStr, newZMI);
		if (father != null) {
			father.addSon(newZMI);
		}
		for (ZMI son: sons) {
			son.setFather(newZMI);
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
			} else if (message instanceof RmiInstallQueryRequest) {
				handleRmiInstallQueryRequest((RmiInstallQueryRequest) message);
			} else if (message instanceof RmiUninstallQueryRequest) {
				handleRmiUninstallQueryRequest((RmiUninstallQueryRequest) message);
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
		RmiResponse response;
		PathName pathName;
		AttributesMap attributesMap;
		Map<String, Value> attributes = request.attributes;;

		try {
			pathName = new PathName(request.zone);
			for (String attr : ProtectedAttributesHelper.PROTECTED_ATTRIBUTES) {
				if (attributes.containsKey(attr)) {
					throw new IllegalArgumentException("'" + attr + "' is a protected attribute.");
				}
			}

			attributesMap = new AttributesMap();
			for (Map.Entry<String, Value> entry : attributes.entrySet()) {
				Attribute attr = new Attribute(entry.getKey());
				if (Attribute.isQuery(attr)) {
					throw new IllegalArgumentException("Cannot set query attribute '" + attr.getName() + "'.");
				}
				if (entry.getValue().isInternal()) {
					throw new IllegalArgumentException("Value for attribute '" + attr.getName()
							+ "' is of internal type " + entry.getValue().getType() + ".");
				}
				attributesMap.add(attr, entry.getValue());
			}
		} catch (RuntimeException e) {
			System.out.println("Data module caught RuntimeException in upsertZoneAttributes. Leaving unchanged.");
			response = new RmiResponse(request, e);
			bus.sendMessage(response);
			return;
		}

		ZMI zmi = createZMIPath(pathName);
		zmi.getAttributes().addOrChange(attributesMap);
		refreshTimestamp(zmi);

		bus.sendMessage(new RmiUpsertZoneAttributesResponse(request));
	}

	private void handleRmiInstallQueryRequest(RmiInstallQueryRequest request) {
		RmiResponse response;
		try {
			verifyAndUpdateQuery(request.signedInstallation);
			response = new RmiInstallQueryResponse(request);
		} catch (RuntimeException e) {
			response = new RmiResponse(request, e);
			System.out.println("Exception when installing query. Ignoring.");
			e.printStackTrace();
		}

		bus.sendMessage(response);
	}

	private void handleRmiUninstallQueryRequest(RmiUninstallQueryRequest request) {
		RmiResponse response;
		try {
			verifyAndUpdateQuery(request.signedUninstallation);
			response = new RmiUninstallQueryResponse(request);
		} catch (RuntimeException e) {
			response = new RmiResponse(request, e);
			System.out.println("Exception when uninstalling query. Ignoring.");
			e.printStackTrace();
		}

		bus.sendMessage(response);
	}

	private void verifyAndUpdateQuery(SignedObject signedObject) {
		signatureVerifier.verify(signedObject);
		SignedObject old = model.queryMap.get(signedObject.getPayload().getName());
		if (old != null && old.getTimestamp() > signedObject.getTimestamp()) {
			throw new IllegalArgumentException("Agent already has a fresher status of this query.");
		}
		model.queryMap.put(signedObject.getPayload().getName(), signedObject);
	}

	private void handleRmiGetFallbackContactsRequest(RmiGetFallbackContactsRequest request) {
		bus.sendMessage(new RmiGetFallbackContactsResponse(
				request, new HashSet<>(model.fallbackContacts)
		));
	}

	private void handleRmiSetFallbackContactsMessage(RmiSetFallbackContactsMessage message) {
		model.fallbackContacts = message.fallbackContacts;
	}

	private void handleRefreshAttributeValuesMessage(RefreshAttributeValuesMessage message) {
		System.out.println("Data module refreshing.");
		Map<Attribute, Program> queries = getInstalledQueries();
		if (model.root != null) {
			refreshAttributeValues(model.root, queries);
		}
	}

	private Map<Attribute, Program> getInstalledQueries() {
		Map<Attribute, Program> queries = new HashMap<>();
		for (Map.Entry<Attribute, SignedObject> entry : model.queryMap.entrySet()) {
			SignedObject signedObject = entry.getValue();
			if (signedObject.getPayload() instanceof InstallationPayload) {
				queries.put(entry.getKey(), ((InstallationPayload) signedObject.getPayload()).getQuery());
			}
		}
		return queries;
	}

	private void refreshAttributeValues(ZMI zmi, Map<Attribute, Program> queries) {
		List<ZMI> sons = zmi.getSons();
		if (!sons.isEmpty()) {
			for (ZMI son : sons) {
				refreshAttributeValues(son, queries);
			}

			boolean changed = false;
			for (Map.Entry<Attribute, Value> entry: zmi.getAttributes().clone()) {
				Attribute attr = entry.getKey();
				if (!ProtectedAttributesHelper.isProtected(attr)) {
					zmi.getAttributes().remove(attr);
					changed = true;
				}
			}

			for (Map.Entry<Attribute, Program> entry : queries.entrySet()) {
				try {
					execQuery(entry.getValue(), zmi);
					changed = true;
				} catch (Exception e) {
					System.out.println(
							"Exception when evaluating query '" + entry.getKey().getName()
									+ "' in node '" + zmi.getName() + "'. Ignoring."
					);
				}
			}
			// TODO - internal queries

			if (changed) {
				refreshTimestamp(zmi);
			}
		}
	}

	private void execQuery(Program query, ZMI zmi) {
		Interpreter interpreter = new Interpreter(zmi);

		List<QueryResult> results = interpreter.interpretProgram(query);
		for (QueryResult result : results) {
			zmi.getAttributes().addOrChange(result.getName(), result.getValue());
		}
	}

	private void refreshTimestamp(ZMI zmi) {
		zmi.getAttributes().addOrChange(ZMI.TIMESTAMP_ATTR, new ValueTime());
	}

	private void handlePurgeOldZonesMessage(PurgeOldZonesMessage message) {
		System.out.println("Data module purging.");
		purgeOldZones(message.timestamp);
	}

	private void purgeOldZones(long timestamp) {
		PathName root = new PathName("");
		if (model.root != null && purgeOldZones(model.root, root, timestamp)) {
			model.zmiIndex.remove(root.getName());
			model.root = null;
		}
	}

	// true iff zmi needs to be purged
	private boolean purgeOldZones(ZMI zmi, PathName path, long timestamp) {
		Set<ZMI> removedSons = new HashSet<>();

		for (ZMI son: zmi.getSons()) {
			PathName sonPath = path.levelDown(son.getName());
			if (purgeOldZones(son, sonPath, timestamp)) {
				removedSons.add(son);
				model.zmiIndex.remove(sonPath.getName());
			}
		}

		zmi.removeSons(removedSons);

		return zmi.getSons().isEmpty() && zmi.getTimestamp() < timestamp;
	}
}
