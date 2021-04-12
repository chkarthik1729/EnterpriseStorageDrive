package crio.vicara.service.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PermissionManager {

    @Autowired PermissionsDao permissionsDao;

    /**
     * Checks if the userEmail has read access to the file/folder
     * @param fileId
     * @param userEmail
     * @return
     */
    public boolean hasReadAccess(String fileId, String userEmail) {
        var permissions = getPermissions(fileId);
        if (permissions.ownerEmail.equals(userEmail)) return true;
        return hasReadAccessRecurse(fileId, userEmail);
    }

    /**
     * Recursively checks if the user has read access on the file/folder or any of its ancestors
     * @param fileId
     * @param userEmail
     * @return
     */
    private boolean hasReadAccessRecurse(String fileId, String userEmail) {
        if (fileId == null) return false;
        var permissions = getPermissions(fileId);
        if (permissions.ownerEmail.equals(userEmail)) return true;

        if (permissions.accessList == null)
            return hasReadAccessRecurse(permissions.parentId, userEmail);

        for (var access : permissions.accessList) {
            if (access.getUserEmail().equals(userEmail) &&
                    (access.getLevel() == AccessLevel.Read || access.getLevel() == AccessLevel.Write))
                return true;
        }
        return hasReadAccessRecurse(permissions.parentId, userEmail);
    }

    /**
     * Checks if the userEmail has write access to the file/folder
     * @param fileId
     * @param userEmail
     * @return
     */
    public boolean hasWriteAccess(String fileId, String userEmail) {
        var permissions = getPermissions(fileId);
        if (permissions.ownerEmail.equals(userEmail)) return true;
        return hasWriteAccessRecurse(fileId, userEmail);
    }

    /**
     * Recursively checks if the user has write access on the file/folder or any of its ancestors
     * @param fileId
     * @param userEmail
     * @return
     */
    private boolean hasWriteAccessRecurse(String fileId, String userEmail) {
        if (fileId == null) return false;
        var permissions = getPermissions(fileId);
        if (permissions.ownerEmail.equals(userEmail)) return true;

        if (permissions.accessList == null)
            return hasWriteAccessRecurse(permissions.parentId, userEmail);

        for (var access : permissions.accessList) {
            if (access.getUserEmail().equals(userEmail) && access.getLevel() == AccessLevel.Write)
                return true;
        }
        return hasWriteAccessRecurse(permissions.parentId, userEmail);
    }

    /**
     * Tells whether userEmail is owner of fileId or not
     * @param fileId
     * @param userEmail
     * @return
     */
    public boolean isOwner(String fileId, String userEmail) {
        var permissions = permissionsDao.getPermissions(fileId);
        return permissions.ownerEmail.equals(userEmail);
    }

    /**
     * Gives read access to userEmail on fileId and its descendants
     * @param fileId
     * @param userEmail
     */
    public void giveReadAccess(String fileId, String userEmail) {
        var permissions = getPermissions(fileId);
        if (permissions.getAccessList() == null) permissions.setAccessList(new ArrayList<>());
        permissions.getAccessList().add(new Access(userEmail, AccessLevel.Read));
        permissionsDao.shareFileWith(fileId, userEmail);
        permissionsDao.savePermissions(permissions);
    }

    /**
     * Gives write access to userEmail on fileId and its descendants
     * @param fileId
     * @param userEmail
     */
    public void giveWriteAccess(String fileId, String userEmail) {
        var permissions = getPermissions(fileId);
        if (permissions.getAccessList() == null) permissions.setAccessList(new ArrayList<>());
        permissions.getAccessList().add(new Access(userEmail, AccessLevel.Write));
        permissionsDao.shareFileWith(fileId, userEmail);
        permissionsDao.savePermissions(permissions);
    }

    /**
     * Revokes access to userEmail on fileId.
     * Note that this does not revoke access on any of the descendants of fileId if given explicitly
     * @param fileId
     * @param userEmail
     */
    public void revokeAccess(String fileId, String userEmail) {
        var permissions = getPermissions(fileId);
        if (permissions.accessList == null) return;
        permissions.accessList.removeIf(acl -> acl.getUserEmail().equals(userEmail));
        permissionsDao.removeFileSharedWithMe(fileId, userEmail);
        permissionsDao.savePermissions(permissions);
    }

    /**
     * Gets FilePermissions for fileId.
     * Note that this only gives permission information given explicitly to this fileId
     * @param fileId
     * @return
     */
    public FilePermissions getPermissions(String fileId) {
        return permissionsDao.getPermissions(fileId);
    }

    /**
     * Adds a new file and creates appropriate access control list
     * @param parentId
     * @param fileId
     * @param ownerEmail
     */
    public void addNewFile(String parentId, String fileId, String ownerEmail) {
        FilePermissions permissions = new FilePermissions(parentId, fileId, ownerEmail);
        permissionsDao.savePermissions(permissions);
    }

    public List<String> filesSharedWithMe(String userEmail) {
        SharedFiles sharedFiles = permissionsDao.getFilesSharedWith(userEmail);
        if (sharedFiles == null || sharedFiles.filesSharedWithMe == null) return List.of();
        return sharedFiles.filesSharedWithMe;
    }
}
