package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemRepository;
import ru.practicum.shareit.user.dto.UserRepository;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingServiceImplement implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking getById(Long userId, Long bookingId) {
        userRepository.checkExistenceById(userId);

        bookingRepository.checkExistenceById(bookingId);
        Booking booking = bookingRepository.getById(bookingId);
        if (Objects.equals(booking.getBookerId(), userId)) return booking;

        itemRepository.checkExistenceById(booking.getItemId());
        Item item = itemRepository.getById(booking.getItemId());
        if (Objects.equals(item.getOwnerId(), userId)) return booking;

        throw new ForbiddenException("User " + userId + " has no rights to see booking # " + booking);
    }

    @Override
    public Booking create(Long userId, Booking booking) {
        userRepository.checkExistenceById(userId);
        booking.setBookerId(userId);
        booking.setStatus(BookingStatus.WAITING);
        return bookingRepository.create(booking);
    }

}