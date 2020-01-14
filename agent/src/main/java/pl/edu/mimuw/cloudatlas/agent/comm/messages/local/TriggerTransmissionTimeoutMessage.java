package pl.edu.mimuw.cloudatlas.agent.comm.messages.local;

import pl.edu.mimuw.cloudatlas.agent.comm.receiver.TransmissionId;
import pl.edu.mimuw.cloudatlas.agent.common.Message;

public class TriggerTransmissionTimeoutMessage extends Message {
	public final TransmissionId transmissionId;

	public TriggerTransmissionTimeoutMessage(String dest, String src, TransmissionId transmissionId) {
		super(dest, src);
		this.transmissionId = transmissionId;
	}
}
