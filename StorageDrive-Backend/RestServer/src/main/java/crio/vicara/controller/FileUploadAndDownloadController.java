package crio.vicara.controller;

import crio.vicara.service.StorageManager;
import crio.vicara.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class FileUploadAndDownloadController {

    @Autowired
    StorageManager storageManager;
    @Autowired
    UserDao userDao;

    @PostMapping(value = "/files/{parentId}", consumes = {"application/json"})
    public String createDirectory(@PathVariable String parentId,
                                  @RequestBody Map<String, Object> payload,
                                  Authentication authentication) throws FileAlreadyExistsException {
        String createdFileId;
        String folderName = (String) payload.get("folderName");
        String currUserEmail = authentication.getName();

        if (parentId == null) {
            createdFileId = storageManager
                    .createFolderInsideUserRootFolder(
                            folderName,
                            userDao.findByEmail(currUserEmail).getStorageProvider(),
                            currUserEmail
                    );
        } else {
            createdFileId = storageManager.createFolder(parentId, folderName, currUserEmail);
        }
        return createdFileId;
    }

    @PostMapping(value = "/files/upload/{parentId}")
    public String uploadFile(@PathVariable String parentId,
                           Authentication authentication,
                           @RequestParam("file") MultipartFile file) throws IOException {

        String createdFileId;
        String fileName = file.getName();
        String currUserEmail = authentication.getName();

        if (parentId == null) {
            createdFileId = storageManager.uploadFileInsideUserRootFolder(fileName,
                    file.getInputStream(),
                    file.getSize(),
                    userDao.findByEmail(currUserEmail).getStorageProvider(),
                    currUserEmail);
        } else {
            createdFileId = storageManager.uploadFile(parentId, fileName, file.getInputStream(), file.getSize(), currUserEmail);
        }
        return createdFileId;
    }
}
