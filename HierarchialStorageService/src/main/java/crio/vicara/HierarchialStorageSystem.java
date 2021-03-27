package crio.vicara;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

/**
 * Represents a Hierarchial Storage Service for Vicara Storage Drive
 */
public interface HierarchialStorageSystem {

    StorageServiceDetails getStorageProviderDetails();

    String createDirectory(String parentId, String directoryName) throws FileAlreadyExistsException;

    File getFile(String fileId);

    void delete(String fileId);

    List<ChildFile> listChildren(String fileId);

    void move(String fromId, String toId, boolean forced);

    void rename(String fileId, String newName) throws FileAlreadyExistsException;

    String uploadFile(String parentId, String fileName, InputStream stream, long length) throws FileAlreadyExistsException;

    String uploadFile(String parentId, String fileName, InputStream stream) throws FileAlreadyExistsException;

    URL downloadableFileURL(String fileId, long urlExpirySeconds);

    InputStream downloadFile(String fileId);
}