package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.agent.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueInt;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Fetcher {
	public static void main(String[] args) {
		System.out.println("Running fetcher");
		try {
			Registry registry = LocateRegistry.getRegistry();
			IAgentAPI api = (IAgentAPI) registry.lookup("AgentAPI");

			System.out.println(api.getZoneAttributes("/uw/violet07", true));
			System.out.println(api.getZoneAttributes("/uw/violet07", false));

			System.out.println(api.getZoneAttributes("/uw", true));
			System.out.println(api.getZoneAttributes("/uw", false));

//			api.installQuery("jakies_query", "SELECT sum(cardinality) AS cardinality");
			Map<String, Value> attrs = new HashMap<>();
			attrs.put("cardinality", new ValueInt(3L));
			api.upsertZoneAttributes("/uw/violet07", attrs);

			System.out.println("\n\n");

			System.out.println(api.getZoneAttributes("/uw/violet07", true));
			System.out.println(api.getZoneAttributes("/uw/violet07", false));

			System.out.println(api.getZoneAttributes("/uw", true));
			System.out.println(api.getZoneAttributes("/uw", false));

		} catch (Exception e) {
			System.err.println("Fetcher exception:");
			e.printStackTrace();
		}
	}
}
