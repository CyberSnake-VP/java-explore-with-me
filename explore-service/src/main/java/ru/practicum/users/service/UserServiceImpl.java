package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.dto.mapper.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto addUser(NewUserRequest user) {
        log.info("Add user: {}", user);
        User savedUser = UserMapper.mapToUser(user);

        if (checkEmailIsExisting(savedUser)) {
            log.info("User already exists with email: {}", user.getEmail());
            throw new IllegalArgumentException(
                    "Пользователь с email:" + user.getEmail() + " уже зарегистрирован!"
            );
        }

        savedUser = userRepository.save(savedUser);

        log.info("Saved user: {}", savedUser);
        return UserMapper.mapToUserDto(savedUser);
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



    private boolean checkEmailIsExisting(User user) {
        log.debug("user by email: {}", user.getEmail());
        return userRepository.findByEmail(user);
    }
}
