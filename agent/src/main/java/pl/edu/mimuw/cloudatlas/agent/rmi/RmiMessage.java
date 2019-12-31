package pl.edu.mimuw.cloudatlas.agent.rmi;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public abstract class RmiMessage extends Message {
	public final long requestId;

	public RmiMessage(String dest, String src, long requestId) {
		super(dest, src);
		this.requestId = requestId;
	}
}
