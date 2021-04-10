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
import java.nio.file.NotDirectoryException;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class HierarchicalStorageService implements HierarchicalStorageSystem {

    private final FlatStorageSystem flatStorageSystem;
    private final MongoDataAccessObject mongoDao;
    private String rootId;

    private HierarchicalStorageService(FlatStorageSystem provider) {
        this.flatStorageSystem = provider;

        var databaseName = flatStorageSystem
                .getStorageProviderDetails()
                .getHyphenatedName();

        var mongoClient = configureAndGetMongoClient();
        var mongoDatabase = mongoClient.getDatabase(databaseName);
        var fileTreeCollection = mongoDatabase.getCollection("fileTree", File.class);

        mongoDao = new MongoDataAccessObject(fileTreeCollection);
        rootId = mongoDao.createRootFolderIfDoesNotExist();
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

    public static HierarchicalStorageService with(FlatStorageSystem provider) {
        return new HierarchicalStorageService(provider);
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
    public String getFileIdByName(String parentId, String fileName) {
        if (parentId == null)
            parentId = rootId;
        return mongoDao.findChildInParent(parentId, fileName).getFileId();
    }

    @Override
    public void delete(String fileId) {
        // Can't delete root folder
        if (rootId.equals(fileId)) return;

        File file = mongoDao.findFile(fileId);

        if (file.getParentId() != null) {
            mongoDao.deleteChildFromParent(file.getParentId(), file.getFileName());
            mongoDao.updateLastModified(file.getParentId(), System.currentTimeMillis());
        }

        deleteRecurse(file);
    }

    private void deleteRecurse(File file) {
        if (file.isDirectory())
            file.getChildren().forEach(
                            child -> deleteRecurse(mongoDao.findFile(child.getFileId()))
                    );
        else flatStorageSystem.deleteObject(file.getFileId());
        mongoDao.delete(file.getFileId());
    }

    @Override
    public List<ChildFile> listChildren(String fileId) {
        return mongoDao.findFile(fileId).getChildren();
    }

    @Override
    public void move(String sourceId, String destinationId, boolean forced) throws FileAlreadyExistsException, NotDirectoryException {
        File destinationFile = mongoDao.findFile(destinationId);
        if (!destinationFile.isDirectory())
            throw new NotDirectoryException("Destination is not a directory");

        //TODO: Moving a parent to its child should not work

        File sourceFile = mongoDao.findFile(sourceId);

        ChildFile duplicateFileInDestination = mongoDao.findChildInParent(destinationId, sourceFile.getFileName());

        if (duplicateFileInDestination != null) {
            if (forced) delete(duplicateFileInDestination.getFileId());
            else throw new FileAlreadyExistsException("A file with the same name in destination folder already exists");
        }

        // Delete source from its parent
        mongoDao.deleteChildFromParent(sourceFile.getParentId(), sourceFile.getFileName());
        mongoDao.updateLastModified(sourceFile.getParentId(), System.currentTimeMillis());

        // Add source to destination
        ChildFile sourceFileChildVersion = createChildFile(sourceFile.getFileId(), sourceFile.getFileName(), sourceFile.isDirectory());
        mongoDao.addChildFileToParent(destinationFile.getFileId(), sourceFileChildVersion);
        mongoDao.updateParentId(sourceId, destinationId);
        mongoDao.updateLastModified(destinationFile.getFileId(), System.currentTimeMillis());
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

    @Override
    public long getLength(String fileId) {
        File file = mongoDao.findFile(fileId);

        if (file.isDirectory()) {
            long totalLength = 0;
            for (ChildFile childFile: file.getChildren())
                totalLength += getLength(childFile.getFileId());
            return totalLength;
        }

        return flatStorageSystem.getLength(fileId);
    }

    @Override
    public boolean exists(String fileId) {
        return mongoDao.findFile(fileId) != null;
    }

    @Override
    public void clearAll() {
        mongoDao.clearAllDocuments();
        flatStorageSystem.clearAll();
        rootId = mongoDao.addFile(createFile(null, "Root", true));
    }

    @Override
    public String getFilePath(String fileId) {
        if (fileId.equals(rootId)) return "";

        StringBuilder path = new StringBuilder();
        File file = getFile(fileId);
        path.append(getFilePath(file.getParentId())).append("/").append(file.getFileName());
        return path.toString();
    }

    private void ensureSameFileNameDoesNotExist(String parentId, String fileName) throws FileAlreadyExistsException {
        ChildFile otherFile = mongoDao.findChildInParent(parentId, fileName);
        if (otherFile != null) throw new FileAlreadyExistsException("A file with the name already exists");
    }

    String addNewFileToDatabase(File file) throws FileAlreadyExistsException {
        ensureSameFileNameDoesNotExist(file.getParentId(), file.getFileName());
        ChildFile childFile = createChildFile(file.getFileId(), file.getFileName(), file.isDirectory());
        mongoDao.addChildFileToParent(file.getParentId(), childFile);
        return mongoDao.addFile(file);
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
