package ru.practicum.shareit.booking.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = IsAfterValidator.class)
public @interface IsAfter {
    String message() default "{constraint.IsAfter}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String minDate();
    String maxDate();
}
