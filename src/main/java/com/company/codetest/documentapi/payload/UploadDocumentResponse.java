package com.company.codetest.documentapi.payload;

public class UploadDocumentResponse {
    private String documentName;
    private String documentDownloadUri;
    private String documentType;
    private long size;

    public UploadDocumentResponse(String documentName, String documentDownloadUri, String documentType, long size) {
        this.documentName = documentName;
        this.documentDownloadUri = documentDownloadUri;
        this.documentType = documentType;
        this.size = size;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
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
