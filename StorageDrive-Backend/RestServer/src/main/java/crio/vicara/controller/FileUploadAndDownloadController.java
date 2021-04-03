package crio.vicara.controller;

import crio.vicara.service.StorageManager;
import crio.vicara.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileUploadAndDownloadController {

    @Autowired
    StorageManager storageManager;
    @Autowired
    UserDao userDao;

    @PostMapping(value = "/files/{parentId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String createDirectory(@PathVariable String parentId,
                                  @RequestBody Map<String, Object> payload,
                                  Authentication authentication) throws FileAlreadyExistsException {
        String folderName = (String) payload.get("folderName");
        String currUserEmail = authentication.getName();
        String createdFileId = storageManager.createFolder(parentId, folderName, currUserEmail);

        return createdFileId;
    }

    @PostMapping(value = "/files", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String createDirectoryInsideUserRootFolder(@RequestBody Map<String, Object> payload,
                                                      Authentication authentication) throws FileAlreadyExistsException {
        String folderName = (String) payload.get("folderName");
        String currUserEmail = authentication.getName();
        String createdFileId = storageManager
                .createFolderInsideUserRootFolder(
                        folderName,
                        userDao.findByEmail(currUserEmail).getStorageProvider(),
                        currUserEmail
                );
        return createdFileId;
    }

    @PostMapping(value = "/files/upload/{parentId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadFile(@PathVariable String parentId,
                           Authentication authentication,
                           @RequestParam("file") MultipartFile file) throws IOException {

        String createdFileId;
        String currUserEmail = authentication.getName();

        createdFileId = storageManager.uploadFile(parentId, file.getOriginalFilename(), file.getInputStream(), file.getSize(), currUserEmail);
        return createdFileId;
    }

    @PostMapping(value = "/files/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String uploadFileInsideUserRootFolder(Authentication authentication,
                                                 @RequestParam("file") MultipartFile file) throws IOException {
        String currUserEmail = authentication.getName();
        String createdFileId = storageManager.uploadFileInsideUserRootFolder(file.getOriginalFilename(),
                file.getInputStream(),
                file.getSize(),
                userDao.findByEmail(currUserEmail).getStorageProvider(),
                currUserEmail);
        return createdFileId;
    }

    @GetMapping(value = "/files/{fileId}")
    public Object getFileOrFolder(Authentication authentication,
                                  @PathVariable String fileId,
                                  HttpServletResponse servletResponse) {
        String currUserEmail = authentication.getName();
        Object res = storageManager.getFileOrFolder(fileId, currUserEmail);
        if (res instanceof URL) {
            servletResponse.setHeader("Location", ((URL) res).toExternalForm());
            servletResponse.setStatus(302);
        } else if (res instanceof InputStream) {
            InputStreamResource isr = new InputStreamResource((InputStream) res);
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.setContentLength(storageManager.getLength(fileId));
            ContentDisposition disposition = ContentDisposition
                    .inline()
                    .filename(storageManager.getFileName(fileId))
                    .build();
            return new ResponseEntity<InputStreamResource> (isr, respHeaders, HttpStatus.OK);
        }
        return null;
    }
}
