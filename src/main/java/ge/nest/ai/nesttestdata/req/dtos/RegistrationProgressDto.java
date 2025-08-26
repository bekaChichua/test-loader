package ge.nest.registration.usecase.web.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record RegistrationProgressDto(
        @Schema(description = "Id of the property being registered", requiredMode = REQUIRED)
        UUID registrationId,
        @Schema(description = "multiform pages", requiredMode = REQUIRED)
        List<String> pages,
        @Schema(description = "total pages", requiredMode = REQUIRED)
        Integer total
) {
}
