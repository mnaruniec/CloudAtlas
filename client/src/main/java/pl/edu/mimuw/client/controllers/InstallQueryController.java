package pl.edu.mimuw.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.edu.mimuw.client.services.AgentService;
import pl.edu.mimuw.client.services.SignerService;

@Controller
public class InstallQueryController {
	@Autowired
	private SignerService signerService;

	@Autowired
	private AgentService agentService;

	public static class InstallForm {
		private String name;
		private String query;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public boolean isFilled() {
			return name != null && query != null && !name.isEmpty() && !query.isEmpty();
		}
	}

	@GetMapping("/install")
	public String installQuery(Model model) {
		model.addAttribute("installForm", new InstallForm());
		return "install";
	}

	@PostMapping("/install")
	public String installQuery(Model model, @ModelAttribute InstallForm installForm) {
		String message = "SUCCESS";
		try {
			if (!installForm.isFilled()) {
				throw new IllegalArgumentException("Provide values in all fields.");
			}
			agentService.installQuery(installForm.getName(), installForm.getQuery());
		} catch (Exception e) {
			e.printStackTrace();
			message = "ERROR:" + e.getMessage();
		}
		model.addAttribute("message", message);
		return "install";
	}
}
