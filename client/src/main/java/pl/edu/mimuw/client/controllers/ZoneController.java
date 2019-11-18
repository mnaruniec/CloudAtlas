package pl.edu.mimuw.client.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ZoneController {
	@GetMapping("/zone")
	public String zone(Model model,
			@RequestParam(name="name", required=false, defaultValue="/") String zoneName) {
		model.addAttribute("zoneName", zoneName);
		return "zone";
	}
}
