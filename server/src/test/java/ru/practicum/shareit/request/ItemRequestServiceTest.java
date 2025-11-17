package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@AutoConfigureTestDatabase
class ItemRequestServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User secondUser;
    private User thirdUser;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest2;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName("requestor");
        requestor.setEmail("requestor@yandex.ru");
        userRepository.save(requestor);

        secondUser = new User();
        secondUser.setName("second");
        secondUser.setEmail("second@yandex.ru");
        userRepository.save(secondUser);

        thirdUser = new User();
        thirdUser.setName("third");
        thirdUser.setEmail("third@yandex.ru");
        userRepository.save(thirdUser);

        itemRequest1 = new ItemRequest();
        itemRequest1.setDescription("need saw");
        itemRequest1.setRequestor(requestor);
        itemRequest1.setCreated(OffsetDateTime.now().minusDays(2));
        itemRequestRepository.save(itemRequest1);

        itemRequest2 = new ItemRequest();
        itemRequest2.setDescription("need knife");
        itemRequest2.setRequestor(secondUser);
        itemRequest2.setCreated(OffsetDateTime.now().minusDays(1));
        itemRequestRepository.save(itemRequest2);
    }

    @Test
    void create() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("need fork");

        ItemRequestResponseDto result = itemRequestService.create(requestor.getId(), createDto);

        assertThat(result).isNotNull();
        assertThat(result.getDescription()).isEqualTo("need fork");
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void createUserNotFound() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("need something");

        assertThrows(NotFoundException.class, () -> itemRequestService.create(99999L, createDto));
    }

    @Test
    void getRequestById() {
        ItemRequestResponseDto result = itemRequestService.getRequestById(itemRequest1.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemRequest1.getId());
        assertThat(result.getDescription()).isEqualTo("need saw");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getRequestByIdWithItems() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real saw");
        item.setAvailable(true);
        item.setOwner(secondUser);
        item.setItemRequest(itemRequest1);
        itemRequest1.getItems().add(item);
        itemRepository.save(item);

        ItemRequestResponseDto result = itemRequestService.getRequestById(itemRequest1.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemRequest1.getId());
        assertThat(result.getDescription()).isEqualTo("need saw");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("saw");
    }

    @Test
    void getRequestByIdNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(99999L));
    }

    @Test
    void getMyRequests() {
        ItemRequest additionalRequest = new ItemRequest();
        additionalRequest.setDescription("need cup");
        additionalRequest.setRequestor(requestor);
        additionalRequest.setCreated(OffsetDateTime.now().minusHours(1));
        itemRequestRepository.save(additionalRequest);

        Collection<ItemRequestResponseDto> result = itemRequestService.getMyRequests(requestor.getId());

        assertThat(result).hasSize(2);
        List<ItemRequestResponseDto> resultList = result.stream().toList();
        assertThat(resultList.get(0).getDescription()).isEqualTo("need cup");
        assertThat(resultList.get(1).getDescription()).isEqualTo("need saw");
    }

    @Test
    void getMyRequestsEmpty() {
        Collection<ItemRequestResponseDto> result = itemRequestService.getMyRequests(thirdUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getMyRequestsUserNotFound() {
        assertThrows(NotFoundException.class, () -> itemRequestService.getMyRequests(99999L));
    }

    @Test
    void getMyRequestsWithItems() {
        Item item = new Item();
        item.setName("saw");
        item.setDescription("real saw");
        item.setAvailable(true);
        item.setOwner(secondUser);
        item.setItemRequest(itemRequest1);
        itemRequest1.setItems(new ArrayList<>());
        itemRequest1.getItems().add(item);
        itemRepository.save(item);

        Collection<ItemRequestResponseDto> result = itemRequestService.getMyRequests(requestor.getId());

        assertThat(result).hasSize(1);
        ItemRequestResponseDto request = result.iterator().next();
        assertThat(request.getItems()).hasSize(1);
        assertThat(request.getItems().getFirst().getName()).isEqualTo("saw");
    }

    @Test
    void getOthersRequests() {
        ItemRequest thirdRequest = new ItemRequest();
        thirdRequest.setDescription("need saw");
        thirdRequest.setRequestor(thirdUser);
        thirdRequest.setCreated(OffsetDateTime.now().minusHours(1));
        itemRequestRepository.save(thirdRequest);

        Collection<ItemRequestResponseSimpleViewDto> result = itemRequestService.getOthersRequests(requestor.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting("description").containsExactlyInAnyOrder("need knife",
                "need saw");

        List<ItemRequestResponseSimpleViewDto> resultList = result.stream().toList();
        assertThat(resultList.get(0).getDescription()).isEqualTo("need saw");
        assertThat(resultList.get(1).getDescription()).isEqualTo("need knife");
    }

    @Test
    void getOthersRequestsEmpty() {
        itemRequestRepository.deleteById(itemRequest2.getId());
        Collection<ItemRequestResponseSimpleViewDto> result = itemRequestService.getOthersRequests(requestor.getId());
        assertThat(result).isEmpty();
    }

    @Test
    void getOthersRequestsExcludesOwnRequests() {
        Collection<ItemRequestResponseSimpleViewDto> result = itemRequestService.getOthersRequests(requestor.getId());

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getDescription()).isEqualTo("need saw");
        assertThat(result).extracting("description").doesNotContain("need saw");
    }

    @Test
    void createSeveralRequestsAndCheckSorting() throws InterruptedException {
        ItemRequestCreateDto createDto1 = new ItemRequestCreateDto();
        createDto1.setDescription("first request");
        ItemRequestResponseDto created1 = itemRequestService.create(requestor.getId(), createDto1);

        Thread.sleep(10);

        ItemRequestCreateDto createDto2 = new ItemRequestCreateDto();
        createDto2.setDescription("second request");
        ItemRequestResponseDto created2 = itemRequestService.create(requestor.getId(), createDto2);

        List<ItemRequestResponseDto> sortedRequests = itemRequestService.getMyRequests(requestor.getId()).stream().toList();

        assertThat(sortedRequests).hasSize(3);
        assertThat(sortedRequests.get(0).getDescription()).isEqualTo("second request");
        assertThat(sortedRequests.get(1).getDescription()).isEqualTo("first request");
        assertThat(sortedRequests.get(2).getDescription()).isEqualTo("need drill");
    }

    @Test
    void requestWithSeveralItems() {
        Item item1 = new Item();
        item1.setName("saw 1");
        item1.setDescription("first saw");
        item1.setAvailable(true);
        item1.setOwner(secondUser);
        item1.setItemRequest(itemRequest1);
        itemRequest1.getItems().add(item1);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("saw 2");
        item2.setDescription("second saw");
        item2.setAvailable(false);
        item2.setOwner(thirdUser);
        item2.setItemRequest(itemRequest1);
        itemRequest1.getItems().add(item2);
        itemRepository.save(item2);

        ItemRequestResponseDto result = itemRequestService.getRequestById(itemRequest1.getId());

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems()).extracting("name").containsExactlyInAnyOrder("saw 1", "saw 2");
    }

    @Test
    void createAndGetImmediately() {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("immediate test");

        ItemRequestResponseDto created = itemRequestService.create(requestor.getId(), createDto);
        ItemRequestResponseDto retrieved = itemRequestService.getRequestById(created.getId());

        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(created.getId());
        assertThat(retrieved.getDescription()).isEqualTo("immediate test");
        assertThat(retrieved.getCreated()).isEqualTo(created.getCreated());
    }
}