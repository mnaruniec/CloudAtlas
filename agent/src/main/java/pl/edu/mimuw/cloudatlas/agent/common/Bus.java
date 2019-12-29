package pl.edu.mimuw.cloudatlas.agent.common;

import java.util.HashMap;
import java.util.Map;

public class Bus {
	private Map<String, ModuleExecutor> executors = new HashMap<>();

	public void sendMessage(Message message) throws InterruptedException {
		ModuleExecutor exec = executors.get(message.dest);
		if (exec == null) {
			System.out.println("Executor for dest module '" + message.dest + "' not found.");
		} else {
			exec.pushMessage(message);
		}
	}

	public void registerModule(String name, ModuleExecutor executor) {
		executors.put(name, executor);
	}
}
