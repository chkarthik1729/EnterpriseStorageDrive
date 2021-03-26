package crio.vicara;

public class File {

    private final HierarchialStorageProvider storage;

    public File(HierarchialStorageProvider storage) {
        this.storage = storage;
    }

    private String fileName;

    private String fileId;

    private String path;

    private boolean isDirectory;

    private long lastModifiedTime;

    private long creationTime;

    private String parentId;

    private long length;

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    File getParent() {
        return storage.getFile(parentId);
    }

    public HierarchialStorageProvider getStorage() {
        return storage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFile() {
        return !isDirectory;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(long lastModified) {
        this.lastModifiedTime = lastModified;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
