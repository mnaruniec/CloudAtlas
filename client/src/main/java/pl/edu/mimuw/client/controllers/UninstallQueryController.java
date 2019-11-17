package pl.edu.mimuw.client.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UninstallQueryController {
	public static class UninstallForm {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@GetMapping("/uninstall")
	public String installQuery(Model model) {
		model.addAttribute("uninstallForm", new UninstallForm());
		return "uninstall";
	}

	@PostMapping("/uninstall")
	public String installQuery(@ModelAttribute UninstallForm installForm) {
		// TODO - implement logic
		return "uninstall";
	}
}
