package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.Document;
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

        if(this.documentService.createDocument(document, documentDTO.getFile(), documentDTO.getFileId())){
            return ResponseEntity.ok().build();
        }else{
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get/{documentId}")
    public ResponseEntity<Document> getDocument(@PathVariable String documentId) {
        return ResponseEntity.ok(this.documentService.getObject(documentId));
    }
}
