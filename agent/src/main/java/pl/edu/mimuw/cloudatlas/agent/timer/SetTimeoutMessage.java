package pl.edu.mimuw.cloudatlas.agent.timer;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

import java.util.Date;

public class SetTimeoutMessage extends Message implements Comparable<SetTimeoutMessage> {
	public final Runnable callback;
	public final long timeoutMs;
	public final long timestamp;

	public SetTimeoutMessage(String dest, String src, Runnable callback, long timeoutMs, long timestamp) {
		super(dest, src);
		this.callback = callback;
		this.timeoutMs = timeoutMs;
		this.timestamp = timestamp;
	}

	public SetTimeoutMessage(String dest, String src, Runnable callback, long timeoutMs) {
		this(dest, src, callback, timeoutMs, new Date().getTime());
	}

	public long getTargetTimestamp() {
		return timestamp + timeoutMs;
	}

	@Override
	public int compareTo(SetTimeoutMessage message) {
		if (message == null) {
			throw new NullPointerException();
		}
		return Long.compare(this.getTargetTimestamp(), message.getTargetTimestamp());
	}
}
