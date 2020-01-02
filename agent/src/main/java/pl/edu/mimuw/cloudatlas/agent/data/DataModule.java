package pl.edu.mimuw.cloudatlas.agent.data;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiSetFallbackContactsMessage;

import java.util.HashSet;

public class DataModule extends Module {
	private DataModel model = new DataModel();

	public DataModule(Bus bus) {
		super(bus);
	}

	@Override
	public String getDefaultName() {
		return Constants.DEFAULT_DATA_MODULE_NAME;
	}

	@Override
	public void handleMessage(Message message) {
		if (message instanceof RmiMessage) {
			handleRmiMessage((RmiMessage) message);
		} else {
			System.out.println("Received unexpected type of message in data module. Ignoring");
		}
	}

	private void handleRmiMessage(RmiMessage message) {
		if (message instanceof RmiGetFallbackContactsRequest) {
			handleRmiGetFallbackContactsRequest((RmiGetFallbackContactsRequest) message);
		} else if (message instanceof RmiSetFallbackContactsMessage) {
			handleRmiSetFallbackContactsMessage((RmiSetFallbackContactsMessage) message);
		} else {
			System.out.println("Received unexpected type of RMI message in data module. Ignoring");
		}
	}

	private void handleRmiGetFallbackContactsRequest(RmiGetFallbackContactsRequest request) {
		bus.sendMessage(new RmiGetFallbackContactsResponse(
				request, new HashSet<>(model.fallbackContacts)
		));
	}

	private void handleRmiSetFallbackContactsMessage(RmiSetFallbackContactsMessage message) {
		model.fallbackContacts = message.fallbackContacts;
	}

}
