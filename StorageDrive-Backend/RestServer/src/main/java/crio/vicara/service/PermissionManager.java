package crio.vicara.service;


// TODO: Implement this using PermissionsDao

public class PermissionManager {

    boolean hasReadAccess(String fileId, String userEmail) {
        return false;
    }

    boolean hasWriteAccess(String fileId, String userEmail) {
        return false;
    }

    void giveReadAccess(String fileId, String userEmail) {

    }

    void giveWriteAccess(String fileId, String userEmail) {

    }

    void revokeReadAccess(String fileId, String userEmail) {

    }

    void revokeWriteAccess(String fileId, String userEmail) {

    }

    void addFile(String parentId, String fileId, String ownerEmail) {

    }
}
