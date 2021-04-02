package crio.vicara.service;

import crio.vicara.HierarchicalStorageSystem;
import crio.vicara.StorageServiceDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class StorageManager {

    private List<HierarchicalStorageSystem> storageSystems = new ArrayList<>();
    private HierarchicalStorageSystem defaultStorageSystem;

    public StorageManager() {
        storageSystems.add(
                HierarchicalStorageService
                        .with(AmazonSimpleStorageService.getInstance())
        );

        // Add Azure Blob Storage

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

    public String createUserFolder(String folderName, String storageProviderName) throws FileAlreadyExistsException {
        var storageSystem = findByName(storageProviderName);
        var originalFileId = storageSystem.createDirectory(null, folderName);
        return encodeFileWithStorageProvider(originalFileId, storageProviderName);
    }

    public String createFolder(String parentId, String folderName) throws FileAlreadyExistsException {
        var storageProviderName = decodeStorageProviderName(parentId);
        var storageSystem = findByName(storageProviderName);
        var originalParentId = decodeOriginalFileIdInsideStorageProvider(parentId);
        var originalFileId = storageSystem.createDirectory(originalParentId, folderName);
        return encodeFileWithStorageProvider(originalFileId, storageProviderName);
    }

    public void deleteFile(String fileId) {
        var storageProviderName = decodeStorageProviderName(fileId);
        var storageSystem = findByName(storageProviderName);
        var originalFileId = decodeOriginalFileIdInsideStorageProvider(fileId);
        storageSystem.delete(originalFileId);
    }

    private static String decodeStorageProviderName(String fileId) {
        return new String(Base64.getDecoder().decode(fileId)).split(":")[0];
    }

    private static String decodeOriginalFileIdInsideStorageProvider(String fileId) {
        return new String(Base64.getDecoder().decode(fileId)).split(":")[1];
    }

    private static String encodeFileWithStorageProvider(String fileId, String storageProvider) {
        return Base64.getEncoder().encodeToString((fileId + ":" + storageProvider).getBytes(StandardCharsets.UTF_8));
    }
}

