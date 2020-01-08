package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.common.ModuleExecutor;
import pl.edu.mimuw.cloudatlas.agent.data.DataModule;
import pl.edu.mimuw.cloudatlas.agent.rmi.RmiModule;
import pl.edu.mimuw.cloudatlas.agent.timer.TimerModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AgentMain {
	public static void main(String[] args) {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		Bus bus = null;
		Module[] modules = null;
		ModuleExecutor[] moduleExecutors = null;
		try {
			bus = new Bus();
			modules = new Module[]{
					new TimerModule(bus),
					new RmiModule(bus),
					new DataModule(bus),
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
