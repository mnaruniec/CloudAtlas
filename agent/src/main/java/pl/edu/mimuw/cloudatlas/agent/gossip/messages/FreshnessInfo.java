package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import java.util.Collections;
import java.util.Map;

public class FreshnessInfo {
	private Map<String, Long> zmiTimestamps;

	public FreshnessInfo(Map<String, Long> zmiTimestamps) {
		this.zmiTimestamps = zmiTimestamps;
	}

	public Map<String, Long> getZmiTimestamps() {
		return Collections.unmodifiableMap(zmiTimestamps);
	}
}
