package ru.practicum.users;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.service.UserService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Valid @RequestBody final NewUserRequest user) {
        log.info("POST by add user: {}", user);
        return userService.addUser(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(name = "ids", required = false) List<Long> ids,
                                  @RequestParam(name = "from", defaultValue = "0") Integer from,
                                  @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return userService.getUsers(ids, PageRequest.of(from, size));
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable(value = "userId") @Positive Long userId) {
         userService.deleteUser(userId);
    }

}
