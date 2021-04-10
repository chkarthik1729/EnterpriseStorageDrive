package crio.vicara.service.permissions;

public class Access {
    private String userEmail;
    private AccessLevel level;

    public Access() {
    }

    public Access(String userEmail, AccessLevel access) {
        this.userEmail = userEmail;
        this.level = access;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public AccessLevel getLevel() {
        return level;
    }

    public void setLevel(AccessLevel level) {
        this.level = level;
    }
}
