package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setName("ivan");
        testUser1.setEmail("ivan@yandex.ru");
        userRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setName("sofa");
        testUser2.setEmail("sofa@yandex.ru");
        userRepository.save(testUser2);
    }

    @Test
    void getList() {
        List<UserResponseDto> result = userService.getList();

        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("ivan", "sofa");
        assertThat(result).extracting("email").containsExactlyInAnyOrder("ivan@yandex.ru", "sofa@yandex.ru");
    }

    @Test
    void getById() {
        UserResponseDto result = userService.getById(testUser1.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser1.getId());
        assertThat(result.getName()).isEqualTo("ivan");
        assertThat(result.getEmail()).isEqualTo("ivan@yandex.ru");
    }

    @Test
    void getByIdUserNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getById(99999L));
    }

    @Test
    void create() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("New User");
        createDto.setEmail("newuser@yandex.ru");

        UserResponseDto result = userService.create(createDto);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getEmail()).isEqualTo("newuser@yandex.ru");
        assertThat(result.getId()).isNotNull();

        assertThat(userRepository.findById(result.getId())).isPresent();
    }

    @Test
    void createDuplicateEmail() {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Another User");
        createDto.setEmail("ivan@yandex.ru");

        assertThrows(ConflictException.class, () -> userService.create(createDto));
    }

    @Test
    void update() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("updated ivan");
        updateDto.setEmail("ivan2@yandex.ru");

        UserResponseDto result = userService.update(updateDto, testUser1.getId());

        assertThat(result.getName()).isEqualTo("updated ivan");
        assertThat(result.getEmail()).isEqualTo("ivan2@yandex.ru");
        assertThat(result.getId()).isEqualTo(testUser1.getId());
    }

    @Test
    void updateOnlyName() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("updated ivan");

        UserResponseDto result = userService.update(updateDto, testUser1.getId());

        assertThat(result.getName()).isEqualTo("updated ivan");
        assertThat(result.getEmail()).isEqualTo("ivan@yandex.ru"); // Остался прежним
        assertThat(result.getId()).isEqualTo(testUser1.getId());
    }

    @Test
    void updateOnlyEmail() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("ivan.updated@yandex.ru");

        UserResponseDto result = userService.update(updateDto, testUser1.getId());

        assertThat(result.getName()).isEqualTo("ivan");
        assertThat(result.getEmail()).isEqualTo("ivan.updated@yandex.ru");
        assertThat(result.getId()).isEqualTo(testUser1.getId());
    }

    @Test
    void updateUserNotFound() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("updated Name");

        assertThrows(NotFoundException.class, () -> userService.update(updateDto, 99999L));
    }

    @Test
    void updateDuplicateEmail() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("sofa@yandex.ru");

        assertThrows(ConflictException.class, () -> userService.update(updateDto, testUser1.getId()));
    }

    @Test
    void updateSameEmailForSameUser() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("ivan@yandex.ru");
        updateDto.setName("updated ivan");

        UserResponseDto result = userService.update(updateDto, testUser1.getId());

        assertThat(result.getEmail()).isEqualTo("ivan@yandex.ru");
        assertThat(result.getName()).isEqualTo("updated ivan");
    }

    @Test
    void updateDuplicateEmailCaseInsensitive() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setEmail("sofa@yandex.ru".toUpperCase());

        assertThrows(ConflictException.class, () -> userService.update(updateDto, testUser1.getId()));
    }

    @Test
    void deleteById() {
        Long userId = testUser1.getId();
        userService.deleteById(userId);

        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(userRepository.findById(testUser2.getId())).isPresent();
    }

    @Test
    void deleteByIdUserNotFound() {
        assertThrows(NotFoundException.class, () -> userService.deleteById(99999L));
    }

    @Test
    void getListEmptyDatabase() {
        userRepository.deleteAll();
        List<UserResponseDto> result = userService.getList();

        assertThat(result).isEmpty();
    }

    @Test
    void createAndGetSeveralUsers() {
        UserCreateDto createDto1 = new UserCreateDto();
        createDto1.setName("User One");
        createDto1.setEmail("user1@test.com");
        UserResponseDto user1 = userService.create(createDto1);

        UserCreateDto createDto2 = new UserCreateDto();
        createDto2.setName("User Two");
        createDto2.setEmail("user2@test.com");
        UserResponseDto user2 = userService.create(createDto2);

        List<UserResponseDto> allUsers = userService.getList();
        assertThat(allUsers).hasSize(4);
        assertThat(allUsers).extracting("email").contains("user1@test.com", "user2@test.com", "ivan@yandex.ru", "sofa@yandex.ru");
    }

    @Test
    void updateDeletedUser() {
        Long userId = testUser1.getId();
        userService.deleteById(userId);

        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setName("not work");

        assertThrows(NotFoundException.class, () -> userService.update(updateDto, userId));
    }
}