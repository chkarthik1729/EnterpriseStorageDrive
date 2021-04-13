package crio.vicara.controller;

import crio.vicara.File;
import crio.vicara.service.FileDetails;
import crio.vicara.service.StorageManager;
import crio.vicara.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    StorageManager storageManager;
    @Autowired
    UserDao userDao;

    @PostMapping(value = "/files/{parentId}", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String createDirectory(@PathVariable String parentId,
                                  @RequestBody Map<String, Object> payload,
                                  Authentication authentication) throws FileAlreadyExistsException, FileNotFoundException {
        String folderName = (String) payload.get("folderName");
        String currUserEmail = authentication.getName();

        return storageManager.createFolder(parentId, folderName, currUserEmail);
    }

    @PostMapping(value = "/files", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public String createDirectoryInsideUserRootFolder(@RequestBody Map<String, Object> payload,
                                                      Authentication authentication) throws FileAlreadyExistsException, FileNotFoundException {
        String folderName = (String) payload.get("folderName");
        String currUserEmail = authentication.getName();
        return storageManager
                .createFolderInsideUserRootFolder(
                        folderName,
                        userDao.findByEmail(currUserEmail).getStorageProvider(),
                        currUserEmail
                );
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
        return storageManager.uploadFileInsideUserRootFolder(file.getOriginalFilename(),
                file.getInputStream(),
                file.getSize(),
                userDao.findByEmail(currUserEmail).getStorageProvider(),
                currUserEmail);
    }

    @GetMapping(value = "/files/{fileId}")
    public Object getFileOrFolder(Authentication authentication,
                                  @PathVariable String fileId,
                                  HttpServletResponse servletResponse) throws FileNotFoundException {
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
            return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
        } else if (res instanceof File) {
            return res;
        }
        return null;
    }

    @GetMapping(value = "/files")
    public File getUserHomeFolder(Authentication authentication) throws FileNotFoundException {
        String currUserEmail = authentication.getName();
        return storageManager.getUserRootFolder(currUserEmail,
                userDao.findByEmail(currUserEmail).getStorageProvider());
    }

    @DeleteMapping(value = "/files/{fileId}")
    public void deleteFile(Authentication authentication,
                           @PathVariable String fileId) throws FileNotFoundException {
        storageManager.deleteFile(fileId, authentication.getName());
    }

    @PutMapping(value = "/files/move")
    public void moveFileOrFolder(Authentication authentication,
                                 @RequestBody Map<String, String> payload) throws FileAlreadyExistsException, NotDirectoryException, FileNotFoundException {
        storageManager.move(
                payload.get("fromId"),
                payload.get("toId"),
                Boolean.parseBoolean(payload.get("forced")),
                authentication.getName()
        );
    }

    @GetMapping(value = "/files/{fileId}/details")
    public FileDetails getFileDetails(Authentication authentication, @PathVariable String fileId) throws FileNotFoundException {
        return storageManager.getFileDetails(fileId, authentication.getName());
    }

    @PatchMapping(value = "/files/{fileId}/details")
    public FileDetails patchFileDetails(Authentication authentication,
                                        @PathVariable String fileId,
                                        @RequestBody Map<String, String> payload) throws FileAlreadyExistsException, FileNotFoundException {
        return storageManager.patchFileDetails(fileId, authentication.getName(), payload);
    }

    @GetMapping(value = "/files/favourites")
    public List<FileDetails> getFavourites(Authentication authentication) {
        return storageManager.getFavourites(authentication.getName());
    }
}
