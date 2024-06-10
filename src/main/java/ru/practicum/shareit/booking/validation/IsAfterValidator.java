package ru.practicum.shareit.booking.validation;

import org.apache.commons.beanutils.PropertyUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class IsAfterValidator implements ConstraintValidator<IsAfter, Object> {
    private String min;
    private String max;

    @Override
    public void initialize(IsAfter constraintAnnotation) {
        min = constraintAnnotation.minDate();
        max = constraintAnnotation.minDate();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintContext) {

        try {
            final LocalDate minDate = (LocalDate) PropertyUtils.getProperty(value, min);
            final LocalDate maxDate = (LocalDate) PropertyUtils.getProperty(value, max);
            if ((minDate == null) || (maxDate == null)) {
                return false;
            } else return maxDate.isAfter(minDate);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

//public class IsAfterValidator implements ConstraintValidator<IsAfter, BookingDto> {
/*public class IsAfterValidator implements ConstraintValidator<IsAfter, LocalDate> {
    private LocalDate minDate;
    private LocalDate maxDate;

    @Override
    public void initialize(IsAfter constraintAnnotation) {
        minDate = LocalDate.parse(constraintAnnotation.minDate());
        maxDate = LocalDate.parse(constraintAnnotation.maxDate());
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintContext) {
        if ((minDate == null) || (maxDate == null)) {
            return false;
        } else return maxDate.isAfter(minDate);
    }

    /*@Override
    public boolean isValid(BookingDto booking, ConstraintValidatorContext constraintContext) {
        if (booking == null) {
            return false;
        } else return booking.getEnd().isAfter(booking.getStart());
    }*/
}
