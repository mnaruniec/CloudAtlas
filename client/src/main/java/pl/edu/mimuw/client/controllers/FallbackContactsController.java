package pl.edu.mimuw.client.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.edu.mimuw.client.services.AgentService;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Type;
import pl.edu.mimuw.cloudatlas.model.TypePrimitive;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ValueContact;
import pl.edu.mimuw.cloudatlas.model.ValueSet;

import javax.lang.model.type.PrimitiveType;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Controller
public class FallbackContactsController {
	@Autowired
	private AgentService agentService;

	public static class FallbackContactsForm {
		private String input;

		public String getInput() {
			return input;
		}

		public void setInput(String name) {
			this.input = name;
		}

		public boolean isFilled() {
			return input != null && !input.isEmpty();
		}
	}

	@GetMapping("/fallback_contacts")
	public String fallbackContacts(Model model) {
		String current = getFallbackContacts();
		model.addAttribute("current", current);
		model.addAttribute("fallbackContactsForm", new FallbackContactsForm());
		return "fallback_contacts";
	}

	@PostMapping("/fallback_contacts")
	public String fallbackContacts(Model model, @ModelAttribute FallbackContactsForm fallbackContactsForm) {
		String message = "SUCCESS";
		try {
			if (!fallbackContactsForm.isFilled()) {
				throw new IllegalArgumentException("Provide values in all fields.");
			}
			String[] lines = fallbackContactsForm.getInput().split("\n");
			if (lines.length % 2 != 0) {
				throw new IllegalArgumentException("You need to provide even number of lines.");
			}

			Set<ValueContact> set = new HashSet<>();
			for (int i = 0; i < lines.length; i += 2) {
				ValueContact value = new ValueContact(
						new PathName(lines[i].trim()),
						InetAddress.getByName(lines[i + 1].trim())
				);
				set.add(value);
			}

			agentService.setFallbackContacts(set);
		} catch (Exception e) {
			e.printStackTrace();
			message = "ERROR:" + e.getMessage();
		} finally {
			String current = getFallbackContacts();
			model.addAttribute("current", current);
		}
		model.addAttribute("message", message);
		return "fallback_contacts";
	}

	private String getFallbackContacts() {
		try {
			Set<ValueContact> set = agentService.getFallbackContacts();
			return set.toString();
		} catch (Exception e) {
			return "Could not retrieve current contacts. Exception " + e.getMessage();
		}
	}
}
