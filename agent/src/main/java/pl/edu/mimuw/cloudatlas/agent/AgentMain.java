package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.comm.CommModule;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.common.ModuleExecutor;
import pl.edu.mimuw.cloudatlas.agent.data.DataModule;
import pl.edu.mimuw.cloudatlas.agent.gossip.GossipModule;
import pl.edu.mimuw.cloudatlas.agent.task.TaskModule;
import pl.edu.mimuw.cloudatlas.agent.timer.TimerModule;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentMain {
	public static void main(String[] args) throws Exception {
//		if (args.length < 1) {
//			System.out.println("usage: agent <config.ini>");
//			System.exit(1);
//		}
//		AgentConfig config = new AgentConfig(new File(args[0]));
		AgentConfig config = new AgentConfig(new File("./config/agent.ini"));

//		if (System.getSecurityManager() == null) {
//			System.setSecurityManager(new SecurityManager());
//		}

		Bus bus = null;
		Module[] modules = null;
		ModuleExecutor[] moduleExecutors = null;
		try {
			bus = new Bus();
			modules = new Module[]{
					new TimerModule(bus),
//					new RmiModule(bus),
					new DataModule(bus, config),
					new CommModule(bus, config),
					new GossipModule(bus, config),
					new TaskModule(bus, config),
			};

			moduleExecutors = new ModuleExecutor[modules.length];
			for (int i = 0; i < modules.length; i++) {
				moduleExecutors[i] = new ModuleExecutor();
				moduleExecutors[i].addModule(modules[i]);
				bus.registerModule(modules[i].getDefaultName(), moduleExecutors[i]);
			}

			for (Module module: modules) {
				module.init();
			}
		} catch (Exception e) {
			System.out.println("Exception in module initialization. Shutting down.");
			e.printStackTrace();
			System.exit(1);
		}

		System.out.println("Starting " + moduleExecutors.length + " executors.");
		ExecutorService executorService = Executors.newFixedThreadPool(moduleExecutors.length);
		for (ModuleExecutor exec : moduleExecutors) {
			executorService.execute(exec);
		}
		System.out.println("Executors started.");

//		bus.sendMessage(new OutNetworkMessage("comm", "main", InetAddress.getByName("127.0.0.1"),
//				new Payload() {}
//				));
		try {
			while (!executorService.awaitTermination(60, TimeUnit.HOURS)) {
				// sleep forever
			}
			System.out.println("All executors finished work.");
		} catch (InterruptedException e) {
			System.out.println("Main thread interrupted.");
		} finally {
			System.out.println("Shutting down.");
			System.exit(0);
		}
	}
}
