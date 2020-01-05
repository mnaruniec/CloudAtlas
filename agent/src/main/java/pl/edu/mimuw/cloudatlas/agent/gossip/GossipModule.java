package pl.edu.mimuw.cloudatlas.agent.gossip;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;

public class GossipModule extends Module {
	public GossipModule(Bus bus) {
		super(bus);
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_GOSSIP_MODULE_NAME;
	}

	@Override
	public void handleMessage(Message message) {

	}
}
