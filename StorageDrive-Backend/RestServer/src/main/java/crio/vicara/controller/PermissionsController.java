package crio.vicara.controller;

import crio.vicara.exception.UnauthorizedException;
import crio.vicara.service.permissions.FilePermissions;
import crio.vicara.service.permissions.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api")
public class PermissionsController {

    @Autowired
    PermissionManager permissionManager;

    @GetMapping("/files/{fileId}/permissions")
    public FilePermissions getPermissions(@PathVariable String fileId,
                                          Authentication authentication) {
        if (permissionManager.hasReadAccess(fileId, authentication.getName())) {
            return permissionManager.getPermissions(fileId);
        } else throw new UnauthorizedException();
    }

    @PatchMapping(value = "/files/{fileId}/permissions", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void patchPermissions(@PathVariable String fileId,
                                 Authentication authentication,
                                 @RequestBody Map<String, String> payload) {

        if (permissionManager.hasWriteAccess(fileId, authentication.getName())) {
            for (Map.Entry<String, String> entry : payload.entrySet()) {
                if (entry.getValue() == null)
                    permissionManager.revokeAccess(fileId, entry.getKey());
                if (entry.getValue().equals("Read"))
                    permissionManager.giveReadAccess(fileId, entry.getKey());
                if (entry.getValue().equals("Write"))
                    permissionManager.giveWriteAccess(fileId, entry.getKey());
            }
        } else throw new UnauthorizedException();
    }
}
