package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.agent.api.IAgentAPI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Fetcher {
	public static void main(String[] args) {
		System.out.println("Running fetcher");
		try {
			Registry registry = LocateRegistry.getRegistry();
			IAgentAPI api = (IAgentAPI) registry.lookup("AgentAPI");
			System.out.println(api.getStoredZones());
		} catch (Exception e) {
			System.err.println("Fetcher exception:");
			e.printStackTrace();
		}
	}
}
