package crio.vicara.user;

import crio.vicara.exception.UnrecognizedStorageProviderException;
import crio.vicara.exception.UsernameAlreadyTakenException;
import crio.vicara.service.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserService implements UserDetailsService {

    @Autowired PasswordEncoder passwordEncoder;
    @Autowired StorageManager storageManager;
    @Autowired UserDao userDao;

    public void registerUser(User user) {
        if (userDao.existsEmail(user.getEmail())) {
            throw new UsernameAlreadyTakenException("Email already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getStorageProvider() == null) {
            user.setStorageProvider(
                    storageManager
                            .getDefaultStorageSystem()
                            .getStorageProviderDetails()
                            .getHyphenatedName()
            );
        } else if (storageManager.findByName(user.getStorageProvider()) == null) {
            throw new UnrecognizedStorageProviderException();
        }

        userDao.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userDao.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email + " not found");
        }
        return new org.springframework.security.core.userdetails.
                User(user.getEmail(), user.getPassword(), List.of());
    }
}
