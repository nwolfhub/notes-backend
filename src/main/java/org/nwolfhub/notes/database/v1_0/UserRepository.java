package org.nwolfhub.notes.database.v1_0;

import org.nwolfhub.notes.database.legacy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User getUserById(Integer id);
    User getUserByUsername(String username);
}
