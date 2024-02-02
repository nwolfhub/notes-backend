package org.nwolfhub.notes.database.repositories;

import org.nwolfhub.notes.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    List<User> findUsersByUsernameLikeIgnoreCase(String username);

}
