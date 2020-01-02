package pl.edu.mimuw.cloudatlas.agent.rmi;

import pl.edu.mimuw.cloudatlas.agent.rmi.exceptions.IllegalAttributeException;
import pl.edu.mimuw.cloudatlas.agent.rmi.exceptions.NoSuchZoneException;
import pl.edu.mimuw.cloudatlas.agent.rmi.exceptions.QueryParsingException;
import pl.edu.mimuw.cloudatlas.agent.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.Constants;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiSetFallbackContactsMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUpsertZoneAttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUpsertZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AgentAPI implements IAgentAPI {
	public static final long FUTURE_TIMEOUT_MS = 60000;

	private final Bus bus;

	private final AtomicLong nextRequestId = new AtomicLong(0);
	private final Map<Long, CompletableFuture<RmiMessage>> futureMap = new ConcurrentHashMap<>();

	public AgentAPI(Bus bus) {
		this.bus = bus;
	}

	@Override
	public Set<String> getStoredZones() throws RemoteException {
		RmiMessage request = new RmiGetStoredZonesRequest(
				Constants.DEFAULT_DATA_MODULE_NAME,
				Constants.DEFAULT_RMI_MODULE_NAME,
				getNextRequestId()
		);
		return sendAndReceive(request, RmiGetStoredZonesResponse.class).zones;
	}

	@Override
	public Map<String, Value> getZoneAttributes(String zone, boolean excludeQueries) throws RemoteException, NoSuchZoneException {
		// TODO - consider asserting non-null
		RmiMessage request = new RmiGetZoneAttributesRequest(
				Constants.DEFAULT_DATA_MODULE_NAME,
				Constants.DEFAULT_RMI_MODULE_NAME,
				getNextRequestId(),
				zone
		);

		RmiGetZoneAttributesResponse response = sendAndReceive(request, RmiGetZoneAttributesResponse.class);
		if (response.attributes == null) {
			throw new NoSuchZoneException("Zone '" + zone + "' not found.");
		}
		return response.attributes;
	}

	@Override
	public void upsertZoneAttributes(String zone, Map<String, Value> attributes) throws RemoteException, NoSuchZoneException, IllegalAttributeException {
		// TODO - consider asserting non-null
		// TODO - consider omitting response
		RmiMessage request = new RmiUpsertZoneAttributesMessage(
				Constants.DEFAULT_DATA_MODULE_NAME,
				Constants.DEFAULT_RMI_MODULE_NAME,
				getNextRequestId(),
				zone,
				attributes
		);

		RmiUpsertZoneAttributesResponse response = sendAndReceive(request, RmiUpsertZoneAttributesResponse.class);
		if (response.errorType == RmiUpsertZoneAttributesResponse.ErrorType.NoSuchZone) {
			throw new NoSuchZoneException("Zone '" + zone + "' not found.");
		}
		if (response.errorType == RmiUpsertZoneAttributesResponse.ErrorType.IllegalAttribute) {
			throw new IllegalAttributeException("Tried to modify special attribute: '" + response.attribute + "'.");
		}
	}

	@Override
	public void installQuery(String name, String query) throws RemoteException, IllegalAttributeException, QueryParsingException {
		// TODO
	}

	@Override
	public void uninstallQuery(String name) throws RemoteException {
		// TODO
	}

	@Override
	public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
		if (contacts == null) {
			throw new NullPointerException("Fallback contacts to set are null.");
		}
		RmiMessage message = new RmiSetFallbackContactsMessage(
				Constants.DEFAULT_DATA_MODULE_NAME,
				Constants.DEFAULT_RMI_MODULE_NAME,
				getNextRequestId(),
				contacts
		);

		bus.sendMessage(message);
	}

	@Override
	public Set<ValueContact> getFallbackContacts() throws RemoteException {
		RmiMessage request = new RmiGetFallbackContactsRequest(
				Constants.DEFAULT_DATA_MODULE_NAME,
				Constants.DEFAULT_RMI_MODULE_NAME,
				getNextRequestId()
		);
		return sendAndReceive(request, RmiGetFallbackContactsResponse.class).fallbackContacts;
	}

	public void registerResponse(RmiMessage message) {
		long requestId = message.requestId;
		CompletableFuture<RmiMessage> future = futureMap.get(requestId);
		if (future == null) {
			System.out.println(
					"Rmi module received response for non-existing request "
					+ requestId + ". Ignoring."
			);
		}
		if (!future.complete(message)) {
			System.out.println(
					"Rmi request " + requestId + " received second response. Ignoring."
			);
		}
	}

	private long getNextRequestId() {
		return nextRequestId.incrementAndGet();
	}

	private <T extends RmiMessage> T sendAndReceive(RmiMessage request, Class<T> responseType) throws RemoteException {
		long id = request.requestId;
		CompletableFuture<RmiMessage> future = new CompletableFuture<>();

		futureMap.put(id, future);
		this.bus.sendMessage(request);

		RmiMessage result = null;
		try {
			result = future.get(FUTURE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception in RMI module. Shutting down.");
			System.exit(1);
		} catch (Exception e) {
			System.out.println("Future execution exception in RMI module. Returning exception.");
			e.printStackTrace();
		} finally {
			futureMap.remove(id);
		}

		if (!responseType.isInstance(result)) {
			System.out.println("Rmi module received a response of unexpected type. Returning exception.");
			result = null;
		}

		if (result == null) {
			throw new RemoteException("Internal error when processing request.");
		}

		return responseType.cast(result);
	}
}
