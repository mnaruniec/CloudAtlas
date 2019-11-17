package pl.edu.mimuw.client.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class InstallQueryController {
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
	}

	@GetMapping("/install")
	public String installQuery(Model model) {
		model.addAttribute("installForm", new InstallForm());
		return "install";
	}

	@PostMapping("/install")
	public String installQuery(@ModelAttribute InstallForm installForm) {
		// TODO - implement logic
		return "install";
	}
}
