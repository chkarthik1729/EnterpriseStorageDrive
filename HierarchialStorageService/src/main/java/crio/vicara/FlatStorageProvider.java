package crio.vicara;

import java.io.InputStream;
import java.net.URL;

/**
 * Represents a StorageProvider that provides Flat Storage Structure
 */
public interface FlatStorageProvider {

    StorageProviderDetails getStorageProviderDetails();

    void putObject(String key, InputStream inputStream, long contentLength);

    void putObject(String key, InputStream inputStream);

    void putObject(String key, String content);

    void deleteObject(String key);

    InputStream getObject(String key);

    URL getObjectURL(String key, long urlExpirySeconds);

}
