package crio.vicara.service.favourites;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;

public class UserFavourites {
    @BsonId
    private String userEmail;

    private List<String> fileIds;

    public UserFavourites() {
    }

    public UserFavourites(String userEmail, List<String> fileIds) {
        this.userEmail = userEmail;
        this.fileIds = fileIds;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<String> getFileIds() {
        return fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }
}
