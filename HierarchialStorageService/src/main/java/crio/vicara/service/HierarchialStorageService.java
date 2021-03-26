package crio.vicara.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import crio.vicara.File;
import crio.vicara.FlatStorageProvider;
import crio.vicara.HierarchialStorageProvider;
import crio.vicara.StorageProviderDetails;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class HierarchialStorageService implements HierarchialStorageProvider {

    private final FlatStorageProvider storageProvider;
    private static final String FILETREE = "file-tree";

    private HierarchialStorageService(FlatStorageProvider provider) {
        this.storageProvider = provider;
    }

    public static HierarchialStorageService with(FlatStorageProvider provider) {
        HierarchialStorageService storageService = new HierarchialStorageService(provider);
        try (MongoClient mongoClient = MongoClients.create()) {
            String databaseName = storageService.storageProvider.getStorageProviderDetails().getHyphenatedName();
            MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
            mongoDatabase.createCollection(FILETREE);
        }
        return storageService;
    }

    @Override
    public StorageProviderDetails getStorageProviderDetails() {
        return storageProvider.getStorageProviderDetails();
    }

    @Override
    public String createDirectory(String parentId, String directoryName) {
        return null;
    }

    @Override
    public File getFile(String fileId) {
        return null;
    }

    @Override
    public void delete(String fileId) {

    }

    @Override
    public List<File> listChildren(String fileId) {
        return null;
    }

    @Override
    public void move(String fromId, String toId, boolean forced) {

    }

    @Override
    public void rename(String fileId, String newName) {

    }

    @Override
    public String uploadFile(String parentId, String fileName, InputStream stream, long length) {
        return null;
    }

    @Override
    public String uploadFile(String parentId, String fileName, InputStream stream) {
        return null;
    }

    @Override
    public URL downloadableFileURL(String fileId, long urlExpirySeconds) {
        return storageProvider.getObjectURL(fileId, urlExpirySeconds);
    }

    @Override
    public InputStream downloadFile(String fileId) {
        return storageProvider.getObject(fileId);
    }
}
