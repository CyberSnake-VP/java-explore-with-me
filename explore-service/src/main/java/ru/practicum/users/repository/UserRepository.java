package ru.practicum.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByIdIn(List<Long> ids);
}
