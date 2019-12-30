package pl.edu.mimuw.cloudatlas.agent.timer;

import java.util.Date;
import java.util.PriorityQueue;

public class SleeperThread implements Runnable {
	private final PriorityQueue<SetTimeoutMessage> queue;

	public SleeperThread(PriorityQueue<SetTimeoutMessage> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		try {
			synchronized (queue) {
				while (true) {
					if (queue.isEmpty()) {
						queue.wait();
					} else {
						SetTimeoutMessage msg = queue.peek();
						long now = new Date().getTime();
						if (now < msg.getTargetTimestamp()) {
							queue.wait(msg.getTargetTimestamp() - now);
						} else {
							queue.poll();
							msg.callback.run();
						}
					}
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Timer's sleeper thread interrupted. Shutting down.");
			System.exit(1);
		}
	}
}
