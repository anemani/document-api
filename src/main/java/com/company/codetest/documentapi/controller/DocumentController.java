package com.company.codetest.documentapi.controller;


import com.company.codetest.documentapi.payload.UploadDocumentResponse;
import com.company.codetest.documentapi.service.DocumentStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequestMapping(path = "/storage")
@RestController
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentStorageService documentStorageService;



    @PostMapping("/documents")
    @ResponseStatus(value = HttpStatus.CREATED)
    public UploadDocumentResponse createDocument(@RequestParam("document") MultipartFile document) {

        String documentName = documentStorageService.saveDocument(document);

        String documentDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/documents/")
                .path(documentName)
                .toUriString();

        return new UploadDocumentResponse(documentName, documentDownloadUri,
                document.getContentType(), document.getSize());
    }

    @PutMapping("/documents/{documentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public UploadDocumentResponse updateDocument(@RequestParam("document") MultipartFile document,
                                                       @PathVariable String documentId) {
        // Load document as Resource
        String updatedDocId = documentStorageService.updateDocument(documentId, document);

        String documentDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/documents/")
                .path(updatedDocId)
                .toUriString();

        return new UploadDocumentResponse(updatedDocId, documentDownloadUri,
                document.getContentType(), document.getSize());
    }

    @DeleteMapping("/documents/{documentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable String documentId) {
        try {
            // delete Document
            documentStorageService.deleteDocument(documentId);
        } catch (Exception ex) {
            logger.info("Could not delete the document:"+documentId);
        }
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<Resource> getDocument(@PathVariable String documentId, HttpServletRequest request) {
        // Load document as Resource
        Resource resource = documentStorageService.loadDocument(documentId);

        // Try to determine document's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine document type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
