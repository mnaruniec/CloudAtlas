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
import pl.edu.mimuw.client.services.PlotService;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AttributeDataController {
	@Autowired
	private PlotService plotService;

	@GetMapping("/data/attribute")
	public JsonNode attribute(
			@RequestParam(name="zone", required=false, defaultValue="/") String zone,
			@RequestParam(name="attribute", required=true) String attribute) {
		return plotService.getPlotData(zone, attribute);
	}
}
