package dev.kuku.youtagserver.user.services;

import dev.kuku.youtagserver.user.repo.UserRepo;
import dev.kuku.youtagserver.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    final UserRepo userRepo;
    @Transactional
    public boolean addUserToDb(String email, String name, String pic) {
        // Check if user already exists
        if (userRepo.existsById(email)) {
            return false;
        }
        // Create and save new user
        var user = new User(email, name, pic);
        userRepo.save(user);
        return true;
    }

    @Transactional
    public boolean updateUser(String email, String name, String pic) {
        if (!userRepo.existsById(email)) {
            return false;
        }
        var user = new User(email, name, pic);
        userRepo.save(user);
        return true;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepo.findById(email);
    }
}
