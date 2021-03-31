package crio.vicara;

public class ChildFile {
    private String fileId;
    private String fileName;
    private boolean isDirectory;

    public ChildFile() {
    }

    public ChildFile(String fileId, String fileName, boolean isDirectory) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.isDirectory = isDirectory;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    @Override
    public String toString() {
        return String.format("fileId: %s - fileName: %s - isDirectory: %s", fileId, fileName, isDirectory);
    }
}
