package ge.nest.registration.usecase.web.dtos;

import ge.nest.commons.valueobjects.spaces.SpaceType;
import ge.nest.registration.draft.Space;
import ge.nest.registration.draft.Spaces;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SpacesDto(
        @NotNull(message = "Space details are missing.")
        @Size(min = 1, max = 10, message = "The values must contain between 1 and 15 elements.")
        List<SpaceRequest> spaces
) {
    public record SpaceRequest(
            SpaceType spaceType,
            Double area
    ) {
    }

    public List<Space> toSpaces() {
        return spaces.stream().map(s -> new Space(s.spaceType(), s.area())).toList();
    }

    public static List<Space> from(Spaces spaces) {
        return spaces.values().stream().map(s -> new Space(s.type(), s.area())).toList();
    }
}
