package com.example.recognition;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaceController {
  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("message", "Hello Thymeleaf!!");
    return "index";
  }
}