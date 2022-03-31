package com.favorsoft.mplatform.cdn.web;

import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

import com.favorsoft.mplatform.cdn.domain.Document;
import com.favorsoft.mplatform.cdn.domain.FileAttach;
import com.favorsoft.mplatform.cdn.dto.DocumentDTO;
import com.favorsoft.mplatform.cdn.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/document")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService){
        this.documentService =documentService;
    }

    @PostMapping("/createDocument")
    public ResponseEntity<Document> createDocument(@ModelAttribute DocumentDTO documentDTO) {
        Document document = new Document(documentDTO.getDocumentId());

        String fileId = UUID.randomUUID().toString();
        if(this.documentService.createDocument(document, documentDTO.getFile(), fileId)){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get/{documentId}")
    public ResponseEntity<Document> getDocument(@PathVariable String documentId) {
        Document document = this.documentService.getObject(documentId);
        document.setFileAttachList(document.getFileAttachList().stream().sorted(Comparator.comparing(FileAttach::getCreatedDate)).collect(Collectors.toList()));
        return ResponseEntity.ok(this.documentService.getObject(documentId));
    }
}
