package dev.kuku.youtagserver.user.domain.repo;

import dev.kuku.youtagserver.user.domain.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<User, String> {
}
