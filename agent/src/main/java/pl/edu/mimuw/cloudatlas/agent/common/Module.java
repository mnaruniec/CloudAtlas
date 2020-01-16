package pl.edu.mimuw.cloudatlas.agent.common;

public abstract class Module {
	protected Bus bus;

	public Module(Bus bus) {
		this.bus = bus;
	}

	public abstract String getName();

	// should be run after all modules are registered, but before executors are started
	public void init() throws Exception {
	}

	public abstract void handleMessage(Message message);
}
