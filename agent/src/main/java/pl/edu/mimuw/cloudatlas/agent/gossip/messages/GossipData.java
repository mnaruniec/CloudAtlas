package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.model.AttributesMap;

import java.util.Collections;
import java.util.Map;

public final class GossipData {
	private Map<String, AttributesMap> zmiMap;

	private GossipData() {}

	public GossipData(Map<String, AttributesMap> zmiMap) {
		this.zmiMap = zmiMap;
	}

	public Map<String, AttributesMap> getZmiMap() {
		return Collections.unmodifiableMap(zmiMap);
	}
}
