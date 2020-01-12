package pl.edu.mimuw.cloudatlas.agent.task.messages;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class RefreshAttributeValuesMessage extends Message {
	public RefreshAttributeValuesMessage(String dest, String src) {
		super(dest, src);
	}
}
