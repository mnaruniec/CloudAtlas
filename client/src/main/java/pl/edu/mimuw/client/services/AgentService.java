package pl.edu.mimuw.client.services;

import org.springframework.stereotype.Service;
import pl.edu.mimuw.cloudatlas.agent.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

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
	private List<String> storedZones = new ArrayList<>();

	public AgentService() throws Exception {
		Registry registry = LocateRegistry.getRegistry();
		agentAPI = (IAgentAPI) registry.lookup("AgentAPI");
		storedZones.addAll(agentAPI.getStoredZones());
		storedZones.sort(Comparator.naturalOrder());
	}

	@Override
	public Set<String> getStoredZones() throws RemoteException {
		return agentAPI.getStoredZones();
	}

	public List<String> getStoredZonesList() {
		return storedZones;
	}

	@Override
	public Map<String, Value> getZoneAttributes(String zone, boolean excludeQueries)
			throws RemoteException {
		return agentAPI.getZoneAttributes(zone, excludeQueries);
	}

	@Override
	public void upsertZoneAttributes(String zone, Map<String, Value> attributes)
			throws RemoteException {
		agentAPI.upsertZoneAttributes(zone, attributes);
	}

	@Override
	public void installQuery(String name, String query)
			throws RemoteException {
		agentAPI.installQuery(name, query);
	}

	@Override
	public void uninstallQuery(String name) throws RemoteException {
		agentAPI.uninstallQuery(name);
	}

	@Override
	public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
		agentAPI.setFallbackContacts(contacts);
	}

	@Override
	public Set<ValueContact> getFallbackContacts() throws RemoteException {
		return agentAPI.getFallbackContacts();
	}
}
