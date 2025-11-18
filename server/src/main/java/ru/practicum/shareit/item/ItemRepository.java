package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long id);

    @Query("""
            select i from Item as i
            where i.available = true
              and
              ( lower(i.name) like %:text%
                or
                lower(i.description) like %:text% )
            """)
    List<Item> findByText(@Param("text") String text);

}