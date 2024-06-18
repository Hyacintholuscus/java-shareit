package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Long>  {

    // Поиск всех бронирований
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Поиск текущих бронирований
    @Query("select b from Booking b " +
            "where b.booker.id = :bookerId " +
            "and b.startDate < :now " +
            "and b.endDate > :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId,
                                        @Param("now") LocalDateTime now,
                                        Sort sort);

    // Поиск будущих бронирований
    List<Booking> findByBookerIdAndStartDateIsAfter(Long bookerId, LocalDateTime startDate, Sort sort);

    // Поиск завершённых бронирований
    List<Booking> findByBookerIdAndEndDateIsBefore(Long bookerId, LocalDateTime endDate, Sort sort);

    // Поиск ожидающих подтверждения и отклонённых бронирований
    List<Booking> findByBookerIdAndStatusIs(Long bookerId, BookingStatus status, Sort sort);

    // Поиск бронирований для комментария
    @Query("select b " +
            "from Booking b " +
            "where b.booker.id = :bookerId " +
            "and b.item.id = :itemId " +
            "and b.endDate < :currentDate " +
            "and b.status in ('APPROVED', 'CANCELED')")
    List<Booking> findBookingToComment(@Param("bookerId") Long bookerId,
                                           @Param("itemId") Long itemId,
                                           @Param("currentDate") LocalDateTime currentDate);

    @Query("select b " +
            "from Booking b " +
            "where b.item.id in ( " +
            "select it.id " +
            "from Item it " +
            "where it.ownerId = :ownerId)")
    // Поиск всех бронирований по владельцу
    List<Booking> findAllByOwner(@Param("ownerId") Long ownerId,
                                 Sort sort);

    // Поиск текущих бронирований по владельцу
    @Query("select b " +
            "from Booking b " +
            "where b.startDate < :now " +
            "and b.endDate > :now " +
            "and b.item.id in ( " +
            "select it.id " +
            "from Item it " +
            "where it.ownerId = :ownerId)")
    List<Booking> findCurrentByOwner(@Param("ownerId") Long ownerId,
                                     @Param("now") LocalDateTime now,
                                     Sort sort);

    // Поиск будущих бронирований по владельцу
    @Query("select b " +
            "from Booking b " +
            "where b.startDate > :now " +
            "and b.status <> 'REJECTED' " +
            "and b.item.id in ( " +
            "select it.id " +
            "from Item it " +
            "where it.ownerId = :ownerId)")
    List<Booking> findFutureByOwner(@Param("ownerId") Long ownerId,
                                    @Param("now") LocalDateTime now,
                                    Sort sort);

    // Поиск завершённых бронирований по владельцу
    @Query("select b " +
            "from Booking b " +
            "where b.endDate < :now " +
            "and b.status not in ('REJECTED', 'WAITING') " +
            "and b.item.id in ( " +
            "select it.id " +
            "from Item it " +
            "where it.ownerId = :ownerId)")
    List<Booking> findPastByOwner(@Param("ownerId") Long ownerId,
                                  @Param("now") LocalDateTime now,
                                  Sort sort);

    // Поиск ожидающих подтверждения и отклонённых бронирований по владельцу
    @Query("select b " +
            "from Booking b " +
            "where b.status = :status " +
            "and b.item.id in ( " +
            "select it.id " +
            "from Item it " +
            "where it.ownerId = :ownerId)")
    List<Booking> findByOwnerByStatus(@Param("ownerId") Long ownerId,
                                      @Param("status") BookingStatus status,
                                      Sort sort);

    // Поиск предыдущего и следующего бронирования для вещи
    @Query("select b " +
            "from Booking b " +
            "where b.item.id = :itemId " +
            "and (b.endDate in (" +
                "select max(b.endDate)" +
                "from Booking b " +
                "where b.item.id = :itemId " +
                "and b.status in ('APPROVED', 'CANCELED') " +
                "and (b.endDate < :currentTime " +
                "or (b.startDate < :currentTime " +
                "and b.endDate > :currentTime)))" +
            "or b.startDate in (" +
                "select min(b.startDate) " +
                "from Booking b " +
                "where b.startDate > :currentTime " +
                "and b.item.id = :itemId " +
                "and b.status in ('APPROVED', 'WAITING'))) ")
    List<Booking> findLastAndNextForItem(@Param("itemId") Long itemId,
                                   @Param("currentTime") LocalDateTime currentTime);
}
