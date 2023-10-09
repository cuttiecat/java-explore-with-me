package ru.practicum.user.service;

import org.springframework.lang.Nullable;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(@Nullable List<Long> ids, Integer from, Integer size);

    UserDto addUser(UserDto userDto);

    void deleteUser(Long userId);

    User checkUserExistAndGet(Long userId);
}
