package pl.edu.mimuw.client.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AttributeController {
	@GetMapping("/attribute")
	public String zone(Model model,
			@RequestParam(name="zone", required=false, defaultValue="/") String zoneName,
			@RequestParam(name="attribute", required=true) String attribute) {
		model.addAttribute("zoneName", zoneName);
		model.addAttribute("attribute", attribute);
		return "attribute";
	}
}
