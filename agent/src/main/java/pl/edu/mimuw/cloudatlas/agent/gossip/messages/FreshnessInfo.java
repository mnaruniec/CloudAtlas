package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import java.util.Collections;
import java.util.Map;

public final class FreshnessInfo {
	private Map<String, Long> zmiTimestamps;

	private FreshnessInfo() {}

	public FreshnessInfo(Map<String, Long> zmiTimestamps) {
		this.zmiTimestamps = zmiTimestamps;
	}

	public Map<String, Long> getZmiTimestamps() {
		return Collections.unmodifiableMap(zmiTimestamps);
	}
}
