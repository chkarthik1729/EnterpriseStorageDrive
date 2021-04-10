package crio.vicara;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

public class File {

    public File() {
    }

    private String fileName;

    @BsonId
    private String fileId;

    private List<ChildFile> children;

    private String path;

    private boolean isDirectory;

    private long lastModifiedTime;

    private long creationTime;

    private String parentId;

    public List<ChildFile> getChildren() {
        return children;
    }

    public void setChildren(List<ChildFile> children) {
        this.children = children;
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
