package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto add(NewUserRequest user) {
        log.info("Add user: {}", user);
        User savedUser = UserMapper.mapToUser(user);

        if (checkEmailIsExisting(savedUser)) {
            log.info("User already exists with email: {}", user.getEmail());
            String message = "User already exists with email: " + user.getEmail();
            throw new ValidationException(message);
        }

        savedUser = userRepository.save(savedUser);

        log.info("Saved user: {}", savedUser);
        return UserMapper.mapToUserDto(savedUser);
    }

    @Override
    public List<UserDto> get(List<Long> ids, Pageable pageable) {
        log.info("Get users ids: {}, from={}, size={}", ids, pageable.getPageNumber(), pageable.getPageSize());
        /** Если идентификаторы пользователей не указаны, тогда вернем всех пользователей с учетом ограничений.*/
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(pageable).stream()
                    .map(UserMapper::mapToUserDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findByIdIn(ids).stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        log.info("Delete user: {}", userId);

        if (userRepository.existsById(userId)) {
            log.info("User already exists with id: {} and deleted", userId);
            userRepository.deleteById(userId);
        } else {
            String reason = "The required object was not found.";
            String message = String.format("User with id=%d  was not found", userId);
            throw new NotFoundException(message, reason);
        }
    }


    private boolean checkEmailIsExisting(User user) {
        log.debug("user by email: {}", user.getEmail());
        return userRepository.findByEmail(user.getEmail()).isPresent();
    }
}
