package com.company.codetest.documentapi.service;

import com.company.codetest.documentapi.domain.DocumentMetaData;
import com.company.codetest.documentapi.exception.DocumentNotFoundException;
import com.company.codetest.documentapi.exception.DocumentStorageException;
import com.company.codetest.documentapi.property.DocumentStorageProperties;
import com.company.codetest.documentapi.repository.DocumentMetaDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class DocumentStorageService {

    private final Path documentStorageLocation;

    @Autowired
    private DocumentIdGeneratorService documentIdGeneratorService;

    @Autowired
    private DocumentMetaDataRepository documentMetaDataRepository;

    @Autowired
    public DocumentStorageService(DocumentStorageProperties documentStorageProperties) {
        this.documentStorageLocation = Paths.get(documentStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.documentStorageLocation);
        } catch (Exception ex) {
            throw new DocumentStorageException("Could not create the directory where the uploaded documents will be stored.", ex);
        }
    }

    public String saveDocument(MultipartFile document) {

        //Load new random Id.
        String newDocumentId = documentIdGeneratorService.generateRandomDocumentId();

        // Normalize document name
        String documentName = StringUtils.cleanPath(document.getOriginalFilename());

        try {
            // Check if the document's name contains invalid characters
            if(documentName.contains("..")) {
                throw new DocumentStorageException("Sorry! Document Name contains invalid path sequence " + documentName);
            }

            // Copy document to the target location (Replacing existing document with the same name)
            Path targetLocation = this.documentStorageLocation.resolve(documentName);
            Files.copy(document.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            //store the metadata details in metadata repository
            DocumentMetaData docMetaDataObj
                    = new DocumentMetaData(newDocumentId, documentName, targetLocation.toString());
            documentMetaDataRepository.save(docMetaDataObj);
            return newDocumentId;
        } catch (IOException ex) {
            throw new DocumentStorageException("Could not store document " + documentName + ". Please try again!", ex);
        }
    }

    public Resource loadDocument(String documentId) {

        try {
            DocumentMetaData docMetaDataObj
                    = documentMetaDataRepository.findDocumentMetaDataByDocumentId(documentId);
            String documentName = docMetaDataObj.getDocumentName();
            Path documentPath = this.documentStorageLocation.resolve(documentName).normalize();
            Resource resource = new UrlResource(documentPath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new DocumentNotFoundException("Document Name not found " + documentName);
            }
        } catch (MalformedURLException ex) {
            throw new DocumentNotFoundException("Document ID not found " + documentId, ex);
        }
    }

    public void deleteDocument(String documentId) {
        try {
            DocumentMetaData docMetaDataObj
                    = documentMetaDataRepository.findDocumentMetaDataByDocumentId(documentId);
            String documentName = docMetaDataObj.getDocumentName();
            Path documentPath = this.documentStorageLocation.resolve(documentName).normalize();
            Resource resource = new UrlResource(documentPath.toUri());
            if(resource.exists()) {
                Files.delete(documentPath);
                //update the metadata details in metadata repository
                documentMetaDataRepository.delete(docMetaDataObj);
            } else {
                throw new DocumentNotFoundException("Document Name not found " + documentName);
            }
        } catch (Exception ex) {
            throw new DocumentNotFoundException("Document ID not found " + documentId, ex);
        }
    }

    public String updateDocument(String documentId, MultipartFile document) {
        try {
            //delete the exiting file
            this.deleteDocument(documentId);

            //create the new file using same id
            return this.saveDocument(document);

        } catch (Exception ex) {
            throw new DocumentNotFoundException("Document ID not updated " + documentId, ex);
        }
    }
}
