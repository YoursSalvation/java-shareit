package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item availableItem;
    private Item unavailableItem;
    private Booking waitingBooking;
    private Booking approvedBooking;
    private Booking rejectedBooking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("owner");
        owner.setEmail("owner@yandex.ru");
        userRepository.save(owner);

        booker = new User();
        booker.setName("booker");
        booker.setEmail("booker@yandex.ru");
        userRepository.save(booker);

        anotherUser = new User();
        anotherUser.setName("another");
        anotherUser.setEmail("another@yandex.ru");
        userRepository.save(anotherUser);

        availableItem = new Item();
        availableItem.setName("saw");
        availableItem.setDescription("real saw");
        availableItem.setAvailable(true);
        availableItem.setOwner(owner);
        itemRepository.save(availableItem);

        unavailableItem = new Item();
        unavailableItem.setName("knife");
        unavailableItem.setDescription("real knife");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        waitingBooking = new Booking();
        waitingBooking.setStart(OffsetDateTime.now().plusDays(1));
        waitingBooking.setEnd(OffsetDateTime.now().plusDays(2));
        waitingBooking.setBooker(booker);
        waitingBooking.setItem(availableItem);
        waitingBooking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(waitingBooking);

        approvedBooking = new Booking();
        approvedBooking.setStart(OffsetDateTime.now().minusDays(2));
        approvedBooking.setEnd(OffsetDateTime.now().minusDays(1));
        approvedBooking.setBooker(booker);
        approvedBooking.setItem(availableItem);
        approvedBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(approvedBooking);

        rejectedBooking = new Booking();
        rejectedBooking.setStart(OffsetDateTime.now().plusDays(3));
        rejectedBooking.setEnd(OffsetDateTime.now().plusDays(4));
        rejectedBooking.setBooker(booker);
        rejectedBooking.setItem(availableItem);
        rejectedBooking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(rejectedBooking);
    }

    @Test
    void createBookerNotFound() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(availableItem.getId());
        createDto.setStart(OffsetDateTime.now().plusDays(1));
        createDto.setEnd(OffsetDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(99999L, createDto));
    }

    @Test
    void createItemNotFound() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(99999L);
        createDto.setStart(OffsetDateTime.now().plusDays(1));
        createDto.setEnd(OffsetDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), createDto));
    }

    @Test
    void createItemNotAvailable() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(unavailableItem.getId());
        createDto.setStart(OffsetDateTime.now().plusDays(1));
        createDto.setEnd(OffsetDateTime.now().plusDays(2));

        assertThrows(BadRequestException.class, () -> bookingService.create(booker.getId(), createDto));
    }

    @Test
    void createOwnerBookingOwnItem() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(availableItem.getId());
        createDto.setStart(OffsetDateTime.now().plusDays(1));
        createDto.setEnd(OffsetDateTime.now().plusDays(2));

        assertThrows(ForbiddenException.class, () -> bookingService.create(owner.getId(), createDto));
    }

    @Test
    void createStartAfterEnd() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(availableItem.getId());
        createDto.setStart(OffsetDateTime.now().plusDays(2));
        createDto.setEnd(OffsetDateTime.now().plusDays(1));

        assertThrows(BadRequestException.class, () -> bookingService.create(booker.getId(), createDto));
    }

    @Test
    void createStartEqualsEnd() {
        OffsetDateTime dateTime = OffsetDateTime.now().plusDays(1);
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(availableItem.getId());
        createDto.setStart(dateTime);
        createDto.setEnd(dateTime);

        assertThrows(BadRequestException.class, () -> bookingService.create(booker.getId(), createDto));
    }

    @Test
    void getByIdUserNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getById(99999L, waitingBooking.getId()));
    }

    @Test
    void getByIdBookingNotFound() {
        assertThrows(NotFoundException.class, () -> bookingService.getById(booker.getId(), 99999L));
    }

    @Test
    void getByIdForbiddenAccess() {
        assertThrows(ForbiddenException.class, () -> bookingService.getById(anotherUser.getId(), waitingBooking.getId()));
    }

    @Test
    void createAndApproveBooking() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(availableItem.getId());
        createDto.setStart(OffsetDateTime.now().plusDays(15));
        createDto.setEnd(OffsetDateTime.now().plusDays(16));

        BookingResponseDto created = bookingService.create(booker.getId(), createDto);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);

        BookingResponseDto approved = bookingService.approveReject(owner.getId(), created.getId(), true);
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(approved.getId()).isEqualTo(created.getId());
    }

    @Test
    void createAndRejectBooking() {
        BookingCreateDto createDto = new BookingCreateDto();
        createDto.setItemId(availableItem.getId());
        createDto.setStart(OffsetDateTime.now().plusDays(17));
        createDto.setEnd(OffsetDateTime.now().plusDays(18));

        BookingResponseDto created = bookingService.create(booker.getId(), createDto);
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);

        BookingResponseDto rejected = bookingService.approveReject(owner.getId(), created.getId(), false);
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
        assertThat(rejected.getId()).isEqualTo(created.getId());
    }

    @Test
    void severalBookingsOfSameItem() {
        BookingCreateDto createDto1 = new BookingCreateDto();
        createDto1.setItemId(availableItem.getId());
        createDto1.setStart(OffsetDateTime.now().plusDays(20));
        createDto1.setEnd(OffsetDateTime.now().plusDays(21));
        BookingResponseDto booking1 = bookingService.create(booker.getId(), createDto1);

        assertThat(booking1.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking1.getItem().getId()).isEqualTo(availableItem.getId());

        BookingCreateDto createDto2 = new BookingCreateDto();
        createDto2.setItemId(availableItem.getId());
        createDto2.setStart(OffsetDateTime.now().plusDays(22));
        createDto2.setEnd(OffsetDateTime.now().plusDays(23));
        BookingResponseDto booking2 = bookingService.create(anotherUser.getId(), createDto2);

        assertThat(booking2.getBooker().getId()).isEqualTo(anotherUser.getId());
        assertThat(booking2.getItem().getId()).isEqualTo(availableItem.getId());

        Collection<BookingResponseDto> ownerBookings = bookingService.getOwnerBookings(owner.getId(), BookingApiState.ALL);
        assertThat(ownerBookings).hasSize(5);
    }
}