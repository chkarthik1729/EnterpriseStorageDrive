package crio.vicara.controller;

import crio.vicara.StorageServiceDetails;
import crio.vicara.service.StorageManager;
import crio.vicara.user.User;
import crio.vicara.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class LoginAndLogoutController {

    @Autowired UserService userService;
    @Autowired StorageManager storageManager;

    @PostMapping("/register")
    public void registerUser(@RequestBody @Valid User user) throws FileAlreadyExistsException {
        userService.registerUser(user);
    }

    @GetMapping("/sign-in")
    public void signIn() {
    }

    @GetMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }

    @GetMapping("/storage-providers")
    public List<StorageServiceDetails> getStorageServiceProvidersDetails() {
        return storageManager.getStorageServiceProvidersDetails();
    }
}
