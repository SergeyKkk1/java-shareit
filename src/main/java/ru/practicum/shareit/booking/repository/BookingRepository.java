package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b FROM Booking b
            WHERE b.borrower.id = :userId
              AND (    :state = 'ALL'
                    OR (:state = 'CURRENT' AND b.startDate <= :now AND b.endDate >= :now)
                    OR (:state = 'PAST'    AND b.endDate   <  :now)
                    OR (:state = 'FUTURE'  AND b.startDate >  :now)
                    OR (:state = 'WAITING'  AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING)
                    OR (:state = 'REJECTED' AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED))
            ORDER BY b.startDate DESC
            """)
    List<Booking> findByBookerAndState(@Param("userId") Long userId,
                                       @Param("state") String state,
                                       @Param("now") LocalDateTime now);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.item.owner.id = :userId
              AND (    :state = 'ALL'
                    OR (:state = 'CURRENT' AND b.startDate <= :now AND b.endDate >= :now)
                    OR (:state = 'PAST'    AND b.endDate   <  :now)
                    OR (:state = 'FUTURE'  AND b.startDate >  :now)
                    OR (:state = 'WAITING'  AND b.status = ru.practicum.shareit.booking.BookingStatus.WAITING)
                    OR (:state = 'REJECTED' AND b.status = ru.practicum.shareit.booking.BookingStatus.REJECTED))
            ORDER BY b.startDate DESC
            """)
    List<Booking> findByOwnerAndState(@Param("userId") Long userId,
                                      @Param("state") String state,
                                      @Param("now") LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartDateBeforeOrderByStartDateDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartDateAfterOrderByStartDateAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByBorrowerIdAndItemIdAndStatusAndEndDateBefore(
            Long borrowerId, Long itemId, BookingStatus status, LocalDateTime now);
}
