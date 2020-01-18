package pl.edu.mimuw.cloudatlas.gtp;

public class GtpUtils {
	private GtpUtils() {}

	public static long getRoundTripDelay(long latestReceive, long latestSend, long oldestReceive, long oldestSend) {
		return (latestReceive - oldestSend) - (latestSend - oldestReceive);
	}

	// says how much remote clock is ahead of local clock
	public static long getTimeOffset(long latestReceive, long latestSend, long rtd) {
		return latestSend + (rtd / 2L) - latestReceive;
	}

	// converts remote timestamp to local time base
	public static long adjustRemoteTimestamp(long timestamp, long dT) {
		return timestamp - dT;
	}
}
