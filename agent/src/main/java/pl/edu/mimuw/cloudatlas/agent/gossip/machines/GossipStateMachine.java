package pl.edu.mimuw.cloudatlas.agent.gossip.machines;

import pl.edu.mimuw.cloudatlas.agent.common.Message;

public interface GossipStateMachine {
	void handleMessage(Message message);

	boolean isFinished();
}
