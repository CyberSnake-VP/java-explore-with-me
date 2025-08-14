package ru.practicum.users.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(NewUserRequest user);
    List<UserDto> getUsers(List<Long> ids, Pageable pageable);
    void deleteUser(Long id);
}
