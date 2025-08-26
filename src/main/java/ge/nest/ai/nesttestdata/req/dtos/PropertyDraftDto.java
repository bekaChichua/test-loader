package ge.nest.registration.usecase.web.dtos;

import ge.nest.commons.contract.dtos.AddressDto;
import ge.nest.commons.contract.dtos.MeasurementsDto;
import ge.nest.commons.contract.dtos.MultiLangTextDto;
import ge.nest.commons.contract.dtos.PhotosWithUrlDto;
import ge.nest.commons.valueobjects.property.PropertyType;
import ge.nest.commons.valueobjects.spaces.SpaceType;
import ge.nest.registration.draft.Amenities;
import ge.nest.registration.draft.PropertyDraft;
import ge.nest.registration.draft.Space;
import ge.nest.registration.flow.Progress;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.*;
import java.util.stream.Collectors;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder
public record PropertyDraftDto(
        @Schema(description = "Id of the property", requiredMode = REQUIRED)
        UUID propertyId,
        @Schema(description = "Type of the property being registered", requiredMode = REQUIRED)
        PropertyType type,
        @Schema(description = "Title of the property, for property owners to identify property", requiredMode = REQUIRED)
        String title,
        @Schema(description = "Descriptions of the property in 3 languages", requiredMode = REQUIRED)
        MultiLangTextDto description,
        @Schema(description = "Measurements of the property, can be null", requiredMode = NOT_REQUIRED)
        MeasurementsDto measurements,
        @Schema(description = "Property address, can be null", requiredMode = NOT_REQUIRED)
        AddressDto address,
        @Schema(description = "Room details, can be empty but never null", requiredMode = REQUIRED)
        Map<SpaceType, List<Double>> spaces,
        @Schema(description = "Image reference urls and keys to the image storage, can be empty but never null", requiredMode = REQUIRED)
        Collection<PhotosWithUrlDto> photos,
        @Schema(description = "Amenities of the property, can be empty but never null", requiredMode = REQUIRED)
        Collection<String> amenities,
        @Schema(description = "Current state of the registration, is never null", requiredMode = REQUIRED)
        List<String> pages,
        @Schema(description = "Total pages on this flow", requiredMode = REQUIRED)
        int total
) {

    public static PropertyDraftDto of(PropertyDraft draft, Progress progress, List<PhotosWithUrlDto> photos) {
        var id = draft.getId();
        var type = draft.getType();
        var address = draft.getAddress().map(AddressDto::of).orElse(null);
        var spaces = draft.getSpaces().map(s -> s.values().stream()
                .collect(Collectors.groupingBy(
                        Space::type,
                        Collectors.mapping(Space::area, Collectors.toList())
                ))).orElse(Collections.emptyMap());
        var measurements = draft.getMeasurements().map(MeasurementsDto::from).orElse(null);
        var amenities = draft.getAmenities().map(Amenities::values).orElse(Collections.emptySet());
        var title = draft.getTitle().orElse("GENERATED TITLE TODO");
        var description = draft.getDescription().map(MultiLangTextDto::from).orElse(MultiLangTextDto.empty());
        var pages = progress.completedStepsPlusNew();
        var total = progress.totalSteps();
        return PropertyDraftDto.builder()
                .propertyId(id)
                .type(type)
                .photos(photos)
                .address(address)
                .spaces(spaces)
                .measurements(measurements)
                .amenities(amenities)
                .title(title)
                .description(description)
                .pages(pages)
                .total(total)
                .build();
    }
}
