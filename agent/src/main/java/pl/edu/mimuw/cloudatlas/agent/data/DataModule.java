package pl.edu.mimuw.cloudatlas.agent.data;

import pl.edu.mimuw.cloudatlas.agent.AgentConfig;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.ModuleNames;
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
import pl.edu.mimuw.cloudatlas.interpreter.query.Yylex;
import pl.edu.mimuw.cloudatlas.interpreter.query.parser;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueSet;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;
import pl.edu.mimuw.cloudatlas.signing.KeyReader;
import pl.edu.mimuw.cloudatlas.signing.SignatureVerifier;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedObject;
import pl.edu.mimuw.cloudatlas.signing.outputs.payloads.InstallationPayload;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataModule extends Module {
	public static final int CONTACTS_SAMPLE = 3;

	public static final String[] INTERNAL_QUERIES = {
			"SELECT sum(" + ZMI.CARDINALITY_ATTR + ") AS " + ZMI.CARDINALITY_ATTR,
			"SELECT to_set(random(" + CONTACTS_SAMPLE + ", unfold(" + ZMI.CONTACTS_ATTR + "))) AS " + ZMI.CONTACTS_ATTR,
	};

	private Data data = new Data();

	private GossipTargetStrategy gossipTargetStrategy;
	private SignatureVerifier signatureVerifier;
	private List<Program> internalQueries = new LinkedList<>();

	private PathName localPathName;
	private InetAddress localAddress;
	private ValueContact localContact;

	public DataModule(Bus bus, AgentConfig config)
			throws Exception {
		super(bus);
		this.localPathName = config.getPathname();
		this.localAddress = config.getIP();
		this.localContact = new ValueContact(localPathName, localAddress);
		this.signatureVerifier = new SignatureVerifier(KeyReader.readPublic(config.getPublicKeyPath()));
		this.gossipTargetStrategy = GossipTargetStrategy.createStrategy(config.getGossipTargetStrategy());

		for (String query: INTERNAL_QUERIES) {
			Yylex lex = new Yylex(new ByteArrayInputStream(query.getBytes()));
			internalQueries.add((new parser(lex)).pProgram());
		}
	}

	@Override
	public String getName() {
		return ModuleNames.DATA_MODULE_NAME;
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
		ValueContact target = gossipTargetStrategy.getNextTarget(gatherLevels());
		bus.sendMessage(new GetGossipTargetResponse(
				request,
				target
		));
//		System.out.println("Next target would be " + target);

		// TODO
//		try {
//			bus.sendMessage(new GetGossipTargetResponse(
//					request,
//					new ValueContact(new PathName("/uw/violet07"), InetAddress.getByName("127.0.0.1"))
//			));
//		} catch (UnknownHostException e) {
//			System.out.println("Weird.");
//			e.printStackTrace();
//		}
	}

	private List<List<List<ValueContact>>> gatherLevels() {
		List<List<List<ValueContact>>> output = new ArrayList<>();
		for (int i = 0; i < localPathName.getComponents().size() + 1; i++) {
			output.add(new ArrayList<>());
		}

		List<ValueContact> fallbacks = getFilteredContacts(data.fallbackContacts);
		if (!fallbacks.isEmpty()) {
			output.get(0).add(fallbacks);
		}

		if (data.root != null) {
			gatherLevels(data.root, 0, output);
		}

		return output;
	}

	private void gatherLevels(ZMI zmi, int depth, List<List<List<ValueContact>>> output) {
		if (depth >= output.size()) {
			return;
		}

		for (ZMI son: zmi.getSons()) {
			gatherLevels(son, depth + 1, output);
		}

		// don't gather root
		if (depth == 0) {
			return;
		}

		List<ValueContact> contacts = getFilteredContacts(zmi.getContacts());
		if (!contacts.isEmpty()) {
			output.get(depth).add(contacts);
		}
	}

	private List<ValueContact> getFilteredContacts(Set<? extends Value> inputContacts) {
		List<ValueContact> contacts = new ArrayList<>();
		for (Value contact: inputContacts) {
			if (!((ValueContact)contact).getName().equals(localPathName)) {
				contacts.add((ValueContact) contact);
			}
		}
		return contacts;
	}

	private void handleGetFreshnessInfoRequest(GetFreshnessInfoRequest request) {
		FreshnessInfo freshnessInfo = null;

		try {
			Map<String, Long> zmiTimestamps = getZmiTimestamps(request.pathName);
			Map<String, Long> queryTimestamps = getQueryTimestamps();
			freshnessInfo = new FreshnessInfo(zmiTimestamps, queryTimestamps);
		} catch (Exception e) {
			System.out.println("Exception occured in data module when collecting freshness info. Returning null.");
			e.printStackTrace();
		}

		bus.sendMessage(new GetFreshnessInfoResponse(
				request,
				freshnessInfo
		));
	}

	private Map<String, Long> getZmiTimestamps(PathName target) {
		Map<String, ZMI> zmiMap = getRelevantZMIs(target);

		Map<String, Long> zmiTimestamps = new HashMap<>();
		for (Map.Entry<String, ZMI> entry: zmiMap.entrySet()) {
			zmiTimestamps.put(entry.getKey(), entry.getValue().getTimestamp());
		}

		return zmiTimestamps;
	}

	private Map<String, Long> getQueryTimestamps() {
		Map<String, Long> queryTimestamps = new HashMap<>();

		for (Map.Entry<Attribute, SignedObject> entry: data.queryMap.entrySet()) {
			queryTimestamps.put(entry.getKey().getName(), entry.getValue().getTimestamp());
		}

		return queryTimestamps;
	}

	private void handleGetGossipDataRequest(GetGossipDataRequest request) {
		GossipData gossipData = null;

		try {
			Map<String, AttributesMap> zmiMap = getZMIGossipData(request);
			List<SignedObject> queryList = getQueryGossipData(request.remoteFreshnessInfo);
			gossipData = new GossipData(zmiMap, queryList);
		} catch (Exception e) {
			System.out.println("Exception occured in data module when collecting gossip data. Returning null.");
			e.printStackTrace();
		}

		bus.sendMessage(new GetGossipDataResponse(
				request,
				gossipData
		));
	}

	private Map<String, AttributesMap> getZMIGossipData(GetGossipDataRequest request) {
		Map<String, AttributesMap> attributes = new HashMap<>();

		Map<String, ZMI> zmiMap = getRelevantZMIs(request.pathName);
		Map<String, Long> zmiTimestamps = request.remoteFreshnessInfo.getZmiTimestamps();

		for (Map.Entry<String, ZMI> entry: zmiMap.entrySet()) {
			String pathName = entry.getKey();
			ZMI zmi = entry.getValue();
			long timestamp = zmiTimestamps.get(pathName);

			// TODO - remove true
			if (timestamp < zmi.getTimestamp()/* || true*/) {
				attributes.put(pathName, zmi.getAttributes().clone());
			}
		}

		return attributes;
	}

	private List<SignedObject> getQueryGossipData(FreshnessInfo remoteFreshnessInfo) {
		List<SignedObject> queryList = new ArrayList<>();

		for (Map.Entry<Attribute, SignedObject> entry: data.queryMap.entrySet()) {
			Long remoteTimestamp = remoteFreshnessInfo.getQueryTimestamps().get(entry.getKey().getName());
			long localTimestamp = entry.getValue().getTimestamp();

			// TODO - remove true
			if (remoteTimestamp == null || remoteTimestamp < localTimestamp /*|| true*/) {
				queryList.add(entry.getValue());
			}
		}

		return queryList;
	}

	private Map<String, ZMI> getRelevantZMIs(PathName target) {
		Map<String, ZMI> zmiMap = new HashMap<>();
		if (data.root != null) {
			getRelevantZMIs(data.root, zmiMap, new LinkedList<>(), new ArrayList<>(target.getComponents()));
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
			if (targetPath.get(depth).equals(sonName) && path.size() < targetPath.size()) {
				getRelevantZMIs(son, zmiMap, path, targetPath);
			} else {
				zmiMap.put(new PathName(path).toString(), son);
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
			verifyZMIGossipData(zmiMap, newZmiMap, existingZmiMap);
			verifyQueryGossipData(message.gossipData.getQueryList());
		} catch (Exception e) {
			System.out.println("Exception caught when verifying received gossip data. Leaving unchanged.");
			e.printStackTrace();
			return;
		}

		updateWithZMIGossipData(newZmiMap, existingZmiMap);
		updateWithQueryGossipData(message.gossipData.getQueryList());

		System.out.println("Updated data.");
	}

	// last two arguments are empty "return values"
	private void verifyZMIGossipData(Map<String, AttributesMap> zmiMap,
									 Map<PathName, AttributesMap> newZmiMap, Map<PathName, AttributesMap> existingZmiMap) {
		for (Map.Entry<String, AttributesMap> entry : zmiMap.entrySet()) {
			PathName pathName = new PathName(entry.getKey());
			verifyAttributesMap(pathName, entry.getValue());
			if (data.zmiIndex.containsKey(pathName.toString())) {
				existingZmiMap.put(pathName, entry.getValue());
			} else {
				newZmiMap.put(pathName, entry.getValue());
			}
		}
	}

	private void verifyQueryGossipData(List<SignedObject> queryList) {
		queryList.forEach(signatureVerifier::verify);
	}

	private void updateWithZMIGossipData(Map<PathName, AttributesMap> newZmiMap,
										 Map<PathName, AttributesMap> existingZmiMap) {
		for (Map.Entry<PathName, AttributesMap> entry: existingZmiMap.entrySet()) {
			substituteZMIIfFresher(entry.getKey(), entry.getValue());
		}

		for (Map.Entry<PathName, AttributesMap> entry: newZmiMap.entrySet()) {
			PathName pathName = entry.getKey();
			createZMIPath(pathName);
			substituteZMI(pathName, entry.getValue());
		}
	}

	private void updateWithQueryGossipData(List<SignedObject> queryList) {
		for (SignedObject query: queryList) {
			Attribute name = query.getPayload().getName();
			long remoteTimestamp = query.getTimestamp();
			SignedObject localQuery = data.queryMap.get(name);
			if (localQuery == null || localQuery.getTimestamp() < remoteTimestamp) {
				data.queryMap.put(name, query);
			}
		}
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
			new PathName(((ValueContact) contact).getName().toString());
		}

		for (Map.Entry<Attribute, Value> entry: attributesMap) {
			if (Attribute.isQuery(entry.getKey())) {
				throw new IllegalArgumentException("Query attributes are forbidden.");
			}
		}
	}

	private ZMI createZMIPath(PathName pathName) {
		ZMI zmi = data.zmiIndex.get(pathName.toString());
		if (zmi == null) {
			zmi = createInitializedZMI(pathName);
			data.zmiIndex.put(pathName.toString(), zmi);

			if (pathName.equals(PathName.ROOT)) {
				data.root = zmi;
			} else {
				createZMIPath(pathName.levelUp()).addSon(zmi);
			}
		}
		return zmi;
	}

	private ZMI createInitializedZMI(PathName pathName) {
		ZMI zmi = new ZMI();
		AttributesMap attrMap = zmi.getAttributes();
		String name = pathName.equals(PathName.ROOT) ? null : pathName.getSingletonName();
		attrMap.add(ZMI.NAME_ATTR, new ValueString(name));
		attrMap.add(ZMI.LEVEL_ATTR, new ValueInt((long) pathName.getComponents().size()));
		attrMap.add(ZMI.CARDINALITY_ATTR, new ValueInt(1L));
		refreshOwnerAndTimestamp(zmi);

		Set<Value> valueSet = new HashSet<>();
		if (pathName.equals(localPathName)) {
			valueSet.add(localContact);
		}
		attrMap.add(ZMI.CONTACTS_ATTR, new ValueSet(valueSet, TypePrimitive.CONTACT));

		return zmi;
	}

	private void substituteZMIIfFresher(PathName pathName, AttributesMap attributesMap) {
		long localTimestamp = data.zmiIndex.get(pathName.toString()).getTimestamp();
		long remoteTimestamp = ((ValueTime) attributesMap.get(ZMI.TIMESTAMP_ATTR)).getValue();
		if (localTimestamp < remoteTimestamp) {
			substituteZMI(pathName, attributesMap);
		}
	}

	private void substituteZMI(PathName pathName, AttributesMap attributesMap) {
		// get old info
		String pathNameStr = pathName.toString();
		ZMI oldZMI = data.zmiIndex.get(pathNameStr);
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
		data.zmiIndex.put(pathNameStr, newZMI);
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
				request, new HashSet<>(data.zmiIndex.keySet())
		));
	}

	private void handleRmiGetZoneAttributesRequest(RmiGetZoneAttributesRequest request) {
		RmiResponse response;
		try {
			ZMI zmi = data.zmiIndex.get(new PathName(request.zone).toString());
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
		refreshOwnerAndTimestamp(zmi);

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
		SignedObject old = data.queryMap.get(signedObject.getPayload().getName());
		if (old != null && old.getTimestamp() > signedObject.getTimestamp()) {
			throw new IllegalArgumentException("Agent already has a fresher status of this query.");
		}
		data.queryMap.put(signedObject.getPayload().getName(), signedObject);
	}

	private void handleRmiGetFallbackContactsRequest(RmiGetFallbackContactsRequest request) {
		bus.sendMessage(new RmiGetFallbackContactsResponse(
				request, new HashSet<>(data.fallbackContacts)
		));
	}

	private void handleRmiSetFallbackContactsMessage(RmiSetFallbackContactsMessage message) {
		data.fallbackContacts = message.fallbackContacts;
	}

	private void handleRefreshAttributeValuesMessage(RefreshAttributeValuesMessage message) {
		System.out.println("Data module refreshing.");
		Map<Attribute, Program> queries = getInstalledQueries();
		if (data.root != null) {
			refreshAttributeValues(data.root, queries);
		}
	}

	private Map<Attribute, Program> getInstalledQueries() {
		Map<Attribute, Program> queries = new HashMap<>();
		for (Map.Entry<Attribute, SignedObject> entry : data.queryMap.entrySet()) {
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

			for (Program query: internalQueries) {
				try {
					execQuery(query, zmi);
					changed = true;
				} catch (Exception e) {
					System.out.println(
							"Exception when evaluating internal query in node '"
									+ zmi.getName() + "'. Ignoring."
					);
				}
			}

			if (changed) {
				refreshOwnerAndTimestamp(zmi);
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

	private void refreshOwnerAndTimestamp(ZMI zmi) {
		zmi.getAttributes().addOrChange(ZMI.OWNER_ATTR, new ValueString(localPathName.toString()));
		zmi.getAttributes().addOrChange(ZMI.TIMESTAMP_ATTR, new ValueTime());
	}

	private void handlePurgeOldZonesMessage(PurgeOldZonesMessage message) {
		System.out.println("Data module purging.");
		purgeOldZones(message.timestamp);
	}

	private void purgeOldZones(long timestamp) {
		PathName root = new PathName("");
		if (data.root != null && purgeOldZones(data.root, root, timestamp)) {
			data.zmiIndex.remove(root.toString());
			data.root = null;
		}
	}

	// true iff zmi needs to be purged
	private boolean purgeOldZones(ZMI zmi, PathName path, long timestamp) {
		Set<ZMI> removedSons = new HashSet<>();

		for (ZMI son: zmi.getSons()) {
			PathName sonPath = path.levelDown(son.getName());
			if (purgeOldZones(son, sonPath, timestamp)) {
				removedSons.add(son);
				data.zmiIndex.remove(sonPath.toString());
			}
		}

		zmi.removeSons(removedSons);

		return zmi.getSons().isEmpty() && zmi.getTimestamp() < timestamp;
	}
}
