package pl.edu.mimuw.cloudatlas.agent.rmi;

import pl.edu.mimuw.cloudatlas.agent.rmi.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.agent.common.Bus;
import pl.edu.mimuw.cloudatlas.agent.common.ModuleNames;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetFallbackContactsResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetStoredZonesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiGetZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiInstallQueryRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiInstallQueryResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiSetFallbackContactsMessage;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUninstallQueryRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUninstallQueryResponse;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUpsertZoneAttributesRequest;
import pl.edu.mimuw.cloudatlas.agent.rmi.messages.RmiUpsertZoneAttributesResponse;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.RMI_MODULE_NAME,
				getNextRequestId()
		);
		return sendAndReceive(request, RmiGetStoredZonesResponse.class).zones;
	}

	@Override
	public Map<String, Value> getZoneAttributes(String zone) throws RemoteException {
		RmiMessage request = new RmiGetZoneAttributesRequest(
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.RMI_MODULE_NAME,
				getNextRequestId(),
				zone
		);
		return sendAndReceive(request, RmiGetZoneAttributesResponse.class).attributes;
	}

	@Override
	public void upsertZoneAttributes(String zone, Map<String, Value> attributes) throws RemoteException {
		RmiMessage request = new RmiUpsertZoneAttributesRequest(
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.RMI_MODULE_NAME,
				getNextRequestId(),
				zone,
				attributes
		);
		// exception is thrown transparently
		sendAndReceive(request, RmiUpsertZoneAttributesResponse.class);
	}

	@Override
	public void installQuery(SignedInstallation signedInstallation) throws RemoteException {
		RmiMessage request = new RmiInstallQueryRequest(
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.RMI_MODULE_NAME,
				getNextRequestId(),
				signedInstallation
		);

		// exception is thrown transparently
		sendAndReceive(request, RmiInstallQueryResponse.class);
	}

	@Override
	public void uninstallQuery(SignedUninstallation signedUninstallation) throws RemoteException {
		RmiMessage request = new RmiUninstallQueryRequest(
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.RMI_MODULE_NAME,
				getNextRequestId(),
				signedUninstallation
		);

		// exception is thrown transparently
		sendAndReceive(request, RmiUninstallQueryResponse.class);
	}

	@Override
	public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
		if (contacts == null) {
			throw new NullPointerException("Fallback contacts to set are null.");
		}
		RmiMessage message = new RmiSetFallbackContactsMessage(
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.RMI_MODULE_NAME,
				getNextRequestId(),
				contacts
		);

		bus.sendMessage(message);
	}

	@Override
	public Set<ValueContact> getFallbackContacts() throws RemoteException {
		RmiMessage request = new RmiGetFallbackContactsRequest(
				ModuleNames.DATA_MODULE_NAME,
				ModuleNames.RMI_MODULE_NAME,
				getNextRequestId()
		);
		return sendAndReceive(request, RmiGetFallbackContactsResponse.class).fallbackContacts;
	}

	public void registerResponse(RmiResponse response) {
		long requestId = response.requestId;
		CompletableFuture<RmiMessage> future = futureMap.get(requestId);
		if (future == null) {
			System.out.println(
					"Rmi module received response for non-existing request "
					+ requestId + ". Ignoring."
			);
		} else if (response.exception != null) {
			if (!future.completeExceptionally(response.exception)) {
				System.out.println(
						"Rmi request " + requestId + " received second response (an exception). Ignoring."
				);
			}
		} else if (!future.complete(response)) {
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
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
//				System.out.println("RuntimeException in RMI module. Rethrowing to client.");
				throw (RuntimeException) cause;
			} else {
				System.out.println("Unexpected future ExecutionException in RMI module. Throwing RemoteException..");
				e.printStackTrace();
			}
		} catch (RuntimeException e) {
			System.out.println("RuntimeException in RMI module. Rethrowing to client.");
			throw e;
		} catch (TimeoutException e) {
			System.out.println("Future TimeoutException in RMI module. Throwing RemoteException.");
		} finally {
			futureMap.remove(id);
		}

		if (!responseType.isInstance(result)) {
			System.out.println("Rmi module received a response of unexpected type. Throwing RemoteException.");
			result = null;
		}

		if (result == null) {
			throw new RemoteException("Internal error when processing request.");
		}

		return responseType.cast(result);
	}

	@Override
	public void ping() throws RemoteException {}
}
