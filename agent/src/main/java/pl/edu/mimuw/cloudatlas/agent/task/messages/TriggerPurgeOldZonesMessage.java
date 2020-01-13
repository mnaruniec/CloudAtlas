package pl.edu.mimuw.cloudatlas.agent.task.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class TriggerPurgeOldZonesMessage extends Message {
	public TriggerPurgeOldZonesMessage(String dest, String src) {
		super(dest, src);
	}
}
