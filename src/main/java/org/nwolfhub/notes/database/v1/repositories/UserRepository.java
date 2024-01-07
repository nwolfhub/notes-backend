package org.nwolfhub.notes.database.v1.repositories;

import org.nwolfhub.notes.database.v1.model.User; //still uses legacy as there is no migration FOR NOW
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository extends JpaRepository<User, Integer> {
    User getUserById(Integer id);
    User getUserByUsername(String username);
    User findUserByUsername(String username);
}
