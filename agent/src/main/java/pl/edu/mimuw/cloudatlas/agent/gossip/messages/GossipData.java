package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.gtp.GtpUtils;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueTime;
import pl.edu.mimuw.cloudatlas.model.ZMI;
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

	public void adjustRemoteTimestamps(long dT) {
		for (Map.Entry<String, AttributesMap> entry: getZmiMap().entrySet()) {
			AttributesMap attrMap = entry.getValue();
			long timestamp = ((ValueTime) attrMap.get(ZMI.TIMESTAMP_ATTR)).getValue();
			long newTimestamp = GtpUtils.adjustRemoteTimestamp(timestamp, dT);
			attrMap.addOrChange(ZMI.TIMESTAMP_ATTR, new ValueTime(newTimestamp));
		}
	}
}
