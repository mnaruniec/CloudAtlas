package pl.edu.mimuw.client.services;

import org.springframework.stereotype.Service;
import pl.edu.mimuw.cloudatlas.agent.rmi.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedInstallation;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AgentService implements IAgentAPI {
	private IAgentAPI agentAPI;
	private String hostname = "localhost";

	public AgentService() {
		try {
			fetchAgentAPI();
		} catch (RemoteException e) {
			// skip
		}
	}

	public synchronized void setHost(String hostname) throws RemoteException {
		if (this.hostname.equals(hostname)) {
			throw new IllegalArgumentException("Hostname " + hostname + " is already being used.");
		}
		this.hostname = hostname;
		agentAPI = null;
		try {
			fetchAgentAPI();
		} catch (Exception e) {
			String message =
					"Failed to immediately connect to the agent. Will be retried in the background.";
			System.out.println(message);
			throw new RemoteException(message, e);
		}
	}

	public String getHost() {
		return hostname;
	}

	private synchronized IAgentAPI fetchAgentAPI() throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			agentAPI = (IAgentAPI) registry.lookup("AgentAPI");
			agentAPI.ping();
			return agentAPI;
		} catch (Exception e) {
			agentAPI = null;
			throw new RemoteException("Failed to connect to the agent.", e);
		}
	}

	@Override
	public Set<String> getStoredZones() throws RemoteException {
		return fetchAgentAPI().getStoredZones();
	}

	public List<String> getStoredZonesList() throws RemoteException {
		List<String> result = new ArrayList<>(getStoredZones());
		result.sort(Comparator.naturalOrder());
		return result;
	}

	@Override
	public Map<String, Value> getZoneAttributes(String zone)
			throws RemoteException {
		return fetchAgentAPI().getZoneAttributes(zone);
	}

	@Override
	public void upsertZoneAttributes(String zone, Map<String, Value> attributes)
			throws RemoteException {
		fetchAgentAPI().upsertZoneAttributes(zone, attributes);
	}

	@Override
	public void installQuery(SignedInstallation signedInstallation)
			throws RemoteException {
		fetchAgentAPI().installQuery(signedInstallation);
	}

	@Override
	public void uninstallQuery(SignedUninstallation signedUninstallation)
			throws RemoteException {
		fetchAgentAPI().uninstallQuery(signedUninstallation);
	}

	@Override
	public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
		fetchAgentAPI().setFallbackContacts(contacts);
	}

	@Override
	public Set<ValueContact> getFallbackContacts() throws RemoteException {
		return fetchAgentAPI().getFallbackContacts();
	}

	@Override
	public void ping() throws RemoteException {
		fetchAgentAPI().ping();
	}
}
