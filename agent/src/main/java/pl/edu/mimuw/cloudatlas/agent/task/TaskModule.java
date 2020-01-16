package pl.edu.mimuw.cloudatlas.agent.task;

import pl.edu.mimuw.cloudatlas.agent.AgentConfig;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.ModuleNames;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.task.messages.PurgeOldZonesMessage;
import pl.edu.mimuw.cloudatlas.agent.task.messages.RefreshAttributeValuesMessage;
import pl.edu.mimuw.cloudatlas.agent.task.messages.TriggerPurgeOldZonesMessage;
import pl.edu.mimuw.cloudatlas.agent.task.messages.TriggerRefreshAttributeValuesMessage;
import pl.edu.mimuw.cloudatlas.agent.timer.SetTimeoutMessage;

import java.util.Date;

public class TaskModule extends Module {
	private long refreshIntervalMs;
	private long purgeIntervalMs;

	public TaskModule(Bus bus, AgentConfig config) {
		super(bus);
		this.refreshIntervalMs = config.getZmiRefreshIntervalMs();
		this.purgeIntervalMs = config.getZmiPurgeIntervalMs();
	}

	@Override
	public String getName() {
		return ModuleNames.TASK_MODULE_NAME;
	}

	@Override
	public void init() {
		setRefreshTimeout();
		// TODO
		setPurgeTimeout();
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof TriggerRefreshAttributeValuesMessage) {
			handleTriggerRefreshAttributeValuesMessage((TriggerRefreshAttributeValuesMessage) message);
		} else if (message instanceof TriggerPurgeOldZonesMessage) {
			handleTriggerPurgeOldZonesMessage((TriggerPurgeOldZonesMessage) message);
		} else {
			System.out.println("Task module received message of unhandled type. Ignore.");
		}
	}

	private void handleTriggerRefreshAttributeValuesMessage(TriggerRefreshAttributeValuesMessage message) {
		bus.sendMessage(new RefreshAttributeValuesMessage(
				ModuleNames.DATA_MODULE_NAME,
				getName()
		));

		setRefreshTimeout();
	}

	private void handleTriggerPurgeOldZonesMessage(TriggerPurgeOldZonesMessage message) {
		long timestamp = new Date().getTime() - purgeIntervalMs;

		bus.sendMessage(new PurgeOldZonesMessage(
				ModuleNames.DATA_MODULE_NAME,
				getName(),
				timestamp
		));

		setPurgeTimeout();
	}

	private void setRefreshTimeout() {
		Runnable sendTrigger = () -> {
			bus.sendMessage(new TriggerRefreshAttributeValuesMessage(
					getName(),
					ModuleNames.TIMER_MODULE_NAME
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				ModuleNames.TIMER_MODULE_NAME,
				getName(),
				sendTrigger,
				refreshIntervalMs
		));
	}

	private void setPurgeTimeout() {
		Runnable sendTrigger = () -> {
			bus.sendMessage(new TriggerPurgeOldZonesMessage(
					getName(),
					ModuleNames.TIMER_MODULE_NAME
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				ModuleNames.TIMER_MODULE_NAME,
				getName(),
				sendTrigger,
				purgeIntervalMs
		));
	}
}
