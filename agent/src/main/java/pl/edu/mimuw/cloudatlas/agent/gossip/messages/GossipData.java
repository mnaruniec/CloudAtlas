package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class GossipData {
	private Map<String, AttributesMap> zmiMap;
	private List<SignedObject> queryList;

	private GossipData() {}

	public GossipData(Map<String, AttributesMap> zmiMap, List<SignedObject> queryList) {
		this.zmiMap = zmiMap;
		this.queryList = queryList;
	}

	public Map<String, AttributesMap> getZmiMap() {
		return Collections.unmodifiableMap(zmiMap);
	}

	public List<SignedObject> getQueryList() {
		return Collections.unmodifiableList(queryList);
	}
}
