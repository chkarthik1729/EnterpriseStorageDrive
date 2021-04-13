package crio.vicara;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.List;

/**
 * Represents a Hierarchial Storage Service for Vicara Storage Drive
 */
public interface HierarchicalStorageSystem {

    StorageServiceDetails getStorageProviderDetails();

    String createDirectory(String parentId, String directoryName) throws FileAlreadyExistsException;

    File getFile(String fileId);

    void delete(String fileId);

    String getFileIdByName(String parentId, String fileName);

    List<ChildFile> listChildren(String fileId);

    void move(String fromId, String toId, boolean forced) throws FileAlreadyExistsException, NotDirectoryException;

    void rename(String fileId, String newName) throws FileAlreadyExistsException;

    String uploadFile(String parentId, String fileName, InputStream stream, long length) throws FileAlreadyExistsException;

    String uploadFile(String parentId, String fileName, InputStream stream) throws FileAlreadyExistsException;

    URL downloadableFileURL(String fileId, long urlExpirySeconds) throws FileNotFoundException;

    InputStream downloadFile(String fileId) throws FileNotFoundException;

    long getLength(String fileId) throws FileNotFoundException;

    boolean exists(String fileId);

    String getFilePath(String fileId);

    void clearAll();
}