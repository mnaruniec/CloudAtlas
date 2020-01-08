package pl.edu.mimuw.cloudatlas.agent.comm;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;

public class CommModule extends Module {
	public static final int RECEIVER_PORT = 31337;
	public static final int MAX_DATAGRAM_SIZE = 512;
	public static final int MIN_HEADER_SIZE = 8;
	public static final int FIRST_HEADER_ADDITION = 4;
	public static final int FIRST_HEADER_SIZE = MIN_HEADER_SIZE + FIRST_HEADER_ADDITION;

	public CommModule(Bus bus) {
		super(bus);
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_COMM_MODULE_NAME;
	}

	@Override
	public void handleMessage(Message message) {

	}
}
