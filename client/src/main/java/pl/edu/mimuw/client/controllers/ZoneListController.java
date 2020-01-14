package pl.edu.mimuw.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.edu.mimuw.client.services.AgentService;

@Controller
public class ZoneListController {
	@Autowired
	private AgentService agentService;

	@GetMapping("/zone_list")
	public String zone(Model model) {
		String message = "";
		try {
			model.addAttribute("zoneList", agentService.getStoredZonesList());
		} catch (Exception e) {
			e.printStackTrace();
			message = "ERROR:" + e.getMessage();
		}
		model.addAttribute("message", message);
		return "zone_list";
	}
}
