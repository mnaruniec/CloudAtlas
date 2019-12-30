package pl.edu.mimuw.cloudatlas.agent.timer;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;

import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimerModule extends Module {
	private final PriorityQueue<SetTimeoutMessage> queue = new PriorityQueue<>();
	private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	public TimerModule(Bus bus) {
		super(bus);
		executorService.execute(new SleeperThread(queue));
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_TIMER_NAME;
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof SetTimeoutMessage) {
			handleSetTimeoutMessage((SetTimeoutMessage) message);
		} else {
			System.out.println("Unknown type of message received by timer module.");
		}
	}

	private void handleSetTimeoutMessage(SetTimeoutMessage message) {
		synchronized (queue) {
			queue.add(message);
			queue.notifyAll();
		}
	}
}
