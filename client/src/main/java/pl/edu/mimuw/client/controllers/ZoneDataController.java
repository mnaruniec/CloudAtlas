package pl.edu.mimuw.client.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.mimuw.client.services.AgentService;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ZoneDataController {
	@Autowired
	private AgentService agentService;

	@Autowired
	private ObjectMapper mapper;

	@GetMapping("/data/zone")
	public JsonNode zone(
			@RequestParam(name="name", required=false, defaultValue="/") String zone) {
		String error = "";
		Map<String, Value> valueMap = new HashMap<>();
		try {
			valueMap = agentService.getZoneAttributes(zone);
		} catch (Exception e) {
			error = e.getMessage();
		}
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("error", error);

		ArrayNode dataNode = mapper.createArrayNode();
		for (Map.Entry<String, Value> entry: valueMap.entrySet()) {
			String type = entry.getValue().getType().toString();
			String value = ((ValueString) entry.getValue().convertTo(TypePrimitive.STRING)).getValue();
			ObjectNode child = mapper.createObjectNode();
			child.put("name", entry.getKey());
			child.put("type", type);
			child.put("value", value);
			dataNode.add(child);
		}

		rootNode.set("data", dataNode);
		return rootNode;
	}
}
