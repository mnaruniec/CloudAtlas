package pl.edu.mimuw.client.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueDouble;
import pl.edu.mimuw.cloudatlas.model.ValueDuration;
import pl.edu.mimuw.cloudatlas.model.ValueInt;

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
		private final String time;
		private final Value value;

		public TimedValue(String time, Value value) {
			this.time = time;
			this.value = value;
		}
	}

	private AgentService agentService;
	private ObjectMapper mapper;

	// zone -> attr -> List<TimedValue>
	private final Map<String, Map<String, List<TimedValue>>> plotMap = new HashMap<>();

	public PlotService(@Autowired AgentService agentService, @Autowired ObjectMapper mapper) {
		this.agentService = agentService;
		this.mapper = mapper;
	}

	public JsonNode getPlotData(String zone, String attribute) {
		ObjectNode root = mapper.createObjectNode();
		ArrayNode data = mapper.createArrayNode();
		ArrayNode data_type = mapper.createArrayNode();
		ArrayNode label = mapper.createArrayNode();

		Map<String, List<TimedValue>> attrMap;
		synchronized (plotMap) {
			attrMap = plotMap.get(zone);
			if (attrMap == null) {
				root.put("error", "Zone not found.");
				return root;
			}
		}
		synchronized (attrMap) {
			List<TimedValue> timeList = attrMap.get(attribute);
			if (timeList == null) {
				root.put("error", "Attribute not found for this zone.");
				return root;
			}

			for (TimedValue timedValue : timeList) {
				label.add(timedValue.time);
				if (timedValue.value == null) {
					data.add((JsonNode) null);
					data_type.add((JsonNode) null);
				} else {
					switch (timedValue.value.getType().getPrimaryType()) {
						case DOUBLE:
							data.add(((ValueDouble) timedValue.value).getValue());
							break;
						case DURATION:
							data.add(((ValueDuration) timedValue.value).getValue());
							break;
						case INT:
							data.add(((ValueInt) timedValue.value).getValue());
							break;
						default:
							data.add((JsonNode) null);
					}
					data_type.add(timedValue.value.getType().toString());
				}
			}
		}

		root.set("data", data);
		root.set("data_type", data_type);
		root.set("label", label);
		return root;
	}

	@Scheduled(fixedDelay = COLLECTION_INTERVAL_MS, initialDelay = 1000)
	public void update() {
		Set<String> zones;
		try {
			zones = agentService.getStoredZones();
		} catch (Exception e) {
			System.out.println("EXCEPTION when fetching zones list:" + e.getMessage() + ". Skipping.");
			return;
		}

		// local snapshot of this.plotMap
		Map<String, Map<String, List<TimedValue>>> plotMap;

		synchronized (this.plotMap) {
			for (String zone: new LinkedList<>(this.plotMap.keySet())) {
				if (!zones.contains(zone)) {
					this.plotMap.remove(zone);
				}
			}
			for (String zone: zones) {
				if (!this.plotMap.containsKey(zone)) {
					this.plotMap.put(zone, new HashMap<>());
				}
			}

			// take snapshot of top level
			plotMap = (Map) ((HashMap) this.plotMap).clone();
		}
		// don't use this.plotMap past this point

		for (String zone: zones) {
			Map<String, List<TimedValue>> attrMap = plotMap.get(zone);

			String now = TIME_FORMAT.format(new Date());
			Map<String, Value> receivedMap;
			try {
				receivedMap = agentService.getZoneAttributes(zone);
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

	public void reset() {
		synchronized (plotMap) {
			plotMap.clear();
		}
	}
}
