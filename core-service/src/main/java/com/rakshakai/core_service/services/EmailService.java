package com.rakshakai.core_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.rakshakai.core_service.models.Tourist;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendSOSAlert(Tourist tourist, String currentLat, String currentLon) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        // Put the destination email address where you want to receive the SOS alert
        message.setTo("tarabiradar@gmail.com"); 
        message.setSubject("🚨 URGENT: Rakshak AI Emergency Alert - " + tourist.getFullName());
        
        String body = "An emergency has been detected for " + tourist.getFullName() + " (ID: " + tourist.getTouristId() + ").\n\n"
                    + "The AI Anomaly Detection system flagged severe distress at the following coordinates:\n"
                    + "Latitude: " + currentLat + "\n"
                    + "Longitude: " + currentLon + "\n\n"
                    + "Medical History on file: " + (tourist.getMedicalConditions() != null ? tourist.getMedicalConditions() : "None") + "\n"
                    + "Blood Type: " + tourist.getBloodType() + "\n\n"
                    + "Rescue teams have been dispatched. Please stand by for further updates.";
                    
        message.setText(body);
        
        // Send asynchronously so it never slows down or blocks the live tracking API
        new Thread(() -> {
            try {
                mailSender.send(message);
                System.out.println("Emergency email successfully sent for " + tourist.getFullName());
            } catch (Exception e) {
                System.err.println("Failed to send email: " + e.getMessage());
            }
        }).start();
    }
}