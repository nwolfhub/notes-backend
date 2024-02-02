package org.nwolfhub.notes.database.repositories;

import org.nwolfhub.notes.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
public interface UserRepository extends JpaRepository<User, String> {
    List<User> findUsersByUsernameLikeIgnoreCase(String username);

}
