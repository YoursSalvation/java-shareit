package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional(readOnly = false)
    public ItemRequestResponseDto create(Long userId, ItemRequestCreateDto itemRequestCreateDto) {
        User requestor = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        ItemRequest newItemRequest = new ItemRequest();
        newItemRequest.setDescription(itemRequestCreateDto.getDescription());
        newItemRequest.setRequestor(requestor);
        newItemRequest.setCreated(OffsetDateTime.now());

        ItemRequest createdItemRequest = itemRequestRepository.save(newItemRequest);
        return ItemRequestResponseDto.from(createdItemRequest);
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long requestId) {
        ItemRequest foundItemRequest = itemRequestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Item Request " + requestId + " not found")
        );
        return ItemRequestResponseDto.from(foundItemRequest);
    }

    @Override
    public Collection<ItemRequestResponseDto> getMyRequests(Long userId) {
        User requestor = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User " + userId + " not found")
        );
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> myRequests = itemRequestRepository.findByRequestorId(userId, sort);
        return myRequests.stream()
                .filter(Objects::nonNull)
                .map(ItemRequestResponseDto::from)
                .toList();
    }

    @Override
    public Collection<ItemRequestResponseSimpleViewDto> getOthersRequests(Long userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> allRequests = itemRequestRepository.findByRequestorIdNot(userId, sort);
        return allRequests.stream()
                .filter(Objects::nonNull)
                .map(ItemRequestResponseSimpleViewDto::from)
                .toList();
    }

}