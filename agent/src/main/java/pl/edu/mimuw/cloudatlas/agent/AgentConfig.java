package pl.edu.mimuw.cloudatlas.agent;

import org.apache.commons.validator.routines.InetAddressValidator;
import pl.edu.mimuw.cloudatlas.config.IniConfig;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class AgentConfig extends IniConfig {
	public enum GossipTargetStrategy {
		UNIFORM_ROUND_ROBIN,
		EXP_ROUND_ROBIN,
		UNIFORM_RANDOM,
		EXP_RANDOM
	}

	public static final String GENERAL_SECTION = "general";
	public static final String GOSSIP_SECTION = "gossip";
	public static final String CRYPTO_SECTION = "crypto";
	public static final String ZMI_SECTION = "zmi";
	public static final String UDP_SECTION = "udp";

	// GENERAL
	public static final String PATHNAME = "pathname";
	public static final String IP = "ip";

	// GOSSIP
	public static final String GOSSIP_INTERVAL_MS = "gossip_interval_ms";
	public static final long DEFAULT_GOSSIP_INTERVAL_MS = 5000L;

	public static final String GOSSIP_TIMEOUT_MS = "gossip_timeout_ms";
	public static final long DEFAULT_GOSSIP_TIMEOUT_MS = 5000L;

	public static final String GOSSIP_RETRY_INTERVAL_MS = "gossip_retry_interval_ms";
	public static final long DEFAULT_GOSSIP_RETRY_INTERVAL_MS = 2000L;

	public static final String GOSSIP_RETRY_LIMIT = "gossip_retry_limit";
	public static final int DEFAULT_GOSSIP_RETRY_LIMIT = 2;

	public static final String GOSSIP_TARGET_STRATEGY = "gossip_target_strategy";
	public static final String DEFAULT_GOSSIP_TARGET_STRATEGY = "uniform_random";

	// CRYPTO
	public static final String PUBLIC_KEY_PATH = "public_key_path";

	// ZMI
	public static final String ZMI_REFRESH_INTERVAL_MS = "refresh_interval_ms";
	public static final long DEFAULT_ZMI_REFRESH_INTERVAL_MS = 5000L;

	public static final String ZMI_PURGE_INTERVAL_MS = "purge_interval_ms";
	public static final long DEFAULT_ZMI_PURGE_INTERVAL_MS = 60000L;

	// UDP
	public static final String UDP_RECEIVE_TIMEOUT_MS = "receive_timeout_ms";
	public static final long DEFAULT_UDP_RECEIVE_TIMEOUT_MS = 5000L;

	public AgentConfig(File file) throws IOException {
		super(file);
	}

	public PathName getPathname() {
		PathName pathName = new PathName(getValueOrThrow(GENERAL_SECTION, PATHNAME, String.class).trim());
		if (pathName.equals(PathName.ROOT)) {
			throw new IllegalArgumentException("Node's pathname cannot be root.");
		}
		return pathName;
	}

	public InetAddress getIP() {
		String str = getValueOrThrow(GENERAL_SECTION, IP, String.class).trim();
		if (!InetAddressValidator.getInstance().isValid(str)) {
			throw new IllegalArgumentException("String '" + str + "' is not a valid IP address.");
		}
		try {
			return InetAddress.getByName(str);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	public Long getGossipIntervalMs() {
		return getValue(GOSSIP_SECTION, GOSSIP_INTERVAL_MS, Long.class, DEFAULT_GOSSIP_INTERVAL_MS);
	}

	public Long getGossipTimeoutMs() {
		return getValue(GOSSIP_SECTION, GOSSIP_TIMEOUT_MS, Long.class, DEFAULT_GOSSIP_TIMEOUT_MS);
	}

	public Long getGossipRetryIntervalMs() {
		return getValue(GOSSIP_SECTION, GOSSIP_RETRY_INTERVAL_MS, Long.class, DEFAULT_GOSSIP_RETRY_INTERVAL_MS);
	}

	public Integer getGossipRetryLimit() {
		return getValue(GOSSIP_SECTION, GOSSIP_RETRY_LIMIT, Integer.class, DEFAULT_GOSSIP_RETRY_LIMIT);
	}

	public GossipTargetStrategy getGossipTargetStrategy() {
		String str = getValue(GOSSIP_SECTION, GOSSIP_TARGET_STRATEGY, String.class, DEFAULT_GOSSIP_TARGET_STRATEGY);
		return GossipTargetStrategy.valueOf(str.toUpperCase());
	}

	public String getPublicKeyPath() {
		return getValueOrThrow(CRYPTO_SECTION, PUBLIC_KEY_PATH, String.class);
	}

	public Long getZmiRefreshIntervalMs() {
		return getValue(ZMI_SECTION, ZMI_REFRESH_INTERVAL_MS, Long.class, DEFAULT_ZMI_REFRESH_INTERVAL_MS);
	}

	public Long getZmiPurgeIntervalMs() {
		return getValue(ZMI_SECTION, ZMI_PURGE_INTERVAL_MS, Long.class, DEFAULT_ZMI_PURGE_INTERVAL_MS);
	}

	public Long getUdpReceiveTimeoutMs() {
		return getValue(UDP_SECTION, UDP_RECEIVE_TIMEOUT_MS, Long.class, DEFAULT_UDP_RECEIVE_TIMEOUT_MS);
	}
}
