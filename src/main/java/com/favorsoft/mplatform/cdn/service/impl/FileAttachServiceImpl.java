package com.favorsoft.mplatform.cdn.service.impl;

import com.favorsoft.mplatform.cdn.domain.Document;
import com.favorsoft.mplatform.cdn.domain.FileAttach;
import com.favorsoft.mplatform.cdn.repository.jpa.DocumentRepository;
import com.favorsoft.mplatform.cdn.repository.jpa.FileAttachRepository;
import com.favorsoft.mplatform.cdn.service.FileAttachService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class FileAttachServiceImpl implements FileAttachService {

    @Value("${spring.profiles.active}")
    private String profile;

    private final DocumentRepository documentRepository;
    private final FileAttachRepository fileAttachRepository;

    public FileAttachServiceImpl(DocumentRepository documentRepository, FileAttachRepository fileAttachRepository){
        this.fileAttachRepository = fileAttachRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public List<FileAttach> getList() {
        return fileAttachRepository.findAll();
    }

    @Override
    public FileAttach save(FileAttach entity) {
        return fileAttachRepository.save(entity);
    }

    @Override
    public FileAttach getObject(Object key) {
        return fileAttachRepository.findById(String.valueOf(key)).orElse(new FileAttach());
    }

    @Override
    public void delete(Object key) {
        String fileId = (String) key;
        FileAttach fileAttach = this.getObject((String) key);

        if(profile.equals(fileAttach.getServerInfo())){
            File file = new File(fileAttach.getFilePath());
            if(file.exists()){
                file.delete();
            }
        }

        Document document = fileAttach.getDocument();
        List<FileAttach> fileAttachList = document.getFileAttachList();
        fileAttachList.remove(fileAttachList.stream().filter(s -> s.getFileId().equals(fileId)).findFirst().get());

        documentRepository.saveAndFlush(document);
    }
}
