package ru.practicum.users.service;

import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(NewUserRequest user);
    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);
    void deleteUser(Long id);
}
