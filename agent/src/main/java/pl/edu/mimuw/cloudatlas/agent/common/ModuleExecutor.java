package pl.edu.mimuw.cloudatlas.agent.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ModuleExecutor implements Runnable {
	private BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
	private Map<String, Module> modules = new HashMap<>();

	@Override
	public void run() {
		while (true) {
			try {
				Message msg = queue.take();
				Module mod = modules.get(msg.dest);
				if (mod == null) {
					System.out.println("Executor received message for unknown module: " + msg.dest);
				} else {
					mod.handleMessage(msg);
				}
			} catch (InterruptedException e) {
				System.out.println("Executor interrupted. Shutting down.");
				System.exit(1);
			} catch (Exception e) {
				System.out.println("Unexpected exception in executor. Ignoring.");
				e.printStackTrace();
			}
		}
	}

	public void addModule(Module module) {
		modules.put(module.getName(), module);
	}

	public void pushMessage(Message message) throws InterruptedException {
		queue.put(message);
	}
}
