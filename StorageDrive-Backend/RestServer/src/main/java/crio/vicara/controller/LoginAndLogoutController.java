package crio.vicara.controller;

import crio.vicara.service.StorageManager;
import crio.vicara.user.User;
import crio.vicara.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.nio.file.FileAlreadyExistsException;

@Validated
@RestController
@RequestMapping("/api")
public class LoginAndLogoutController {

    @Autowired UserService userService;

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
}
