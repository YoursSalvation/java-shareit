package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    @Query("""
            select exists (
              select 1 from User as u
              where lower(u.email) = :email
              and u.id != :id
            )
            """)
    boolean existsByEmailButNotTheSame(@Param("email") String email, @Param("id") Long id);

}