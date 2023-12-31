package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Document;
import com.favorsoft.mplatform.cdn.domain.FileAttach;
import com.favorsoft.mplatform.cdn.repository.jpa.DocumentRepository;
import com.favorsoft.mplatform.cdn.service.DocumentService;
import com.favorsoft.mplatform.support.CommonUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackOn = Exception.class)
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${spring.profiles.active}")
    private String profile;

    public DocumentServiceImpl(DocumentRepository documentRepository){
        this.documentRepository = documentRepository;
    }

    @Override
    public List<Document> getList() {
        return documentRepository.findAll();
    }

    @Override
    public Document save(Document entity) {
        return documentRepository.saveAndFlush(entity);
    }

    @Override
    public Document getObject(Object key) {
        String documentId = (String) key;
        return documentRepository.findById(documentId).orElse(new Document());
    }

    @Override
    public void delete(Object key) {
        Document document = documentRepository.findById(String.valueOf(key)).orElse(new Document());
        documentRepository.delete(document);
    }

    @Override
    public boolean createDocument(Document document, MultipartFile file, String fileId) {
        Document doc = getObject(document.getDocumentId());
        BeanUtils.copyProperties(document, doc, CommonUtil.getNullPropertyNames(document));

        //기존에 존재하던 파일이름이고 파일 사이즈가 같지 않을 경우에 덮어쓰기
        Optional<FileAttach> fileAttachOpt = doc.getFileAttachList().stream().filter(docFile ->
                docFile.getFileOrgName().equals(file.getOriginalFilename())).findFirst();
        if(fileAttachOpt.isPresent()){
            FileAttach fileAttach = fileAttachOpt.get();
            if(fileAttach.getFileSize() != file.getSize()){
                File dest = new File(uploadPath + "/"+fileAttach.getFileId());
                if(dest.exists()){
                    dest.delete();
                }
                try{
                    file.transferTo(dest);
                    fileAttach.setStatus("USE");
                    fileAttach.setFileSize(file.getSize());
                }catch(IOException e){
                    document.setErrorMessage(e.getMessage());
                    fileAttach.setStatus("ERROR");
                }
            }
        }
        //새로운 파일
        else{
            FileAttach fileAttach = new FileAttach();
            fileAttach.setFileId(fileId);
            fileAttach.setFileName(fileAttach.getFileId());
            fileAttach.setFileOrgName(file.getOriginalFilename());
            fileAttach.setFileType(file.getContentType());
            fileAttach.setFileSize(file.getSize());
            fileAttach.setDocument(doc);
            fileAttach.setFilePath(uploadPath +"/"+fileAttach.getFileId());
            fileAttach.setServerInfo(profile);
            doc.getFileAttachList().add(fileAttach);
            File mkdir = new File(uploadPath);
            if(!mkdir.exists()){
                mkdir.mkdirs();
            }
            File dest = new File(uploadPath + "/"+fileAttach.getFileId());
            if(dest.exists()){
                dest.delete();
                dest = new File(uploadPath+"/"+fileAttach.getFileId());
            }
            try{
                file.transferTo(dest);
                fileAttach.setStatus("USE");
            }catch(IOException e){
                doc.setErrorMessage(e.getMessage());
                fileAttach.setStatus("ERROR");
            }
        }

        long errorCnt = doc.getFileAttachList().stream().filter(s -> s.getStatus().equals("ERROR")).count();

        if(errorCnt == 0){
            this.save(doc);
            return true;
        }

        return false;
    }
}
