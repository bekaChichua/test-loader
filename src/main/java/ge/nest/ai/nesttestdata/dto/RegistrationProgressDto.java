package ge.nest.ai.nesttestdata.dto;

import java.util.List;
import java.util.UUID;

public record RegistrationProgressDto(
        UUID registrationId,
        List<String> pages,
        Integer total
) {
}
