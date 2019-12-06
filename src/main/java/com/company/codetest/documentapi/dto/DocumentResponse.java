package com.company.codetest.documentapi.dto;

public class DocumentResponse {
    private String documentId;
    private String documentDownloadUri;
    private String documentType;
    private long size;

    public DocumentResponse(String documentId,
                            String documentDownloadUri,
                            String documentType,
                            long size) {
        this.documentId = documentId;
        this.documentDownloadUri = documentDownloadUri;
        this.documentType = documentType;
        this.size = size;
    }

    public DocumentResponse() {
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentDownloadUri() {
        return documentDownloadUri;
    }

    public void setDocumentDownloadUri(String documentDownloadUri) {
        this.documentDownloadUri = documentDownloadUri;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
