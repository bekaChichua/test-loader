package ge.nest.registration.usecase.web.dtos;

import ge.nest.commons.valueobjects.property.PropertyType;
import jakarta.validation.constraints.NotNull;

public record TypeDto(
        @NotNull
        PropertyType propertyType
) {
}