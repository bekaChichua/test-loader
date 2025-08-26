package ge.nest.listing.web;

import ge.nest.commons.valueobjects.listing.ListingType;

import java.math.BigDecimal;

public record ListingCreationRequest(
        BigDecimal price,
        ListingType listingType
) {
}
