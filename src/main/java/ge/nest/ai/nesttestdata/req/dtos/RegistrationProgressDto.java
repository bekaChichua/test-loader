package ge.nest.ai.nesttestdata.req.dtos;


import java.util.List;
import java.util.UUID;


public record RegistrationProgressDto(
        UUID registrationId,
        List<String> pages,
        Integer total
) {
}
