package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking " + bookingId + " not found"));

        boolean userIsBooker = Objects.equals(user, existingBooking.getBooker());
        boolean userIsItemOwner = Objects.equals(user, existingBooking.getItem().getOwner());
        if (!userIsBooker && !userIsItemOwner)
            throw new ForbiddenException("User # " + userId + " has no rights to see booking # " + bookingId);

        return BookingResponseDto.from(existingBooking);
    }

    @Override
    @Transactional(readOnly = false)
    public BookingResponseDto create(Long userId, BookingCreateDto dto) {
        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new BadRequestException("Start should be before End");
        }

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item " + dto.getItemId() + " not found"));

        if (!item.getAvailable()) {
            throw new BadRequestException("Item " + dto.getItemId() + " is not available for booking");
        }
        if (Objects.equals(booker, item.getOwner())) {
            throw new ForbiddenException("User can not book his own item");
        }

        Booking newBooking = new Booking();
        newBooking.setStart(dto.getStart());
        newBooking.setEnd(dto.getEnd());
        newBooking.setBooker(booker);
        newBooking.setItem(item);
        newBooking.setStatus(BookingStatus.WAITING);

        Booking createdBooking = bookingRepository.save(newBooking);
        return BookingResponseDto.from(createdBooking);
    }

    @Override
    @Transactional(readOnly = false)
    public BookingResponseDto approveReject(Long userId, Long bookingId, boolean isApproved) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking " + bookingId + " not found"));

        boolean userIsItemOwner = Objects.equals(userId, existingBooking.getItem().getOwner().getId());
        if (!userIsItemOwner) throw new ForbiddenException("User is not owner of Item");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));

        boolean statusIsWaiting = BookingStatus.WAITING.equals(existingBooking.getStatus());
        if (!statusIsWaiting) throw new ForbiddenException("Booking status should be WAITING");

        if (isApproved) {
            existingBooking.setStatus(BookingStatus.APPROVED);
        } else {
            existingBooking.setStatus(BookingStatus.REJECTED);
        }

        return BookingResponseDto.from(existingBooking);
    }

    @Override
    public Collection<BookingResponseDto> getBookerBookings(Long userId, BookingApiState state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));

        OffsetDateTime nowTime = OffsetDateTime.now();
        Sort sort = Sort.by("start").descending();

        List<Booking> bookings = switch (state) {
            case BookingApiState.CURRENT ->
                    bookingRepository.findByBookerIdAndStatusAndStartBeforeAndEndAfter(userId,
                            BookingStatus.APPROVED, nowTime, nowTime, sort);
            case BookingApiState.PAST ->
                    bookingRepository.findByBookerIdAndStatusAndEndBefore(userId, BookingStatus.APPROVED, nowTime,
                            sort);
            case BookingApiState.FUTURE ->
                    bookingRepository.findByBookerIdAndStatusAndStartAfter(userId, BookingStatus.APPROVED, nowTime,
                            sort);
            case BookingApiState.WAITING ->
                    bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case BookingApiState.REJECTED ->
                    bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
            default -> bookingRepository.findByBookerId(userId, sort);
        };

        return bookings.stream().filter(Objects::nonNull).map(BookingResponseDto::from).toList();
    }

    @Override
    public Collection<BookingResponseDto> getOwnerBookings(Long userId, BookingApiState state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));

        OffsetDateTime nowTime = OffsetDateTime.now();
        Sort sort = Sort.by("start").descending();

        List<Booking> bookings = switch (state) {
            case BookingApiState.CURRENT ->
                    bookingRepository.findByItemOwnerIdAndStatusAndStartBeforeAndEndAfter(userId, BookingStatus.APPROVED, nowTime, nowTime, sort);
            case BookingApiState.PAST ->
                    bookingRepository.findByItemOwnerIdAndStatusAndEndBefore(userId, BookingStatus.APPROVED, nowTime, sort);
            case BookingApiState.FUTURE ->
                    bookingRepository.findByItemOwnerIdAndStatusAndStartAfter(userId, BookingStatus.APPROVED, nowTime, sort);
            case BookingApiState.WAITING ->
                    bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case BookingApiState.REJECTED ->
                    bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
            default -> bookingRepository.findByItemOwnerId(userId, sort);
        };

        return bookings.stream().filter(Objects::nonNull).map(BookingResponseDto::from).toList();
    }

}