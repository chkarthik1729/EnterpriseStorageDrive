package crio.vicara.service;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import crio.vicara.*;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class HierarchialStorageService implements HierarchialStorageSystem {

    private final FlatStorageSystem flatStorageSystem;
    private final MongoDataAccessObject mongoDao;
    private final String rootId;

    private HierarchialStorageService(FlatStorageSystem provider) {
        this.flatStorageSystem = provider;

        var databaseName = flatStorageSystem
                .getStorageProviderDetails()
                .getHyphenatedName();

        var mongoClient = configureAndGetMongoClient();
        var mongoDatabase = mongoClient.getDatabase(databaseName);
        var fileTreeCollection = mongoDatabase.getCollection("file-tree", File.class);

        mongoDao = new MongoDataAccessObject(fileTreeCollection);
        rootId = mongoDao.addToFileCollection(createFile(null, "Root", true));
    }

    private static MongoClient configureAndGetMongoClient() {
        var connectionString = new ConnectionString("mongodb://localhost");
        var pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        var codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        var clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();
        return MongoClients.create(clientSettings);
    }

    public static HierarchialStorageService with(FlatStorageSystem provider) {
        return new HierarchialStorageService(provider);
    }

    @Override
    public StorageServiceDetails getStorageProviderDetails() {
        return flatStorageSystem.getStorageProviderDetails();
    }

    @Override
    public String createDirectory(String parentId, String directoryName) throws FileAlreadyExistsException {
        if (parentId == null) parentId = rootId;
        return addNewFileToDatabase(createFile(parentId, directoryName, true));
    }

    @Override
    public File getFile(String fileId) {
        return mongoDao.findFile(fileId);
    }

    @Override
    public void delete(String fileId) {
        // Can't delete root folder
        if (fileId.equals(rootId)) return;

        File file = mongoDao.findFile(fileId);
        if (file.getParentId() != null)
            mongoDao.deleteChildFromParent(file.getParentId(), file.getFileName());
        deleteRecurse(file);

        mongoDao.updateLastModified(file.getParentId(), System.currentTimeMillis());
    }

    private void deleteRecurse(File file) {
        flatStorageSystem.deleteObject(file.getFileId());
        mongoDao.delete(file.getFileId());

        for (ChildFile child : file.getChildren()) {
            deleteRecurse(mongoDao.findFile(child.getFileId()));
        }
    }

    @Override
    public List<ChildFile> listChildren(String fileId) {
        return mongoDao.findFile(fileId).getChildren();
    }

    @Override
    public void move(String fromId, String toId, boolean forced) {
        File file = mongoDao.findFile(fromId);
        ChildFile duplicateChildFile = mongoDao.findChildInParent(toId, file.getFileName());

        if (duplicateChildFile != null) {
            if (forced) {
                delete(duplicateChildFile.getFileId());
                mongoDao.deleteChildFromParent(toId, duplicateChildFile.getFileName());
                mongoDao.updateLastModified(toId, System.currentTimeMillis());
            } else return;
        }

        mongoDao.deleteChildFromParent(file.getParentId(), file.getFileName());
        mongoDao.updateLastModified(file.getParentId(), System.currentTimeMillis());
        ChildFile currFileChildVersion = createChildFile(file.getFileId(), file.getFileName(), file.isDirectory());
        mongoDao.addChildFileToParent(file.getParentId(), currFileChildVersion);
        mongoDao.updateLastModified(file.getParentId(), System.currentTimeMillis());
    }

    @Override
    public void rename(String fileId, String newName) throws FileAlreadyExistsException {
        File file = mongoDao.findFile(fileId);
        ensureSameFileNameDoesNotExist(file.getParentId(), newName);
        mongoDao.updateChildFileNameInParent(file.getParentId(), file.getFileName(), newName);
        mongoDao.updateFileName(fileId, newName);
        mongoDao.updateLastModified(fileId, System.currentTimeMillis());
    }

    @Override
    public String uploadFile(String parentId, String fileName, InputStream stream, long length) throws FileAlreadyExistsException {
        File file = createFile(parentId, fileName, false);
        addNewFileToDatabase(file);
        flatStorageSystem.putObject(file.getFileId(), stream, length);
        return file.getFileId();
    }

    @Override
    public String uploadFile(String parentId, String fileName, InputStream stream) throws FileAlreadyExistsException {
        File file = createFile(parentId, fileName, false);
        addNewFileToDatabase(file);
        flatStorageSystem.putObject(file.getFileId(), stream);
        return file.getFileId();
    }

    @Override
    public URL downloadableFileURL(String fileId, long urlExpirySeconds) {
        return flatStorageSystem.getObjectURL(fileId, urlExpirySeconds);
    }

    @Override
    public InputStream downloadFile(String fileId) {
        return flatStorageSystem.getObject(fileId);
    }

    private void ensureSameFileNameDoesNotExist(String parentId, String fileName) throws FileAlreadyExistsException {
        ChildFile otherFile = mongoDao.findChildInParent(parentId, fileName);
        if (otherFile != null) throw new FileAlreadyExistsException("A file with the name already exists");
    }

    String addNewFileToDatabase(File file) throws FileAlreadyExistsException {
        ensureSameFileNameDoesNotExist(file.getParentId(), file.getFileName());
        ChildFile childFile = createChildFile(file.getFileId(), file.getFileName(), file.isDirectory());
        mongoDao.addChildFileToParent(file.getParentId(), childFile);
        return mongoDao.addToFileCollection(file);
    }

    ChildFile createChildFile(String fileId, String fileName, boolean isDirectory) {
        return new ChildFile(fileId, fileName, isDirectory);
    }

    File createFile(String parentId, String fileName, boolean isDirectory) {
        File file = new File();
        ObjectId id = new ObjectId();
        file.setFileId(id.toHexString());
        file.setParentId(parentId);
        file.setFileName(fileName);
        file.setDirectory(isDirectory);
        file.setCreationTime(System.currentTimeMillis());
        file.setLastModifiedTime(System.currentTimeMillis());
        return file;
    }
}
