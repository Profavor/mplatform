package com.favorsoft.mplatform.cdn.web;

import com.favorsoft.mplatform.cdn.domain.FileAttach;
import com.favorsoft.mplatform.cdn.service.FileAttachService;
import com.favorsoft.mplatform.support.CommonUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;

@RestController
@RequestMapping("/api/file-attach")
public class FileAttachController {
    private final FileAttachService fileAttachService;

    public FileAttachController(FileAttachService fileAttachService){
        this.fileAttachService = fileAttachService;
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId, HttpServletRequest request){
        FileAttach fileAttach = fileAttachService.getObject(fileId);
        fileAttach.setTotalDownCnt(fileAttach.getTotalDownCnt() + 1);
        fileAttach.setLastDownDate(new Date());
        fileAttachService.save(fileAttach);

        File file = new File(fileAttach.getFilePath());
        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
                        return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + CommonUtil.getFileNm(CommonUtil.getBrowser(request), fileAttach.getFileOrgName()) + "\"")
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(resource);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @GetMapping("/remove/{fileId}")
    public ResponseEntity<Boolean> removeFile(@PathVariable String fileId) {
        fileAttachService.delete(fileId);
        return ResponseEntity.ok().body(true);
    }
}
