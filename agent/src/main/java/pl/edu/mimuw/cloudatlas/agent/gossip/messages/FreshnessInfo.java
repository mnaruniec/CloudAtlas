package pl.edu.mimuw.cloudatlas.agent.gossip.messages;

import pl.edu.mimuw.cloudatlas.gtp.GtpUtils;

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

	// all query timestamps are in signer's base, no need to adjust
	public void adjustRemoteTimestamps(long dT) {
		zmiTimestamps.replaceAll((k, v) -> GtpUtils.adjustRemoteTimestamp(v, dT));
	}
}
