package pl.edu.mimuw.cloudatlas.fetcher;

import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import pl.edu.mimuw.cloudatlas.agent.rmi.api.IAgentAPI;
import pl.edu.mimuw.cloudatlas.model.Value;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class Fetcher {
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args.length < 1) {
			System.out.println("usage: fetcher <config.ini>");
			System.exit(1);
		}
		Config config = new Config(new File(args[0]));

		OperatingSystemMXBean bean = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
		SystemInfo systemInfo = new SystemInfo();
		DataCollector collector = new DataCollector(bean, systemInfo, config);
		String zoneName = config.getName();
		long collectionIntervalMs = config.getCollectionIntervalMs();

		IAgentAPI api;
		try {
			Registry registry = LocateRegistry.getRegistry();
			api = (IAgentAPI) registry.lookup("AgentAPI");
			api.ping();
		} catch (Exception e) {
			System.out.println("Could not retrieve AgentAPI object: " + e.getMessage());
			System.exit(1);
			return;
		}

		System.out.println("Starting fetcher loop.");

		while (true) {
			Map<String, Value> map = collector.getValueMap();
			try {
				api.upsertZoneAttributes(zoneName, map);
			} catch (Exception e) {
				System.out.println("Failed to upsert values, exception: " + e.getMessage());
			}
			Thread.sleep(collectionIntervalMs);
		}
	}
}
