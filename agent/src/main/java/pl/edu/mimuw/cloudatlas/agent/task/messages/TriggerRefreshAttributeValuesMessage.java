package pl.edu.mimuw.cloudatlas.agent.task.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class TriggerRefreshAttributeValuesMessage extends Message {
	public TriggerRefreshAttributeValuesMessage(String dest, String src) {
		super(dest, src);
	}
}
