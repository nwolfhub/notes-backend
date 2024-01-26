package org.nwolfhub.notes.database.repositories;

import org.nwolfhub.notes.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> { }
