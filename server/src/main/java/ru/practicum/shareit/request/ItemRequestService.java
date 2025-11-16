package ru.practicum.shareit.request;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestResponseDto create(Long userId, ItemRequestCreateDto itemRequestCreateDto);

    ItemRequestResponseDto getRequestById(Long requestId);

    Collection<ItemRequestResponseDto> getMyRequests(Long userId);

    Collection<ItemRequestResponseSimpleViewDto> getOthersRequests(Long userId);

}