package pl.edu.mimuw.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.edu.mimuw.client.services.AgentService;
import pl.edu.mimuw.client.services.PlotService;
import pl.edu.mimuw.client.services.SignerService;
import pl.edu.mimuw.cloudatlas.signing.outputs.SignedUninstallation;

@Controller
public class ChangeAgentController {
	@Autowired
	private PlotService plotService;

	@Autowired
	private AgentService agentService;

	public static class ChangeAgentForm {
		private String hostname;

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public boolean isFilled() {
			return hostname != null && !hostname.isEmpty();
		}
	}

	@GetMapping("/change_agent")
	public String changeAgent(Model model) {
		model.addAttribute("changeAgentForm", new ChangeAgentForm());
		return "change_agent";
	}

	@PostMapping("/change_agent")
	public String changeAgent(Model model, @ModelAttribute ChangeAgentForm changeAgentForm) {
		String message = "SUCCESS: Connected.";
		try {
			if (!changeAgentForm.isFilled()) {
				throw new IllegalArgumentException("Provide values in all fields.");
			}
			agentService.setHost(changeAgentForm.getHostname().trim());
			plotService.reset();
		} catch (Exception e) {
			e.printStackTrace();
			message = "WARNING:" + e.getMessage();
		}
		model.addAttribute("message", message);
		return "change_agent";
	}
}
