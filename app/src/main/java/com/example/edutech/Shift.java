package com.example.edutech;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Shift {
    private String userName;
    private String shiftDetails;
    private Timestamp startTime;
    private Timestamp endTime;

    // This field will hold the document's ID from Firestore
    @Exclude // This tells Firestore not to save this field
    private String documentId;

    public Shift() {} // Required for Firestore

    // Getters
    public String getUserName() { return userName; }
    public String getShiftDetails() { return shiftDetails; }
    public Timestamp getStartTime() { return startTime; }
    public Timestamp getEndTime() { return endTime; }
    public String getDocumentId() { return documentId; }

    // --- NEW: Add this setter method ---
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}