package ru.practicum.shareitgate.booking.dto;

import java.util.Optional;

public enum BookingState {
    ALL,
    CURRENT,
    FUTURE,
    WAITING,
    REJECTED,
    PAST;

    public static Optional<BookingState> getState(String stringState) {
        for (BookingState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
