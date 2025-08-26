package ge.nest.ai.nesttestdata.req.dtos;

import ge.nest.commons.contract.dtos.AddressDto;
import ge.nest.commons.contract.dtos.MeasurementsDto;
import ge.nest.commons.contract.dtos.MultiLangTextDto;
import ge.nest.commons.contract.dtos.PhotosWithUrlDto;
import ge.nest.commons.valueobjects.property.PropertyType;
import ge.nest.commons.valueobjects.spaces.SpaceType;
import lombok.Builder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record PropertyDraftDto(
        UUID propertyId,
        PropertyType type,
        String title,
        MultiLangTextDto description,
        MeasurementsDto measurements,
        AddressDto address,
        Map<SpaceType, List<Double>> spaces,
        Collection<PhotosWithUrlDto> photos,
        Collection<String> amenities,
        List<String> pages,
        int total
) {
}
