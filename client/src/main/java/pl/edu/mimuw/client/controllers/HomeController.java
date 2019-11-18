package pl.edu.mimuw.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.edu.mimuw.client.services.AgentService;

@Controller
public class HomeController {
	@Autowired
	private AgentService agentService;

	@GetMapping("/")
	public String home() {
		return "redirect:/zone_list";
	}
}
