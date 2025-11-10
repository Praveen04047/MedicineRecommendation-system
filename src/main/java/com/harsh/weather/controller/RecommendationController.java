package com.harsh.medicine.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.harsh.medicine.model.MedicationRecommendation;
import com.harsh.medicine.service.RecommendationService;

@Controller
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;
    
    @GetMapping("")
    public String showForm() {
        return "recommendation";
    }
    
    @PostMapping("")
    public String getRecommendations(
            @RequestParam("symptoms") String symptoms,
            Model model) {
        
        // Split comma‐separated symptoms into list
        List<String> symptomList = List.of(symptoms.split("\\s*,\\s*"));
        
        List<MedicationRecommendation> recommendations =
                recommendationService.getRecommendations(symptomList);
        
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("symptoms", symptoms);
        
        return "recommendation";
    }
}
