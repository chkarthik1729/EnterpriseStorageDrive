package crio.vicara.service;

import crio.vicara.File;
import crio.vicara.HierarchicalStorageSystem;
import crio.vicara.StorageServiceDetails;
import crio.vicara.exception.UnauthorizedException;
import crio.vicara.service.favourites.Favourites;
import crio.vicara.service.permissions.PermissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.*;

@Component
public class StorageManager {

    private List<HierarchicalStorageSystem> storageSystems = new ArrayList<>();
    private HierarchicalStorageSystem defaultStorageSystem;

    @Autowired
    private PermissionManager permissionManager;

    @Autowired
    private Favourites favourites;

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
        var originalFileId = storageSystem.uploadFile(originalParentId, fileName, stream, length);
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

    public File getUserRootFolder(String userEmail, String storageProviderName) {
        var storageSystem = findByName(storageProviderName);

        File file = storageSystem.getFile(storageSystem.getFileIdByName(null, userEmail));
        file.getChildren().forEach(
                childFile -> childFile.setFileId(
                        encodeFileWithStorageProvider(childFile.getFileId(),
                                storageProviderName)
                ));
        file.setFileId(encodeFileWithStorageProvider(file.getFileId(), storageProviderName));
        file.setParentId(encodeFileWithStorageProvider(file.getParentId(), storageProviderName));
        return file;
    }

    public Object getFileOrFolder(String fileId, String userEmail) {
        var storageProviderName = decodeStorageProviderName(fileId);
        var storageSystem = findByName(storageProviderName);

        String originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);

        if (!permissionManager.hasReadAccess(fileId, userEmail))
            throw new UnauthorizedException();

        if (storageSystem.getFile(originalFileId).isDirectory()) {
            File file = storageSystem.getFile(originalFileId);
            file.getChildren().forEach(
                    childFile -> childFile.setFileId(
                            encodeFileWithStorageProvider(childFile.getFileId(),
                                    storageProviderName)
                    ));
            file.setFileId(encodeFileWithStorageProvider(file.getFileId(), storageProviderName));
            file.setParentId(encodeFileWithStorageProvider(file.getParentId(), storageProviderName));
            return file;
        }

        if (permissionManager.isOwner(fileId, userEmail))
            return storageSystem.downloadableFileURL(originalFileId, 3600);

        else return storageSystem.downloadFile(originalFileId);
    }

    public void move(String fromId, String toId, boolean forced, String userEmail) throws FileAlreadyExistsException, NotDirectoryException {
        if (!decodeStorageProviderName(fromId).equals(decodeStorageProviderName(toId)))
            throw new UnauthorizedException();
        if (!permissionManager.hasWriteAccess(fromId, userEmail)
                || !permissionManager.hasWriteAccess(toId, userEmail))
            throw new UnauthorizedException();

        var storageProviderName = decodeStorageProviderName(fromId);
        var storageSystem = findByName(storageProviderName);

        storageSystem.move(
                decodeOriginalFileIdInsideStorageProvider(fromId),
                decodeStorageProviderName(toId),
                forced
        );
    }

    public FileDetails getFileDetails(String fileId, String userEmail) {
        if (!permissionManager.hasReadAccess(fileId, userEmail))
            throw new UnauthorizedException();

        FileDetails fileDetails = new FileDetails();
        var storageProviderName = decodeStorageProviderName(fileId);
        var storageSystem = findByName(storageProviderName);
        var originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);
        File file = storageSystem.getFile(originalFileId);

        fileDetails.setFileName(file.getFileName());
        fileDetails.setFilePath(storageSystem.getFilePath(originalFileId));
        fileDetails.setFileId(fileId);
        fileDetails.setDirectory(file.isDirectory());
        fileDetails.setFavourite(favourites.isFavourite(fileId, userEmail));
        fileDetails.setCreatedAt(file.getCreationTime());
        fileDetails.setLength(storageSystem.getLength(originalFileId));
        fileDetails.setLastModified(file.getLastModifiedTime());

        return fileDetails;
    }

    public FileDetails patchFileDetails(String fileId, String userEmail, Map<String, String> changes) throws FileAlreadyExistsException {
        if (!permissionManager.hasWriteAccess(fileId, userEmail)) throw new UnauthorizedException();

        var storageSystem = findByName(decodeStorageProviderName(fileId));
        var originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);

        if (changes.containsKey("fileName")) {
            storageSystem.rename(originalFileId, changes.get("fileName"));
        }

        if (changes.containsKey("favourite")) {
            boolean fav = Boolean.parseBoolean(changes.get("favourite"));
            if (fav) favourites.addFavourite(fileId, userEmail);
            else favourites.removeFavourite(fileId, userEmail);
        }

        return getFileDetails(fileId, userEmail);
    }

    public List<FileDetails> getFavourites(String userEmail) {
        List<FileDetails> favouriteFiles = new ArrayList<>();
        favourites.getFavourites(userEmail).forEach(favFile -> favouriteFiles.add(getFileDetails(favFile, userEmail)));
        return favouriteFiles;
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

