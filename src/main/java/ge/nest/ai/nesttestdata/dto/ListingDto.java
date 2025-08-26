package ge.nest.ai.nesttestdata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ge.nest.commons.valueobjects.listing.ListingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ListingDto {

    @JsonProperty("listing_id")
    private long listingId;

    @JsonProperty("listing_type")
    private ListingType listingType;

    @JsonProperty("property_type")
    private String propertyType;

    @JsonProperty("total_price")
    private int totalPrice;

    @JsonProperty("square_meter_price")
    private int squareMeterPrice;

    @JsonProperty("lat")
    private double lat;

    @JsonProperty("lng")
    private double lng;

    @JsonProperty("address")
    private String address;

    @JsonProperty("area")
    private Double area;

    @JsonProperty("yard_area")
    private Double yardArea;

    @JsonProperty("num_rooms")
    private String numRooms;

    @JsonProperty("num_bedrooms")
    private String numBedrooms;

    @JsonProperty("generated_title")
    private String generatedTitle;

    public String getGeneratedTitle() {
        return generatedTitle.substring(0, Math.min(50, generatedTitle.length()));
    }

    @JsonProperty("floor")
    private int floor;

    @JsonProperty("total_floors")
    private int totalFloors;

    @JsonProperty("district_name")
    private String districtName;

    @JsonProperty("city_name")
    private String cityName;

    @JsonProperty("images")
    private List<String> images;

    public Optional<String> getAddress() {
        return Optional.ofNullable(address);
    }

    public Optional<Double> getYardArea() {
        return Optional.ofNullable(yardArea);
    }

    public Optional<String> getNumRooms() {
        return Optional.ofNullable(numBedrooms);
    }

    public Optional<String> getNumBedrooms() {
        return Optional.ofNullable(numBedrooms);
    }

    public Optional<String> getDistrictName() {
        return Optional.ofNullable(districtName);
    }

    public Optional<String> getCityName() {
        return Optional.ofNullable(cityName);
    }
}
