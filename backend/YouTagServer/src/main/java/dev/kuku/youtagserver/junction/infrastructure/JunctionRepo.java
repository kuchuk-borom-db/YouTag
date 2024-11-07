package dev.kuku.youtagserver.junction.infrastructure;

import dev.kuku.youtagserver.junction.domain.Junction;
import dev.kuku.youtagserver.junction.domain.JunctionId;
import org.springframework.data.repository.CrudRepository;

public interface JunctionRepo extends CrudRepository<Junction, JunctionId> {
}
