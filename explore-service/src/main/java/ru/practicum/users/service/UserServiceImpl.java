package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Override
    public UserDto addUser(NewUserRequest user) {
        log.info("Add user: {}", user);
        return null;
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Get users: {}, from={}, size={}", ids, from, size);
        return List.of();
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Delete user: {}", id);
    }
}
