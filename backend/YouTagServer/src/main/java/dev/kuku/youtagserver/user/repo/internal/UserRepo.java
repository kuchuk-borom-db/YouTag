package dev.kuku.youtagserver.user.repo.internal;

import dev.kuku.youtagserver.user.entity.internal.User;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface UserRepo extends CrudRepository<User, String> {
    Optional<User> findByEmail(String email);
}
