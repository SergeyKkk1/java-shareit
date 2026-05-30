package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private Long id;
    private boolean isConfirmedByOwner;
    private LocalDate startDate;
    private LocalDate endDate;
    private String review;
    private Long itemId;
    private Long borrowerId;
}
