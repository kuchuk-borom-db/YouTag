package dev.kuku.youtagserver.user.infrastructure.repo;

import dev.kuku.youtagserver.user.domain.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends CrudRepository<User, String> {
    Optional<User> findByEmail(String email);
    void deleteById(String id);
}
