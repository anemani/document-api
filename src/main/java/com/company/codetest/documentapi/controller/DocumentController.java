package com.company.codetest.documentapi.controller;


import com.company.codetest.documentapi.dto.DocumentRequest;
import com.company.codetest.documentapi.dto.DocumentResponse;
import com.company.codetest.documentapi.exception.DocumentNotFoundException;
import com.company.codetest.documentapi.service.StorageService;
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
    private StorageService storageService;

    @PostMapping("/documents")
    @ResponseStatus(value = HttpStatus.CREATED)
    public DocumentResponse createDocument(@RequestParam("document") MultipartFile document) {
        DocumentRequest documentRequest = new DocumentRequest(null, document);
        return storageService.saveDocument(documentRequest);
    }

    @PutMapping("/documents/{documentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public DocumentResponse updateDocument(@RequestParam("document") MultipartFile document,
                                           @PathVariable String documentId) {
        DocumentRequest documentRequest = new DocumentRequest(documentId, document);
        // Load document as Resource
        return storageService.updateDocument(documentRequest);

    }

    @DeleteMapping("/documents/{documentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable String documentId) {
        try {
            DocumentRequest documentRequest = new DocumentRequest(documentId, null);
            // delete Document
            storageService.deleteDocument(documentRequest);
        } catch (Exception ex) {
            logger.info("Could not delete the document:"+documentId);
        }
    }

    @GetMapping("/documents/{documentId}")
    public ResponseEntity<Resource> getDocument(@PathVariable String documentId, HttpServletRequest request) {
        //prepare request object
        DocumentRequest documentRequest = new DocumentRequest(documentId, null);
        // Load document as Resource
        String contentType = null;
        Resource resource = null;
        try {
            resource = storageService.loadDocument(documentRequest);
            // Try to determine document's content type
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.error("Could not determine content type.");
        } catch (DocumentNotFoundException ex) {
            logger.error("Could not determine document type.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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

/*    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<Exception> handleAllExceptions(RuntimeException ex) {
        return new ResponseEntity<Exception>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }*/
}
