package pl.edu.mimuw.cloudatlas.agent.common;

public abstract class Module {
	protected Bus bus;

	public Module(Bus bus) {
		this.bus = bus;
	}

	public abstract String getDefaultName();

	public abstract void handleMessage(Message message);
}
