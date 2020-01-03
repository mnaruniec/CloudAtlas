package pl.edu.mimuw.cloudatlas.agent.data;

import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.common.Message;
import pl.edu.mimuw.cloudatlas.agent.common.Module;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiSetFallbackContactsMessage;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;

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
		try {
			if (message instanceof RmiGetFallbackContactsRequest) {
				handleRmiGetFallbackContactsRequest((RmiGetFallbackContactsRequest) message);
			} else if (message instanceof RmiSetFallbackContactsMessage) {
				handleRmiSetFallbackContactsMessage((RmiSetFallbackContactsMessage) message);
			} else if (message instanceof RmiGetStoredZonesRequest) {
				handleRmiGetStoredZonesRequest((RmiGetStoredZonesRequest) message);
			} else if (message instanceof RmiGetZoneAttributesRequest) {
				handleRmiGetZoneAttributesRequest((RmiGetZoneAttributesRequest) message);
			} else {
				System.out.println("Received unexpected type of RMI message in data module. Ignoring");
			}
		} catch (RuntimeException e) {
			System.out.println(
					"Data module caught unhandled RuntimeException when handling RmiMessage." +
					"Sending to rmi module."
			);
			bus.sendMessage(new RmiResponse(message, e));
		}
	}

	private void handleRmiGetStoredZonesRequest(RmiGetStoredZonesRequest request) {
		bus.sendMessage(new RmiGetStoredZonesResponse(
				request, new HashSet<>(model.zmiIndex.keySet())
		));
	}

	private void handleRmiGetZoneAttributesRequest(RmiGetZoneAttributesRequest request) {
		RmiResponse response;
		try {
			ZMI zmi = model.zmiIndex.get(new PathName(request.zone).getName());
			if (zmi == null) {
				response = new RmiResponse(
						request,
						new IllegalArgumentException("Zone '" + request.zone + "' not found.")
				);
			} else {
				response = new RmiGetZoneAttributesResponse(
						request,
						zmi.getAttributes().toMap(true)
				);
			}
		} catch (IllegalArgumentException e) {
			response = new RmiResponse(
					request,
					new IllegalArgumentException("'" + request.zone + "' is not a proper zone name.")
			);
		}

		bus.sendMessage(response);
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
