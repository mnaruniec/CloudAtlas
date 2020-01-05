package pl.edu.mimuw.cloudatlas.agent.comm;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;

public class CommModule extends Module {
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
