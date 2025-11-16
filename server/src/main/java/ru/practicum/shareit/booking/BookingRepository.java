package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(
            Long userId,
            Sort sort
    );

    List<Booking> findByBookerIdAndStatus(
            Long userId,
            BookingStatus status,
            Sort sort
    );

    List<Booking> findByBookerIdAndStatusAndEndBefore(
            Long userId,
            BookingStatus status,
            OffsetDateTime time,
            Sort sort
    );

    List<Booking> findByBookerIdAndStatusAndStartAfter(
            Long userId,
            BookingStatus status,
            OffsetDateTime time,
            Sort sort
    );

    List<Booking> findByBookerIdAndStatusAndStartBeforeAndEndAfter(
            Long userId,
            BookingStatus status,
            OffsetDateTime time1,
            OffsetDateTime time2,
            Sort sort
    );

    List<Booking> findByItemOwnerId(
            Long userId,
            Sort sort
    );

    List<Booking> findByItemOwnerIdAndStatus(
            Long userId,
            BookingStatus status,
            Sort sort
    );

    List<Booking> findByItemOwnerIdAndStatusAndEndBefore(
            Long userId,
            BookingStatus status,
            OffsetDateTime time,
            Sort sort
    );

    List<Booking> findByItemOwnerIdAndStatusAndStartAfter(
            Long userId,
            BookingStatus status,
            OffsetDateTime time,
            Sort sort
    );

    List<Booking> findByItemOwnerIdAndStatusAndStartBeforeAndEndAfter(
            Long userId,
            BookingStatus status,
            OffsetDateTime time1,
            OffsetDateTime time2,
            Sort sort
    );

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long userId,
            Long itemId,
            BookingStatus status,
            OffsetDateTime time
    );

    @Query(value = """
            select max(b.end) from Booking as b
            where b.item.id = :itemId
            and b.end < :time
            """)
    OffsetDateTime getLastBookingDate(
            @Param("itemId") Long itemId,
            @Param("time") OffsetDateTime time
    );

    @Query(value = """
            select min(b.start) from Booking as b
            where b.item.id = :itemId
            and b.start > :time
            """)
    OffsetDateTime getNextBookingDate(
            @Param("itemId") Long itemId,
            @Param("time") OffsetDateTime time
    );

    @Query(value = """
            select b.item.id as id, max(b.end) as time from Booking as b
            where b.item.id in :itemIdSet
            and b.end < :time
            group by b.item.id
            """)
    List<IdAndTimeJpaProjection> getListOfLastBookingDates(
            @Param("itemIdSet") Set<Long> itemIdSet,
            @Param("time") OffsetDateTime time
    );

    @Query(value = """
            select b.item.id as id, min(b.start) as time from Booking as b
            where b.item.id in :itemIdSet
            and b.start > :time
            group by b.item.id
            """)
    List<IdAndTimeJpaProjection> getListOfNextBookingDates1(
            @Param("itemIdSet") Set<Long> itemIdSet,
            @Param("time") OffsetDateTime time
    );

}