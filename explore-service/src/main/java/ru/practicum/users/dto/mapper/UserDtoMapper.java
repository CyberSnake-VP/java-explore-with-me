package ru.practicum.users.dto.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserDtoMapper {

    public static UserDto mapToUserDto(final User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static User mapToUser(final UserDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getName())
                .build();
    }

    public static UserDto mapToUserDto(final NewUserRequest newUserRequest) {
        return UserDto.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

}
