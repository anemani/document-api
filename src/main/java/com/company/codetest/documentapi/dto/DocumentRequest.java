package com.company.codetest.documentapi.dto;

import org.springframework.web.multipart.MultipartFile;


public class DocumentRequest {

    private String documentId;
    private MultipartFile document;

    public DocumentRequest() {
    }

    public DocumentRequest(String documentId, MultipartFile document) {
        this.documentId = documentId;
        this.document = document;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public MultipartFile getDocument() {
        return document;
    }

    public void setDocument(MultipartFile document) {
        this.document = document;
    }

    @Override
    public String toString() {
        return "DocumentRequest{" +
                "documentId='" + documentId + '\'' +
                ", document=" + document +
                '}';
    }
}
