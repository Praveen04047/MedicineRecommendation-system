package com.harsh.medicine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.harsh.medicine.model.MedicationRecommendation;

// Imports for Gemini API SDK (adjust version as needed)
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

@Service
public class RecommendationService {

    @Value("${gemini.model}")
    private String geminiModel;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public List<MedicationRecommendation> getRecommendations(List<String> symptoms) {
        List<MedicationRecommendation> recommendations = new ArrayList<>();

        try {
            // Initialize Gemini API client
            Client client = Client.builder()
                                  .setApiKey(geminiApiKey)
                                  .build();

            // Build prompt
            String prompt = buildPrompt(symptoms);

            // Send request
            GenerateContentResponse response = client.models()
                                                     .generateContent(geminiModel, prompt, null);

            String aiOutput = response.text();

            // Parse AI output into medication records
            recommendations = parseRecommendations(aiOutput);

        } catch (Exception e) {
            // In case of error, fallback to a simple mapping
            recommendations = fallbackRecommendations(symptoms);
        }

        return recommendations;
    }

    private String buildPrompt(List<String> symptoms) {
        StringBuilder sb = new StringBuilder();
        sb.append("A user reports the following symptoms: ");
        sb.append(String.join(", ", symptoms));
        sb.append(". Based on these symptoms, recommend possible over-the-counter tablet medications (name, typical dosage, known side-effects). ");
        sb.append("Include a clear disclaimer: this is for informational purposes only and not medical advice.");
        return sb.toString();
    }

    private List<MedicationRecommendation> parseRecommendations(String aiOutput) {
        List<MedicationRecommendation> list = new ArrayList<>();

        // ** Simple parsing logic **
        // For example, assume AI returns lines like:
        // "1. Paracetamol – dose 500 mg every 6 hours; side-effects: liver toxicity, rash."
        String[] lines = aiOutput.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            try {
                // Very basic extraction (you may improve using regex or JSON schema)
                String[] parts = line.split("–");
                String namePart = parts[0].trim();
                String detailsPart = parts.length > 1 ? parts[1].trim() : "";

                String name = namePart.replaceAll("^\\d+\\.\\s*", "");
                String dosage = "";
                String sideEffects = "";

                if (detailsPart.contains(";")) {
                    String[] segs = detailsPart.split(";");
                    dosage = segs[0].trim();
                    sideEffects = (segs.length > 1 ? segs[1].trim() : "");
                } else {
                    dosage = detailsPart;
                }

                MedicationRecommendation rec = new MedicationRecommendation();
                rec.setMedicineName(name);
                rec.setDosage(dosage);
                rec.setSideEffects(sideEffects);
                list.add(rec);

            } catch (Exception ex) {
                // skip malformed line
            }
        }

        if (list.isEmpty()) {
            // fallback if parsing fails
            return fallbackRecommendations(null);
        }

        return list;
    }

    private List<MedicationRecommendation> fallbackRecommendations(List<String> symptoms) {
        List<MedicationRecommendation> list = new ArrayList<>();

        // Simple static mapping as fallback
        if (symptoms != null && symptoms.contains("fever")) {
            MedicationRecommendation rec = new MedicationRecommendation();
            rec.setMedicineName("Paracetamol");
            rec.setDosage("500 mg every 6 hours");
            rec.setSideEffects("Liver toxicity, rash");
            list.add(rec);
        } else {
            MedicationRecommendation rec = new MedicationRecommendation();
            rec.setMedicineName("Consult-a-Doctor");
            rec.setDosage("N/A");
            rec.setSideEffects("N/A");
            list.add(rec);
        }

        return list;
    }
}
