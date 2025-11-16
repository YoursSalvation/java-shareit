package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.OffsetDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User requestor;
    private User booker;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@example.com");
        userRepository.save(owner);

        booker = new User();
        booker.setName("booker");
        booker.setEmail("booker@example.com");
        userRepository.save(booker);

        requestor = new User();
        requestor.setName("requestor");
        requestor.setEmail("requestor@example.com");
        userRepository.save(requestor);

        itemRequest = new ItemRequest();
        itemRequest.setDescription("wanna saw");
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(OffsetDateTime.now());
        itemRequestRepository.save(itemRequest);
    }

    @Test
    void create() {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("saw");
        createDto.setDescription("real");
        createDto.setAvailable(true);

        ItemResponseDto result = itemService.create(owner.getId(), createDto);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("saw");
        assertThat(result.getDescription()).isEqualTo("real");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getId()).isNotNull();

        ItemCreateDto createDtoWithRequestId = new ItemCreateDto();
        createDtoWithRequestId.setName("saw");
        createDtoWithRequestId.setDescription("real");
        createDtoWithRequestId.setAvailable(true);
        createDtoWithRequestId.setRequestId(itemRequest.getId());

        ItemResponseDto result2 = itemService.create(owner.getId(), createDtoWithRequestId);
        assertThat(result2).isNotNull();
        assertThat(result2.getRequestId()).isEqualTo(itemRequest.getId());
    }

    @Test
    void createUserNotFound() {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("saw");
        createDto.setDescription("real");
        createDto.setAvailable(true);

        assertThrows(NotFoundException.class, () -> itemService.create(99999L, createDto));
    }

    @Test
    void createRequestNotFound() {
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setName("saw");
        createDto.setDescription("real");
        createDto.setAvailable(true);
        createDto.setRequestId(99999L);

        assertThrows(NotFoundException.class, () -> itemService.create(owner.getId(), createDto));
    }

    @Test
    void update() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("updated saw");
        updateDto.setDescription("new saw");
        updateDto.setAvailable(false);

        ItemResponseDto result = itemService.update(owner.getId(), item.getId(), updateDto);
        assertThat(result.getName()).isEqualTo("updated saw");
        assertThat(result.getDescription()).isEqualTo("new saw");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void updateWrongOwner() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setName("updated saw");

        assertThrows(ForbiddenException.class, () -> itemService.update(booker.getId(), item.getId(), updateDto));
    }

    @Test
    void getById() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking.setStart(OffsetDateTime.now().minusDays(2));
        pastBooking.setEnd(OffsetDateTime.now().minusDays(1));
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking();
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.APPROVED);
        futureBooking.setStart(OffsetDateTime.now().plusDays(1));
        futureBooking.setEnd(OffsetDateTime.now().plusDays(2));
        bookingRepository.save(futureBooking);

        ItemResponseExtendedViewDto result = itemService.getById(owner.getId(), item.getId());
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();

        ItemResponseExtendedViewDto result2 = itemService.getById(booker.getId(), item.getId());
        assertThat(result2).isNotNull();
        assertThat(result2.getId()).isEqualTo(item.getId());
        assertThat(result2.getLastBooking()).isNull();
        assertThat(result2.getNextBooking()).isNull();
    }

    @Test
    void findByOwnerId() {
        Item item1 = new Item();
        item1.setName("saw 1");
        item1.setDescription("real 1");
        item1.setAvailable(true);
        item1.setOwner(owner);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("saw 2");
        item2.setDescription("real 2");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        Collection<ItemResponseExtendedViewDto> result = itemService.findByOwnerId(owner.getId());
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("saw 1", "saw 2");
    }

    @Test
    void findByText() {
        Item item1 = new Item();
        item1.setName("saw");
        item1.setDescription("real 1");
        item1.setAvailable(true);
        item1.setOwner(owner);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("knife");
        item2.setDescription("real 2");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        Collection<ItemResponseDto> result = itemService.findByText("saw");
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getDescription()).isEqualTo("real 1");
    }

    @Test
    void delete() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        Long itemId = item.getId();

        itemService.deleteById(owner.getId(), itemId);
        assertThat(itemRepository.findById(itemId)).isEmpty();
    }

    @Test
    void deleteWrongOwner() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        assertThrows(ForbiddenException.class, () -> itemService.deleteById(booker.getId(), item.getId()));
    }

    @Test
    void addComment() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        pastBooking.setStart(OffsetDateTime.now().minusDays(2));
        pastBooking.setEnd(OffsetDateTime.now().minusDays(1));
        bookingRepository.save(pastBooking);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("very real");

        CommentResponseDto result = itemService.addComment(booker.getId(), item.getId(), commentDto);
        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("very real");
        assertThat(result.getAuthorName()).isEqualTo(booker.getName());
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void addCommentWithoutPastBooking() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);

        CommentCreateDto commentDto = new CommentCreateDto();
        commentDto.setText("very real");

        assertThrows(BadRequestException.class, () -> itemService.addComment(booker.getId(), item.getId(), commentDto));
    }

}