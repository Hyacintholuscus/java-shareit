package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Long>  {
    // Поиск текущих бронирований
    @Query("select b from Booking b" +
            "where b.booker.id = :bookerId" +
            "and b.start.date > :now" +
            "and b.end.date < :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                        @Param("now") LocalDateTime now,
                                        Sort sort);

    // Поиск будущих бронирований
    List<Booking> findByBookerIdAndStartIsBefore(Long bookerId, LocalDateTime start, Sort sort);

    // Поиск завершённых, ожидающих подтверждения и отклонённых бронирований
    List<Booking> findByBookerIdAndStatusIs(Long bookerId, BookingStatus status, Sort sort);

    // Поиск текущих бронирований по владельцу
    @Query("select b" +
            "from Booking b" +
            "where b.start.date > :now" +
            "and b.end.date < :now" +
            "And b.item.id in (" +
            "select it.id" +
            "from Item it" +
            "where it.owner.id = :ownerId)")
    List<Booking> findCurrentByOwner(@Param("ownerId") Long ownerId,
                                     @Param("now") LocalDateTime now,
                                     Sort sort);

    // Поиск будущих бронирований по владельцу
    @Query("select b" +
            "from Booking b" +
            "where b.start.date > :now" +
            "and b.status <> REJECTED" +
            "and b.item.id in (" +
            "select it.id" +
            "from Item it" +
            "where it.owner.id = :ownerId)")
    List<Booking> findFutureByOwner(@Param("ownerId") Long ownerId,
                                    @Param("now") LocalDateTime now,
                                    Sort sort);

    // Поиск завершённых, ожидающих подтверждения и отклонённых бронирований по владельцу
    @Query("select b" +
            "from Booking b" +
            "where b.status = :status" +
            "and b.item.id in (" +
            "select it.id" +
            "from Item it" +
            "where it.owner.id = :ownerId)")
    List<Booking> findByOwnerByStatus(@Param("ownerId") Long ownerId,
                                      @Param("status") BookingStatus status,
                                      Sort sort);
}
