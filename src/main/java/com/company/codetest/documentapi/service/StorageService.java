package com.company.codetest.documentapi.service;

import com.company.codetest.documentapi.controller.DocumentController;
import com.company.codetest.documentapi.domain.DocumentMetaData;
import com.company.codetest.documentapi.dto.DocumentRequest;
import com.company.codetest.documentapi.dto.DocumentResponse;
import com.company.codetest.documentapi.exception.DocumentNotFoundException;
import com.company.codetest.documentapi.exception.DocumentStorageException;
import com.company.codetest.documentapi.config.DocumentStorageProperties;
import com.company.codetest.documentapi.repository.DocumentMetaDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    private final Path documentStorageLocation;

    @Autowired
    private IdGeneratorService idGeneratorService;

    @Autowired
    private DocumentMetaDataRepository documentMetaDataRepository;

    @Autowired
    public StorageService(DocumentStorageProperties documentStorageProperties) {
        this.documentStorageLocation = Paths.get(documentStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.documentStorageLocation);
        } catch (Exception ex) {
            logger.error("Could not create the directory");
            throw new DocumentStorageException("Could not create the directory where the uploaded documents will be stored.", ex);
        }
    }

    public DocumentResponse saveDocument(DocumentRequest documentRequest) {
        MultipartFile document = documentRequest.getDocument();
        //Load new random Id.
        String newDocumentId = idGeneratorService.generateRandomDocumentId();

        // Normalize document name
        String documentName = StringUtils.cleanPath(document.getOriginalFilename());

        try {
            // Check if the document's name contains invalid characters
            if(documentName.contains("..")) {
                logger.error("Invalid Document Name");
                throw new DocumentStorageException("Sorry! Document Name contains invalid path sequence " + documentName);
            }

            // Copy document to the target location (Replacing existing document with the same name)
            Path targetLocation = this.documentStorageLocation.resolve(documentName);
            Files.copy(document.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            //store the metadata details in metadata repository
            DocumentMetaData docMetaDataObj
                    = new DocumentMetaData(newDocumentId, documentName, targetLocation.toString());
            documentMetaDataRepository.save(docMetaDataObj);

            //return the Response Object
            String documentDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/documents/")
                    .path(newDocumentId)
                    .toUriString();

            return new DocumentResponse(newDocumentId, documentDownloadUri,
                    document.getContentType(), document.getSize());
        } catch (IOException ex) {
            logger.error("StorageService.saveDocument()...Document save error");
            throw new DocumentStorageException("Could not store document " + documentName + ". Please try again!", ex);
        }
    }

    public Resource loadDocument(DocumentRequest documentRequest) {
        String documentId = documentRequest.getDocumentId();
        try {
            DocumentMetaData docMetaDataObj
                    = documentMetaDataRepository.findDocumentMetaDataByDocumentId(documentId);
            if(docMetaDataObj==null || !docMetaDataObj.getDocumentId().equalsIgnoreCase(documentId)) {
                throw new DocumentNotFoundException("Document MetaData not found " + documentId);
            }
            String documentName = docMetaDataObj.getDocumentName();
            Path documentPath = this.documentStorageLocation.resolve(documentName).normalize();
            Resource resource = new UrlResource(documentPath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                logger.error("StorageService.loadDocument()...Document Name not found");
                throw new DocumentNotFoundException("Document Name not found " + documentName);
            }
        } catch (MalformedURLException ex) {
            logger.error("StorageService.loadDocument()...Document ID not found");
            throw new DocumentNotFoundException("Document ID not found " + documentId, ex);
        }
    }

    public void deleteDocument(DocumentRequest documentRequest) {
        try {
            String documentId = documentRequest.getDocumentId();
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
                logger.error("StorageService.deleteDocument()...Document name not found");
                throw new DocumentNotFoundException("Document Name not found " + documentName);
            }
        } catch (Exception ex) {
            logger.error("StorageService.deleteDocument()...unable to delete Document.");
            throw new DocumentNotFoundException("Unable to delete document : ", ex);
        }
    }

    public DocumentResponse updateDocument(DocumentRequest documentRequest) {
        try {
            String documentId = documentRequest.getDocumentId();
            MultipartFile document = documentRequest.getDocument();
            // Normalize document name
            String newDocumentName = StringUtils.cleanPath(document.getOriginalFilename());

            // Copy document to the target location (Replacing existing document with the same name)
            Path targetLocation = this.documentStorageLocation.resolve(newDocumentName);
            Files.copy(document.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            DocumentMetaData docMetaDataObj
                    = documentMetaDataRepository.findDocumentMetaDataByDocumentId(documentId);
            String oldDocumentName = docMetaDataObj.getDocumentName();

            //update the metadata details in metadata repository
            docMetaDataObj.setDocumentName(newDocumentName);
            docMetaDataObj.setDocumentLocation(targetLocation.toString());
            documentMetaDataRepository.save(docMetaDataObj);

            //return the Response Object
            String documentDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/documents/")
                    .path(documentId)
                    .toUriString();
            logger.info("Document with document id '{}' is replaced with '{}'", oldDocumentName, newDocumentName);
            return new DocumentResponse(documentId, documentDownloadUri,
                    document.getContentType(), document.getSize());

        } catch (Exception ex) {
            logger.error("StorageService.updateDocument()...Document update error");
            throw new DocumentNotFoundException("Error while updating document : ", ex);
        }
    }
}
