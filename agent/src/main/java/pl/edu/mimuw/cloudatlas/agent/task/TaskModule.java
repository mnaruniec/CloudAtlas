package pl.edu.mimuw.cloudatlas.agent.task;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.task.messages.PurgeOldZonesMessage;
import pl.edu.mimuw.cloudatlas.agent.task.messages.RefreshAttributeValuesMessage;
import pl.edu.mimuw.cloudatlas.agent.task.messages.TriggerPurgeOldZonesMessage;
import pl.edu.mimuw.cloudatlas.agent.task.messages.TriggerRefreshAttributeValuesMessage;
import pl.edu.mimuw.cloudatlas.agent.timer.SetTimeoutMessage;

import java.util.Date;

public class TaskModule extends Module {
	// TODO - config
	public static final long REFRESH_TIMEOUT_MS = 5000;
	public static final long PURGE_TIMEOUT_MS = 60000;

	public TaskModule(Bus bus) {
		super(bus);
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_TASK_MODULE_NAME;
	}

	@Override
	public void init() {
		setRefreshTimeout();
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
				Constants.DEFAULT_DATA_MODULE_NAME,
				getDefaultName()
		));

		setRefreshTimeout();
	}

	private void handleTriggerPurgeOldZonesMessage(TriggerPurgeOldZonesMessage message) {
		long timestamp = new Date().getTime() - PURGE_TIMEOUT_MS;

		bus.sendMessage(new PurgeOldZonesMessage(
				Constants.DEFAULT_DATA_MODULE_NAME,
				getDefaultName(),
				timestamp
		));

		setPurgeTimeout();
	}

	private void setRefreshTimeout() {
		Runnable sendTrigger = () -> {
			bus.sendMessage(new TriggerRefreshAttributeValuesMessage(
					getDefaultName(),
					Constants.DEFAULT_TIMER_MODULE_NAME
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				Constants.DEFAULT_TIMER_MODULE_NAME,
				getDefaultName(),
				sendTrigger,
				REFRESH_TIMEOUT_MS
		));
	}

	private void setPurgeTimeout() {
		Runnable sendTrigger = () -> {
			bus.sendMessage(new TriggerPurgeOldZonesMessage(
					getDefaultName(),
					Constants.DEFAULT_TIMER_MODULE_NAME
			));
		};

		bus.sendMessage(new SetTimeoutMessage(
				Constants.DEFAULT_TIMER_MODULE_NAME,
				getDefaultName(),
				sendTrigger,
				PURGE_TIMEOUT_MS
		));
	}
}
