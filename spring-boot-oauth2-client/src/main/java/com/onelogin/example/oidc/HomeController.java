package com.onelogin.example.oidc;

import java.util.TreeMap;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String index(@AuthenticationPrincipal OidcUser user, Model model) {
		model.addAttribute("user", user);
		return "index";
	}

	@GetMapping("/profile")
	public String profile(@AuthenticationPrincipal OidcUser user, Model model) {
		model.addAttribute("user", user);
		model.addAttribute("claims", new TreeMap<>(user.getIdToken().getClaims()));
		return "profile";
	}

}
