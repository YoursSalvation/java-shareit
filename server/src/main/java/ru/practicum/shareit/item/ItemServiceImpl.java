package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.IdAndTimeJpaProjection;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional(readOnly = false)
    public ItemResponseDto create(Long userId, ItemCreateDto itemCreateDto) {
        User owner = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );

        Item newItem = new Item();
        newItem.setName(itemCreateDto.getName());
        newItem.setDescription(itemCreateDto.getDescription());
        newItem.setAvailable(itemCreateDto.getAvailable());
        newItem.setOwner(owner);
        if (itemCreateDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemCreateDto.getRequestId()).orElseThrow(
                    () -> new NotFoundException("Item Request " + itemCreateDto.getRequestId() + " not found")
            );
            newItem.setItemRequest(itemRequest);
        }

        Item createdItem = itemRepository.save(newItem);
        return ItemResponseDto.from(createdItem);
    }

    @Override
    @Transactional(readOnly = false)
    public ItemResponseDto update(Long userId, Long itemId, ItemUpdateDto itemUpdateDto) {
        User owner = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        Item existingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Item " + itemId + " not found")
        );
        if (!Objects.equals(existingItem.getOwner(), owner)) throw new ForbiddenException("Owner check failed");

        if (itemUpdateDto.getName() != null) {
            existingItem.setName(itemUpdateDto.getName());
        }
        if (itemUpdateDto.getDescription() != null) {
            existingItem.setDescription(itemUpdateDto.getDescription());
        }
        if (itemUpdateDto.getAvailable() != null) {
            existingItem.setAvailable(itemUpdateDto.getAvailable());
        }
        return ItemResponseDto.from(existingItem);
    }

    @Override
    public ItemResponseExtendedViewDto getById(Long userId, Long itemId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        Item foundItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Item " + itemId + " not found")
        );

        ItemResponseExtendedViewDto dto = ItemResponseExtendedViewDto.from(foundItem);

        if (Objects.equals(user, foundItem.getOwner())) {
            OffsetDateTime nowTime = OffsetDateTime.now();
            OffsetDateTime lastBooking = bookingRepository.getLastBookingDate(itemId, nowTime);
            OffsetDateTime nextBooking = bookingRepository.getNextBookingDate(itemId, nowTime);
            dto.setLastBooking(lastBooking);
            dto.setNextBooking(nextBooking);
        }

        return dto;
    }

    @Override
    public Collection<ItemResponseExtendedViewDto> findByOwnerId(Long userId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("User " + userId + " not found");
        OffsetDateTime nowTime = OffsetDateTime.now();

        List<ItemResponseExtendedViewDto> dtos = itemRepository.findByOwnerId(userId).stream()
                .filter(Objects::nonNull)
                .map(ItemResponseExtendedViewDto::from)
                .toList();
        Set<Long> itemIdSet = dtos.stream()
                .map(ItemResponseExtendedViewDto::getId)
                .collect(Collectors.toSet());

        Map<Long, OffsetDateTime> lastBookingDates = bookingRepository.getListOfLastBookingDates(itemIdSet, nowTime)
                .stream()
                .collect(Collectors.toMap(
                        IdAndTimeJpaProjection::getId,
                        IdAndTimeJpaProjection::getTime
                ));
        Map<Long, OffsetDateTime> nextBookingDates = bookingRepository.getListOfNextBookingDates1(itemIdSet, nowTime)
                .stream()
                .collect(Collectors.toMap(
                        IdAndTimeJpaProjection::getId,
                        IdAndTimeJpaProjection::getTime
                ));

        for (ItemResponseExtendedViewDto dto : dtos) {
            dto.setLastBooking(lastBookingDates.get(dto.getId()));
            dto.setNextBooking(nextBookingDates.get(dto.getId()));
        }
        return dtos;
    }

    @Override
    public Collection<ItemResponseDto> findByText(String text) {
        return itemRepository.findByText(text.toLowerCase()).stream()
                .filter(Objects::nonNull)
                .map(ItemResponseDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteById(Long userId, Long itemId) {
        User owner = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        Item existingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Item " + itemId + " not found")
        );
        if (!Objects.equals(existingItem.getOwner(), owner)) throw new ForbiddenException("Owner check failed");
        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional(readOnly = false)
    public CommentResponseDto addComment(Long userId, Long itemId, CommentCreateDto commentCreateDto) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new NotFoundException("Item " + itemId + " not found")
        );
        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(userId, itemId, BookingStatus.APPROVED,
                OffsetDateTime.now())) {
            throw new BadRequestException("User " + userId + " doesn't have past bookings of item " + itemId);
        }
        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setItem(item);
        comment.setText(commentCreateDto.getText());
        comment.setCreated(OffsetDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentResponseDto.from(savedComment);
    }

}