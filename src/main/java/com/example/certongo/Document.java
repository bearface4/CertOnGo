package com.example.certongo;

import com.google.firebase.Timestamp;

public class Document {
    private String name;
    private String type_of_document;
    private String purpose_of_request;
    private String date;
    private Timestamp submit_date;
    private String status;
    private String reqfile;
    private String feedback;
    private String documentId;

    public Document() {}

    public Document(String name, String type_of_document, String purpose_of_request, String date, Timestamp submit_date, String status, String reqfile, String feedback) {
        this.name = name;
        this.type_of_document = type_of_document;
        this.purpose_of_request = purpose_of_request;
        this.date = date;
        this.submit_date = submit_date;
        this.status = status;
        this.reqfile = reqfile;
        this.feedback = feedback;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType_of_document() {
        return type_of_document;
    }

    public void setType_of_document(String type_of_document) {
        this.type_of_document = type_of_document;
    }

    public String getPurpose_of_request() {
        return purpose_of_request;
    }

    public void setPurpose_of_request(String purpose_of_request) {
        this.purpose_of_request = purpose_of_request;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Timestamp getSubmitDate() {
        return submit_date;
    }

    public void setSubmitDate(Timestamp submit_date) {
        this.submit_date = submit_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReqfile() {
        return reqfile;
    }

    public void setReqfile(String reqfile) {
        this.reqfile = reqfile;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
