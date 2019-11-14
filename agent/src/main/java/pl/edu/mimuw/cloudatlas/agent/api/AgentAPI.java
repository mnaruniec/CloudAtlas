package pl.edu.mimuw.cloudatlas.agent.api;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AgentAPI implements IAgentAPI {
	@Override
	public List<String> getStoredZones() throws RemoteException{
		return null;
	}

	@Override
	public Map<String, Value> getZoneAttributes(String zone) throws RemoteException {
		return null;
	}

	@Override
	public void upsertZoneAttributes(String zone, Map<String, Value> attributes) throws RemoteException {

	}

	@Override
	public void installQuery(String name, String query) throws RemoteException {

	}

	@Override
	public void uninstallQuery(String name) throws RemoteException {

	}

	@Override
	public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {

	}
}
