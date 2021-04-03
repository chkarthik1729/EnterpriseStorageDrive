package crio.vicara.service.permission;

import org.bson.codecs.pojo.annotations.BsonId;

import java.io.File;
import java.util.List;

public class FilePermissions {

    @BsonId
    String fileId;

    String parentId;

    String ownerEmail;

    List<Access> accessList;

    public FilePermissions(){
    }

    public FilePermissions(String parentId, String fileId, String ownerEmail) {
        this.parentId = parentId;
        this.fileId = fileId;
        this.ownerEmail = ownerEmail;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public List<Access> getAccessList() {
        return accessList;
    }

    public void setAccessList(List<Access> accessList) {
        this.accessList = accessList;
    }
}

enum AccessLevel {
    Read, Write
}

class Access {
    String userEmail;
    AccessLevel access;

    public Access(String userEmail, AccessLevel access) {
        this.userEmail = userEmail;
        this.access = access;
    }
}
