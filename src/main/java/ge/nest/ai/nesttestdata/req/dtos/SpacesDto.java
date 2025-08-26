package ge.nest.ai.nesttestdata.req.dtos;

import ge.nest.commons.valueobjects.spaces.SpaceType;

import java.util.List;

public record SpacesDto(
        List<SpaceRequest> spaces
) {
    public record SpaceRequest(
            SpaceType spaceType,
            Double area
    ) {
    }

}
