package dev.kuku.youtagserver.user.infrastructure.persistence;

import dev.kuku.youtagserver.user.domain.entity.User;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface UserRepo extends CrudRepository<User, String> {
    Optional<User> findByEmail(String email);
}
