package pl.edu.mimuw.cloudatlas.agent.api;

import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.agent.NoSuchZoneException;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;


public class AgentAPI implements IAgentAPI {
	private Agent agent;

	public AgentAPI(Agent agent) {
		if (agent == null) {
			throw new IllegalArgumentException();
		}
		this.agent = agent;
	}

	@Override
	public Set<String> getStoredZones() throws RemoteException {
		return agent.getStoredZones();
	}

	@Override
	public Map<String, Value> getZoneAttributes(String zone) throws RemoteException, NoSuchZoneException {
		return agent.getZoneAttributes(zone);
	}

	@Override
	public void upsertZoneAttributes(String zone, Map<String, Value> attributes) throws RemoteException {
		agent.upsertZoneAttributes(zone, attributes);
	}

	@Override
	public void installQuery(String name, String query) throws RemoteException {
		agent.installQuery(name, query);
	}

	@Override
	public void uninstallQuery(String name) throws RemoteException {
		agent.uninstallQuery(name);
	}

	@Override
	public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
		agent.setFallbackContacts(contacts);
	}

	@Override
	public Set<ValueContact> getFallbackContacts() throws RemoteException {
		return agent.getFallbackContacts();
	}
}
