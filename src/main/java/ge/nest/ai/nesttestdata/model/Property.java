package ge.nest.ai.nesttestdata.model;

import ge.nest.ai.nesttestdata.dto.ListingDto;
import ge.nest.ai.nesttestdata.dto.SpaceCalculator;
import ge.nest.commons.valueobjects.listing.ListingType;
import ge.nest.commons.valueobjects.location.Coordinates;
import ge.nest.commons.valueobjects.property.PropertyType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
public class Property {
    @Id
    private final Long id;
    private final PropertyType type;
    private final ListingType listingType;
    private final int price;
    private final String title;
    private final Double totalArea;
    private final Double livingArea;
    private final Double balconyArea;
    @Embedded.Nullable
    private final Coordinates coordinates;
    private final String fullAddressName;
    private final String country;
    private final String city;
    private final String street;
    @MappedCollection
    private final List<Photo> images;
    @MappedCollection
    private final List<Space> spaces;

    private static Property createLand(ListingDto dto) {
        var totalArea = dto.getArea();
        var livingArea = getLivingArea(dto.getArea(), dto.getYardArea());
        var balconyArea = getBalconyArea(dto.getArea(), dto.getYardArea());

        if (totalArea < (livingArea + balconyArea)) {
            livingArea = totalArea;
            balconyArea = 0.0;
        }

        if (livingArea < 0) {
            livingArea = 0.0;
        }

        if (balconyArea < 0) {
            balconyArea = 0.0;
        }

        return new Property(
                null,
                PropertyType.valueOf(dto.getPropertyType()),
                dto.getListingType(),
                dto.getTotalPrice(),
                dto.getGeneratedTitle(),
                totalArea,
                livingArea,
                balconyArea,
                createCoordinates(dto.getLat(), dto.getLng()),
                (dto.getAddress().orElse("12 Example St.") + ", " + dto.getDistrictName().orElse("Tbilisi Municipality") + ", " + dto.getCityName().orElse("Tbilisi")),
                "Georgia",
                dto.getCityName().orElse(dto.getDistrictName().orElse("Tbilisi")),
                dto.getAddress().orElse("12 Example St."),
                dto.getImages().stream().map(Photo::new).toList(),
                Collections.emptyList()
        );
    }

    private static Property createResidential(ListingDto dto) {
        var totalArea = dto.getArea();
        var livingArea = getLivingArea(dto.getArea(), dto.getYardArea());
        var balconyArea = getBalconyArea(dto.getArea(), dto.getYardArea());

        if (totalArea < (livingArea + balconyArea)) {
            livingArea = totalArea;
            balconyArea = 0.0;
        }

        if (livingArea < 0) {
            livingArea = 0.0;
        }

        if (balconyArea < 0) {
            balconyArea = 0.0;
        }

        return new Property(
                null,
                PropertyType.valueOf(dto.getPropertyType()),
                dto.getListingType(),
                dto.getTotalPrice(),
                dto.getGeneratedTitle(),
                totalArea,
                livingArea,
                balconyArea,
                createCoordinates(dto.getLat(), dto.getLng()),
                (dto.getAddress().orElse("12 Example St.") + ", " + dto.getDistrictName().orElse("Tbilisi Municipality") + ", " + dto.getCityName().orElse("Tbilisi")),
                "Georgia",
                dto.getCityName().orElse(dto.getDistrictName().orElse("Tbilisi")),
                dto.getAddress().orElse("12 Example St."),
                dto.getImages().stream().map(Photo::new).toList(),
                createSpace(dto.getNumRooms(), dto.getNumBedrooms(), dto.getArea())
        );
    }

    public static Property createProperty(ListingDto dto) {
        if (dto.getPropertyType().equals("LAND")) {
            if (Math.random() > 0.5) {
                dto.setPropertyType("RESIDENTIAL_LAND");
            } else {
                dto.setPropertyType("AGRICULTURAL_LAND");
            }
            return createLand(dto);
        }

        return createResidential(dto);
    }


    private static Coordinates createCoordinates(double lat, double lng) {
        var CorrectLat = Math.min(lat, lng);
        var CorrectLng = Math.max(lat, lng);
        return Coordinates.of(CorrectLat, CorrectLng);
    }

    private static List<Space> createSpace(Optional<String> total, Optional<String> bedroomsOpt, Double area) {
        SpaceCalculator calculator = new SpaceCalculator();
        return calculator.createSpaces(total, bedroomsOpt, area);
    }

    private static Double getLivingArea(Double totalArea, Optional<Double> balconyArea) {
        var livingArea = totalArea - (balconyArea.orElse(0.0));
        return Math.min(livingArea, 0);
    }

    private static Double getBalconyArea(Double totalArea, Optional<Double> balconyArea) {
        var balcony = balconyArea.orElse(0.0);
        return Math.min(totalArea - balcony, 0);
    }

}
