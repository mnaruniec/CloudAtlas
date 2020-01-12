package pl.edu.mimuw.cloudatlas.agent.task.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class PurgeOldZonesMessage extends Message {
	public final long timestamp;

	public PurgeOldZonesMessage(String dest, String src, long timestamp) {
		super(dest, src);
		this.timestamp = timestamp;
	}
}
