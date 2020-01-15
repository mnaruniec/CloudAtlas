package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import java.util.Collections;
import java.util.Map;

public final class FreshnessInfo {
	private Map<String, Long> zmiTimestamps;
	private Map<String, Long> queryTimestamps;

	private FreshnessInfo() {}

	public FreshnessInfo(Map<String, Long> zmiTimestamps, Map<String, Long> queryTimestamps) {
		this.zmiTimestamps = zmiTimestamps;
		this.queryTimestamps = queryTimestamps;
	}

	public Map<String, Long> getZmiTimestamps() {
		return Collections.unmodifiableMap(zmiTimestamps);
	}

	public Map<String, Long> getQueryTimestamps() {
		return Collections.unmodifiableMap(queryTimestamps);
	}
}
