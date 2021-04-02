package crio.vicara.user;

import org.bson.codecs.pojo.annotations.BsonId;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class User {
    @Id
    @NotNull
    @Email(regexp = ".+@.+\\..+", message = "Given string is not a valid email")
    @BsonId
    private String email;

    @NotNull
    @Size(min = 5, message = "Password must be atleast 5 characters long")
    private String password;

    private String storageProvider;

    public User() {
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public void setStorageProvider(String storageProvider) {
        this.storageProvider = storageProvider;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
