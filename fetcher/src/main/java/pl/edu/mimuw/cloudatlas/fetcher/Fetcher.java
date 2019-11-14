package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.agent.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueContact;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Set;

public class Fetcher {
	public static void main(String[] args) {
		System.out.println("Running fetcher");
		try {
			Registry registry = LocateRegistry.getRegistry();
			IAgentAPI api = (IAgentAPI) registry.lookup("AgentAPI");

			System.out.println(api.getStoredZones());

			System.out.println(api.getZoneAttributes("/uw/violet07"));

			Set<ValueContact> cont = new HashSet<>();
			cont.add(new ValueContact(new PathName("/some/host"), InetAddress.getByAddress(new byte[] {
					1, 3, 3, 7
			})));
			api.setFallbackContacts(cont);

			System.out.println(api.getFallbackContacts());

		} catch (Exception e) {
			System.err.println("Fetcher exception:");
			e.printStackTrace();
		}
	}
}
