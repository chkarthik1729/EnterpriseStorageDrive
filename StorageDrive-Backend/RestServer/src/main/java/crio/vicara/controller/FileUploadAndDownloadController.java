package crio.vicara.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FileUploadAndDownloadController {

    @PostMapping(value = "/files/{parentId}", consumes = {"application/json"})
    public void createDirectory(@PathVariable String parentId, Authentication authentication) {

    }
}
