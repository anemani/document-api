package com.company.codetest.documentapi.domain;


import javax.persistence.*;

@Entity
public class DocumentMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "document_id")
    private String documentId;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "document_location")
    private String documentLocation;

    protected DocumentMetaData() {
    }
    public DocumentMetaData(String documentId,
                            String documentName,
                            String documentLocation) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.documentLocation = documentLocation;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentLocation() {
        return documentLocation;
    }

    public void setDocumentLocation(String documentLocation) {
        this.documentLocation = documentLocation;
    }

    @Override
    public String toString() {
        return "DocumentMetaData{" +
                "id=" + id +
                ", documentId='" + documentId + '\'' +
                ", documentName='" + documentName + '\'' +
                ", documentLocation='" + documentLocation + '\'' +
                '}';
    }
}