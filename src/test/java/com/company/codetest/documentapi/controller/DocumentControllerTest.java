package com.company.codetest.documentapi.controller;

import com.company.codetest.documentapi.dto.DocumentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DocumentControllerTest {

    Logger logger = LoggerFactory.getLogger(DocumentControllerTest.class);
    private static String createDocumentUrl;
    private static String updateDocumentUrl;
    private static String getDocumentUrl;
    private static String deleteDocumentUrl;
    private static TestRestTemplate testRestTemplate;
    private static String FILE1_NAME;
    private static String FILE2_NAME;
    private static String FILE1_CONTENTS;
    private static String FILE2_CONTENTS;

    @LocalServerPort
    private int randomServerPort;

    private static MockMultipartFile mockMultipartFile1;
    private static MockMultipartFile mockMultipartFile2;


    @BeforeEach
    void setUp() {
        FILE1_NAME = "test-file-1.txt";
        FILE2_NAME = "test-file-2.txt";
        FILE1_CONTENTS = "test data 1 contents";
        FILE2_CONTENTS = "test data 2 contents";
        ClassLoader classLoader = getClass().getClassLoader();
        mockMultipartFile1 = new MockMultipartFile("test-file-1", FILE1_NAME,
                "text/plain", FILE1_CONTENTS.getBytes());
        mockMultipartFile2 = new MockMultipartFile("test-file-2", FILE2_NAME,
                "text/plain", FILE2_CONTENTS.getBytes());

        createDocumentUrl   = "http://localhost:"+randomServerPort+"/storage/documents";
        updateDocumentUrl   = "http://localhost:"+randomServerPort+"/storage/documents/";
        getDocumentUrl      = "http://localhost:"+randomServerPort+"/storage/documents/";
        deleteDocumentUrl   = "http://localhost:"+randomServerPort+"/storage/documents/";

        testRestTemplate = new TestRestTemplate();
    }

    @Test
    void createDocument() throws IOException  {
        //1.Create the Document
        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity
                = prepareRequestEntity(mockMultipartFile1);
        ResponseEntity<DocumentResponse> documentResponse
                = testRestTemplate.exchange(createDocumentUrl, HttpMethod.POST, reqEntity, DocumentResponse.class);
        //2.Verify request succeed
        assertNotNull(documentResponse);
        assertEquals(HttpStatus.CREATED, documentResponse.getStatusCode());
        assertTrue(documentResponse.getBody().getDocumentType().equalsIgnoreCase("application/txt"));
        assertEquals(20L, documentResponse.getBody().getSize());
        logger.info("Document {} with contents: {} created",
                documentResponse.getBody().getDocumentId(), documentResponse.getBody().getDocumentId());
    }

    @Test
    void updateDocument() throws IOException {
        //1. Create the Document
        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity
                = prepareRequestEntity(mockMultipartFile1);
        ResponseEntity<DocumentResponse> createDocumentResponse
                = testRestTemplate.exchange(createDocumentUrl, HttpMethod.POST, reqEntity, DocumentResponse.class);
        String documentId = createDocumentResponse.getBody().getDocumentId();
        //2. Verify the Document exists
        ResponseEntity<Resource> resourceResponseEntity
                = testRestTemplate.getForEntity(getDocumentUrl+documentId, Resource.class);
        assertEquals(HttpStatus.OK, resourceResponseEntity.getStatusCode());
        assertEquals(FILE1_NAME, resourceResponseEntity.getBody().getFilename());
        assertEquals(FILE1_CONTENTS, readFileData(resourceResponseEntity));
        //3. update the same documentID with different document content
        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity2 = prepareRequestEntity(mockMultipartFile2);
        ResponseEntity<DocumentResponse> updateDocumentResponse
                = testRestTemplate.exchange(updateDocumentUrl+documentId, HttpMethod.PUT, reqEntity2, DocumentResponse.class);
        assertEquals(HttpStatus.NO_CONTENT, updateDocumentResponse.getStatusCode());
        //4. Verify if the Document update succeeded
        ResponseEntity<Resource> resourceResponseEntity2 = testRestTemplate.getForEntity(getDocumentUrl+documentId, Resource.class);
        assertEquals(HttpStatus.OK, resourceResponseEntity2.getStatusCode());
        assertNotNull(resourceResponseEntity2.getBody().getFilename());
        assertEquals(FILE2_NAME, resourceResponseEntity2.getBody().getFilename());
        assertEquals(FILE2_CONTENTS, readFileData(resourceResponseEntity2));
    }

    @Test
    void deleteDocument() throws IOException {
        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = prepareRequestEntity(mockMultipartFile1);
        ResponseEntity<DocumentResponse> documentResponse
                = testRestTemplate.exchange(createDocumentUrl, HttpMethod.POST, reqEntity, DocumentResponse.class);
        String documentId = documentResponse.getBody().getDocumentId();
        ResponseEntity<Resource> resource = testRestTemplate.getForEntity(getDocumentUrl+documentId, Resource.class);

        //Verify request succeed
        assertEquals(HttpStatus.OK, resource.getStatusCode());
        assertEquals(FILE1_NAME, resource.getBody().getFilename());
        assertEquals(FILE1_CONTENTS, readFileData(resource));

        //Delete the document
        testRestTemplate.delete(deleteDocumentUrl+documentId);
        //check if its deleted
        ResponseEntity<Resource> resource2 = testRestTemplate.getForEntity(getDocumentUrl+documentId, Resource.class);
        assertEquals(HttpStatus.NOT_FOUND, resource2.getStatusCode());
    }

    @Test
    void getDocument() throws IOException {
        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity
                = prepareRequestEntity(mockMultipartFile1);
        ResponseEntity<DocumentResponse> documentResponse
                = testRestTemplate.exchange(createDocumentUrl, HttpMethod.POST, reqEntity, DocumentResponse.class);
        String documentId = documentResponse.getBody().getDocumentId();
        assertNotNull(documentId);
        ResponseEntity<Resource> resource
                = testRestTemplate.getForEntity(getDocumentUrl+documentId, Resource.class);
        //Verify request succeed
        assertEquals(HttpStatus.OK, resource.getStatusCode());
        assertEquals(FILE1_NAME, resource.getBody().getFilename());
        assertEquals(FILE1_CONTENTS, readFileData(resource));
    }
    private HttpEntity<LinkedMultiValueMap<String, Object>> prepareRequestEntity(MockMultipartFile mockMultipartFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> documentFileHeaderMap = new LinkedMultiValueMap<>();
        documentFileHeaderMap.add("Content-disposition", "form-data; name=document; filename=" + mockMultipartFile.getOriginalFilename());
        documentFileHeaderMap.add("Content-type", "application/txt");
        HttpEntity<byte[]> doc = new HttpEntity<byte[]>(mockMultipartFile.getBytes(), documentFileHeaderMap);

        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("document", doc);

        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(multipartReqMap, headers);

        return reqEntity;
    }
    private String readFileData(ResponseEntity<Resource> resourceResponseEntity){
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(resourceResponseEntity.getBody().getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            System.out.println(result.toString());
        }catch (Exception ex){
            System.out.println("Exception while reading the file data : "+ ex.getMessage());
        }
        return result.toString();
    }
}