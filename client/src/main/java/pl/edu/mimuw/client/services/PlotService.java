package pl.edu.mimuw.client.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.agent.NoSuchZoneException;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PlotService {
	public static final int SAMPLE_CAPACITY = 10000;
	public static final int COLLECTION_INTERVAL_MS = 5000;
	public static final DateFormat TIME_FORMAT = new SimpleDateFormat("MM/dd HH:mm:ss");


	private static class TimedValue {
		private String time;
		private Value value;

		public TimedValue(String time, Value value) {
			this.time = time;
			this.value = value;
		}
	}

	private AgentService agentService;
	private ObjectMapper mapper;

	// zone -> attr -> List<TimedValue>
	private Map<String, Map<String, List<TimedValue>>> plotMap = new HashMap<>();

	public PlotService(@Autowired AgentService agentService, @Autowired ObjectMapper mapper) {
		this.agentService = agentService;
		this.mapper = mapper;

		for (String zone: agentService.getStoredZonesList()) {
			plotMap.put(zone, new HashMap<>());
		}
	}

	public JsonNode getPlotData(String zone, String attribute) {
		ObjectNode root = mapper.createObjectNode();
		if (plotMap.get(zone) == null) {
			root.put("error", "Zone not found.");
			return root;
		}
		if (plotMap.get(zone).get(attribute) == null) {
			root.put("error", "Attribute not found for this zone.");
			return root;
		}
		List<TimedValue> timeList = plotMap.get(zone).get(attribute);

		ArrayNode data = mapper.createArrayNode();
		ArrayNode data_type = mapper.createArrayNode();
		ArrayNode label = mapper.createArrayNode();

		for (TimedValue timedValue: timeList) {
			label.add(timedValue.time);
			if (timedValue.value == null) {
				data.add((JsonNode)null);
				data_type.add((JsonNode)null);
			} else {
				switch (timedValue.value.getType().getPrimaryType()) {
					case DOUBLE:
						data.add(((ValueDouble)timedValue.value).getValue());
						break;
					case DURATION:
						data.add(((ValueDuration)timedValue.value).getValue());
						break;
					case INT:
						data.add(((ValueInt)timedValue.value).getValue());
						break;
					default:
						data.add((JsonNode)null);
				}
				data_type.add(timedValue.value.getType().toString());
			}
		}

		root.set("data", data);
		root.set("data_type", data_type);
		root.set("label", label);
		return root;
	}

	@Scheduled(fixedDelay = COLLECTION_INTERVAL_MS, initialDelay = 1000)
	public void update() {
		List<String> zones = agentService.getStoredZonesList();

		for (String zone: zones) {
			Map<String, List<TimedValue>> attrMap = plotMap.get(zone);

			String now = TIME_FORMAT.format(new Date());
			Map<String, Value> receivedMap;
			try {
				receivedMap = agentService.getZoneAttributes(zone, true);
			} catch (Exception e) {
				System.out.println("EXCEPTION: " + e.getMessage());
				e.printStackTrace();
				// we just skip the read if failed
				continue;
			}
			synchronized (attrMap) {
				for (Map.Entry<String, Value> newEntry: receivedMap.entrySet()) {
					List<TimedValue> timeList = attrMap.computeIfAbsent(
							newEntry.getKey(), k -> new LinkedList<>()
					);

					Value value = newEntry.getValue();
					value = value == null || value.isNull() || !value.isNumeric() ?
							null : value;

					timeList.add(new TimedValue(now, value));
					if (timeList.size() > SAMPLE_CAPACITY) {
						timeList.remove(0);
					}
				}

				// remove old elements
				for (String attr: attrMap.keySet()) {
					if (!receivedMap.containsKey(attr)) {
						attrMap.remove(attr);
					}
				}
			}
		}
	}
}
