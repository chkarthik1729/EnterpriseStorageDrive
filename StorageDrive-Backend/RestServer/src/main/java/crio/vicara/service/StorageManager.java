package crio.vicara.service;

import crio.vicara.HierarchicalStorageSystem;
import crio.vicara.StorageServiceDetails;
import crio.vicara.exception.UnauthorizedException;
import crio.vicara.service.permission.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class StorageManager {

    private List<HierarchicalStorageSystem> storageSystems = new ArrayList<>();
    private HierarchicalStorageSystem defaultStorageSystem;

    @Autowired
    PermissionManager permissionManager;

    public StorageManager() {
        storageSystems.add(
                HierarchicalStorageService
                        .with(AmazonSimpleStorageService.getInstance())
        );

        //TODO: Add Azure Blob Storage

        defaultStorageSystem = storageSystems.get(0);
    }

    public HierarchicalStorageSystem findByName(String storageProvider) {
        for (var storageSystem : storageSystems)
            if (storageSystem
                    .getStorageProviderDetails()
                    .getHyphenatedName()
                    .equals(storageProvider)
            )
                return storageSystem;
        return null;
    }

    public HierarchicalStorageSystem getDefaultStorageSystem() {
        return defaultStorageSystem;
    }

    public List<StorageServiceDetails> getStorageServiceProvidersDetails() {
        var storageServiceProvidersDetails = new ArrayList<StorageServiceDetails>();
        storageSystems.forEach(
                service -> storageServiceProvidersDetails
                        .add(service.getStorageProviderDetails())
        );
        return storageServiceProvidersDetails;
    }

    public String createUserRootFolder(String userEmail, String storageProviderName) throws FileAlreadyExistsException {
        var storageSystem = findByName(storageProviderName);
        var originalFileId = storageSystem.createDirectory(null, userEmail);
        var finalFileId = encodeFileWithStorageProvider(originalFileId, storageProviderName);
        permissionManager.addNewFile(null, finalFileId, userEmail);
        return finalFileId;
    }

    public String createFolderInsideUserRootFolder(String folderName,
                                                   String storageProviderName,
                                                   String userEmail) throws FileAlreadyExistsException {
        var storageSystem = findByName(storageProviderName);
        var userRootFolderId = storageSystem.getFileIdByName(null, userEmail);
        var originalFileId = storageSystem.createDirectory(userRootFolderId, folderName);
        var finalFileId = encodeFileWithStorageProvider(originalFileId, storageProviderName);
        permissionManager.addNewFile(encodeFileWithStorageProvider(userRootFolderId, storageProviderName), finalFileId, userEmail);
        return finalFileId;
    }

    public String createFolder(String parentId, String folderName, String userEmail) throws FileAlreadyExistsException {
        if (!permissionManager.hasWriteAccess(parentId, userEmail))
            throw new UnauthorizedException();
        var storageProviderName = decodeStorageProviderName(parentId);
        var storageSystem = findByName(storageProviderName);
        var originalParentId = decodeOriginalFileIdInsideStorageProvider(parentId);
        var originalFileId = storageSystem.createDirectory(originalParentId, folderName);
        var finalFileId = encodeFileWithStorageProvider(originalFileId, storageProviderName);
        permissionManager.addNewFile(parentId, finalFileId, userEmail);
        return finalFileId;
    }

    public String uploadFileInsideUserRootFolder(String fileName,
                                                 InputStream stream,
                                                 long length,
                                                 String storageProviderName,
                                                 String userEmail) throws FileAlreadyExistsException {

        var storageSystem = findByName(storageProviderName);
        var userRootFolderId = storageSystem.getFileIdByName(null, userEmail);
        var originalFileId = storageSystem.uploadFile(userRootFolderId, fileName, stream, length);
        var finalFileId = encodeFileWithStorageProvider(originalFileId, storageProviderName);
        permissionManager.addNewFile(encodeFileWithStorageProvider(userRootFolderId, storageProviderName), finalFileId, userEmail);
        return finalFileId;
    }

    public String uploadFile(String parentId, String fileName, InputStream stream, long length, String userEmail) throws FileAlreadyExistsException {
        if (!permissionManager.hasWriteAccess(parentId, userEmail))
            throw new UnauthorizedException();
        var storageProviderName =  decodeStorageProviderName(parentId);
        var storageSystem = findByName(storageProviderName);
        var originalParentId = decodeOriginalFileIdInsideStorageProvider(parentId);
        var originalFileId = storageSystem.uploadFile(parentId, fileName, stream, length);
        return encodeFileWithStorageProvider(originalFileId, storageProviderName);
    }

    /**
     * Deletes fileId if the userEmail has write access on that fileId
     * @param fileId
     * @param userEmail
     */
    public void deleteFile(String fileId, String userEmail) {
        if (!permissionManager.hasWriteAccess(fileId, userEmail))
            throw new UnauthorizedException();
        var storageProviderName = decodeStorageProviderName(fileId);
        var storageSystem = findByName(storageProviderName);
        var originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);
        storageSystem.delete(originalFileId);
    }

    public Object getFileOrFolder(String fileId, String userEmail) {
        var storageProviderName = decodeStorageProviderName(fileId);
        var storageSystem = findByName(storageProviderName);
        var originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);

        if (permissionManager.isOwner(fileId, userEmail)) {
            return storageSystem.downloadableFileURL(originalFileId, 3600);
        } else if (permissionManager.hasReadAccess(fileId, userEmail)) {
            return storageSystem.downloadFile(originalFileId);
        }
        throw new UnauthorizedException();
    }

    public String getFileName(String fileId) {
        var storageProviderName = decodeStorageProviderName(fileId);
        var storageSystem = findByName(storageProviderName);
        var originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);
        return storageSystem.getFile(originalFileId).getFileName();
    }

    public long getLength(String fileId) {
        var storageProviderName = decodeStorageProviderName(fileId);
        var storageSystem = findByName(storageProviderName);
        var originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);
        return storageSystem.getLength(originalFileId);
    }

    /**
     * Gets storage provider name from fileId
     * @param fileId
     * @return
     */
    public static String decodeStorageProviderName(String fileId) {
        return new String(Base64.getDecoder().decode(fileId), StandardCharsets.UTF_8).split(":")[0];
    }

    /**
     * Gets original fileId in the storage provider by decoding this fileId
     * @param fileId
     * @return
     */
    public static String decodeOriginalFileIdInsideStorageProvider(String fileId) {
        return new String(Base64.getDecoder().decode(fileId), StandardCharsets.UTF_8).split(":")[1];
    }

    /**
     * Encodes this fileId and storage provider to create a unique fileId for end user
     * @param fileId
     * @param storageProvider
     * @return
     */
    public static String encodeFileWithStorageProvider(String fileId, String storageProvider) {
        return Base64.getEncoder().encodeToString((storageProvider + ":" + fileId).getBytes(StandardCharsets.UTF_8));
    }
}

