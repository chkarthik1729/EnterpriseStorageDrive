package crio.vicara;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Represents a Hierarchial Storage Service for Vicara Storage Drive
 */
public interface HierarchialStorageProvider {

    StorageProviderDetails getStorageProviderDetails();

    String createDirectory(String parentId, String directoryName);

    File getFile(String fileId);

    void delete(String fileId);

    List<File> listChildren(String fileId);

    void move(String fromId, String toId, boolean forced);

    void rename(String fileId, String newName);

    String uploadFile(String parentId, String fileName, InputStream stream, long length);

    String uploadFile(String parentId, String fileName, InputStream stream);

    URL downloadableFileURL(String fileId, long urlExpirySeconds);

    InputStream downloadFile(String fileId);
}