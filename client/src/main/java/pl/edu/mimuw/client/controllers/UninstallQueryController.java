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
public class UninstallQueryController {
	@Autowired
	private SignerService signerService;

	@Autowired
	private AgentService agentService;

	public static class UninstallForm {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isFilled() {
			return name != null && !name.isEmpty();
		}
	}

	@GetMapping("/uninstall")
	public String uninstallQuery(Model model) {
		model.addAttribute("uninstallForm", new UninstallForm());
		return "uninstall";
	}

	@PostMapping("/uninstall")
	public String uninstallQuery(Model model, @ModelAttribute UninstallForm uninstallForm) {
		String message = "SUCCESS";
		try {
			if (!uninstallForm.isFilled()) {
				throw new IllegalArgumentException("Provide values in all fields.");
			}
			// TODO
//			agentService.uninstallQuery(uninstallForm.getName());
		} catch (Exception e) {
			e.printStackTrace();
			message = "ERROR:" + e.getMessage();
		}
		model.addAttribute("message", message);
		return "uninstall";
	}
}
