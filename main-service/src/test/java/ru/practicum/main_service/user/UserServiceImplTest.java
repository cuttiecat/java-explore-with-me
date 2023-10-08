package ru.practicum.main_service.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.main_service.MainCommonUtils;
import ru.practicum.main_service.exception.NotFoundException;
import ru.practicum.main_service.user.dto.NewUserRequest;
import ru.practicum.main_service.user.dto.UserDto;
import ru.practicum.main_service.user.mapper.UserMapper;
import ru.practicum.main_service.user.model.User;
import ru.practicum.main_service.user.repository.UserRepository;
import ru.practicum.main_service.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private final NewUserRequest newUserRequest = NewUserRequest.builder()
            .email("test1@yandex.ru")
            .name("test name 1")
            .build();
    private final User user1 = User.builder()
            .id(1L)
            .email(newUserRequest.getEmail())
            .name(newUserRequest.getName())
            .build();
    private final User user2 = User.builder()
            .id(2L)
            .email("test2@yandex.ru")
            .name("test name 2")
            .build();
    private final Integer from = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_FROM);
    private final Integer size = Integer.parseInt(MainCommonUtils.PAGE_DEFAULT_SIZE);
    private final Pageable pageable = PageRequest.of(from / size, size);

    @Nested
    class Create {
        @Test
        public void shouldCreate() {
            when(userMapper.toUser(any())).thenReturn(user1);
            when(userMapper.toUserDto(any())).thenReturn(UserDto.builder().build());
            when(userRepository.save(any())).thenReturn(user1);

            UserDto savedUserDto = userService.create(newUserRequest);


            verify(userMapper, times(1)).toUser(any());
            verify(userMapper, times(1)).toUserDto(any());
            verify(userRepository, times(1)).save(userArgumentCaptor.capture());

            User savedUser = userArgumentCaptor.getValue();

            assertEquals(newUserRequest.getEmail(), savedUser.getEmail());
            assertEquals(newUserRequest.getName(), savedUser.getName());
        }
    }

    @Nested
    class GetUsers {
        @Test
        public void shouldGetAllIfIdIsNull() {
            when(userMapper.toUserDto(any())).thenReturn(UserDto.builder().build());
            when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user1, user2)));

            List<UserDto> usersDtoFromService = userService.getUsers(null, pageable);

            assertEquals(2, usersDtoFromService.size());

            verify(userMapper, times(2)).toUserDto(any());
            verify(userRepository, times(1)).findAll(pageable);
        }

        @Test
        public void shouldGetAllIfIdIsEmpty() {
            when(userMapper.toUserDto(any())).thenReturn(UserDto.builder().build());
            when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user1, user2)));

            List<UserDto> usersDtoFromService = userService.getUsers(List.of(), pageable);

            assertEquals(2, usersDtoFromService.size());

            verify(userMapper, times(2)).toUserDto(any());
            verify(userRepository, times(1)).findAll(pageable);
        }

        @Test
        public void shouldGetTwoById() {
            when(userMapper.toUserDto(any())).thenReturn(UserDto.builder().build());
            when(userRepository.findAllByIdIn(List.of(user1.getId(), user2.getId()), pageable))
                    .thenReturn(List.of(user1, user2));

            List<UserDto> usersDtoFromService = userService.getUsers(List.of(user1.getId(), user2.getId()), pageable);

            assertEquals(2, usersDtoFromService.size());

            verify(userMapper, times(2)).toUserDto(any());
            verify(userRepository, times(1)).findAllByIdIn(any(), any());
        }

        @Test
        public void shouldGetOneById() {
            when(userMapper.toUserDto(any())).thenReturn(UserDto.builder().build());
            when(userRepository.findAllByIdIn(List.of(user2.getId()), pageable))
                    .thenReturn(List.of(user2));

            List<UserDto> usersDtoFromService = userService.getUsers(List.of(user2.getId()), pageable);

            assertEquals(1, usersDtoFromService.size());

            verify(userMapper, times(1)).toUserDto(any());
            verify(userRepository, times(1)).findAllByIdIn(any(), any());
        }

        @Test
        public void shouldGetEmptyIfIdNotFound() {
            when(userRepository.findAllByIdIn(List.of(99L), pageable))
                    .thenReturn(List.of());

            List<UserDto> usersDtoFromService = userService.getUsers(List.of(99L), pageable);

            assertTrue(usersDtoFromService.isEmpty());

            verify(userRepository, times(1)).findAllByIdIn(any(), any());
        }
    }

    @Nested
    class DeleteById {
        @Test
        public void shouldDelete() {
            when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));

            userService.deleteById(user1.getId());

            verify(userRepository, times(1)).deleteById(user1.getId());
        }

        @Test
        public void shouldThrowExceptionIfNotUserFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.deleteById(99L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());

            verify(userRepository, never()).deleteById(any());
        }
    }

    @Nested
    class GetUserById {
        @Test
        public void shouldGet() {
            when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));

            User userFromService = userService.getUserById(user2.getId());

            assertEquals(user2.getId(), userFromService.getId());
            assertEquals(user2.getName(), userFromService.getName());
            assertEquals(user2.getEmail(), userFromService.getEmail());

            verify(userRepository, times(1)).findById(user2.getId());
        }

        @Test
        public void shouldThrowExceptionIfNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> userService.getUserById(99L));
            assertEquals("Пользователя с таким id не существует.", exception.getMessage());

            verify(userRepository, times(1)).findById(any());
        }
    }

}
