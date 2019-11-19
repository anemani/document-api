package com.company.codetest.documentapi.repository;

import com.company.codetest.documentapi.domain.DocumentMetaData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentMetaDataRepository extends JpaRepository<DocumentMetaData, Integer> {

    DocumentMetaData findDocumentMetaDataByDocumentId(String docId);
}