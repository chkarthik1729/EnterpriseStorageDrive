package crio.vicara.service.permissions;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

public class SharedFiles {

    @BsonId
    String userEmail;

    List<String> filesSharedWithMe;

    public SharedFiles() {
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<String> getFilesSharedWithMe() {
        return filesSharedWithMe;
    }

    public void setFilesSharedWithMe(List<String> filesSharedWithMe) {
        this.filesSharedWithMe = filesSharedWithMe;
    }
}
