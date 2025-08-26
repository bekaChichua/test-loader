package ge.nest.ai.nesttestdata;

import ge.nest.ai.nesttestdata.model.Space;
import ge.nest.commons.valueobjects.spaces.SpaceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// Assuming SpaceType and Space class are defined as before:
// package ge.nest.commons.valueobjects.spaces;
// public enum SpaceType { BEDROOM, BATHROOM, KITCHEN, LIVING_ROOM; }

// package ge.nest.ai.nesttestdata.model;
// @PropertyData public class Space { private final SpaceType type; private final double area; }

public class SpaceCalculator {

    // Proportional weights for area distribution (can be tuned)
    private static final double BATHROOM_WEIGHT = 1.0;
    private static final double KITCHEN_WEIGHT = 2.5;
    private static final double BEDROOM_WEIGHT = 3.0;
    private static final double LIVING_ROOM_WEIGHT = 4.0;

    private int parseNumericString(Optional<String> optStr) {
        if (optStr.isEmpty()) return 0;
        try {
            String s = optStr.get();
            if (s == null || "null".equalsIgnoreCase(s.trim())) return 0;
            return (int) Math.round(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public List<Space> createSpaces(Optional<String> totalRoomsStrOpt, Optional<String> bedroomsStrOpt, Double totalArea) {
        if (totalArea == null || totalArea <= 0) {
            return Collections.emptyList();
        }

        int numTotalReportedRooms = parseNumericString(totalRoomsStrOpt);
        int numExplicitlyReportedBedrooms = parseNumericString(bedroomsStrOpt);

        int actualBedrooms = 0;
        int actualBathrooms = 0;
        int actualKitchens = 0;
        int actualLivingRooms = 0;

        // Rule: "If there is only 1 totalRooms it should be a bedroom..."
        if (numTotalReportedRooms == 1) {
            actualBedrooms = 1;
            actualKitchens = 1; // Still need a kitchen
            actualBathrooms = 1; // And a bathroom (matches bedroom, min 1)
            actualLivingRooms = 0; // The single reported room is the bedroom, no separate living room.
        }
        // Rule: "If there are 2 rooms provided one should be a bedroom and 1 bathroom."
        // Interpretation: The 2 reported rooms are effectively 1 Bedroom + 1 Living Room.
        // The "1 bathroom" is an additional requirement. Kitchen is also mandatory.
        else if (numTotalReportedRooms == 2) {
            actualBedrooms = 1;    // One of the 2 reported rooms
            actualLivingRooms = 1; // The other reported room
            actualKitchens = 1;    // Mandatory
            actualBathrooms = 1;   // As per rule, also min 1 and matches bedroom count
        }
        // General cases (numTotalReportedRooms > 2, or numTotalReportedRooms is 0/missing but area exists)
        else {
            // Rule: "Try to fit at least 1 bathroom 1 kitchen and 1 livingroom if possible
            // but never exceed more then 1 kitchen and 1 living room."
            actualKitchens = 1;
            actualLivingRooms = 1;

            if (numTotalReportedRooms > 2) {
                // Rule: "If there There are no bedrooms provided and total rooms is more then 2
                // make rest of them as bedrooms..." (bathrooms determined later)
                if (numExplicitlyReportedBedrooms == 0) {
                    actualBedrooms = numTotalReportedRooms - 1; // -1 for the living room
                }
                // Rule: "If there is more then 2 rooms provided and also you have number of bedrooms act accordingly"
                else { // numExplicitlyReportedBedrooms > 0
                    actualBedrooms = numExplicitlyReportedBedrooms;
                    // If total_rooms implies more habitable rooms (Living + Bed) than specified, increase bedrooms.
                    int impliedHabitableSpacesAfterLivingRoom = numTotalReportedRooms - 1;
                    if (impliedHabitableSpacesAfterLivingRoom > actualBedrooms) {
                        actualBedrooms = impliedHabitableSpacesAfterLivingRoom;
                    }
                }
            } else if (numTotalReportedRooms <= 0 && numExplicitlyReportedBedrooms > 0) {
                // Case: total_rooms is missing/zero, but bedrooms are specified.
                actualBedrooms = numExplicitlyReportedBedrooms;
            }
            // If numTotalReportedRooms is 0/missing and bedrooms are 0/missing, actualBedrooms remains 0 (studio).

            // Rule: "try to match the amount of bedrooms and bathrooms..."
            actualBathrooms = Math.max(1, actualBedrooms); // At least 1 bathroom, otherwise matches bedroom count.
        }


        // --- Construct list of space prototypes for area distribution ---
        List<SpacePrototype> spacePrototypes = new ArrayList<>();
        if (actualLivingRooms > 0) {
            for (int i = 0; i < actualLivingRooms; i++)
                spacePrototypes.add(new SpacePrototype(SpaceType.LIVING_ROOM, LIVING_ROOM_WEIGHT));
        }
        if (actualKitchens > 0) {
            for (int i = 0; i < actualKitchens; i++)
                spacePrototypes.add(new SpacePrototype(SpaceType.KITCHEN, KITCHEN_WEIGHT));
        }
        if (actualBedrooms > 0) {
            for (int i = 0; i < actualBedrooms; i++)
                spacePrototypes.add(new SpacePrototype(SpaceType.BEDROOM, BEDROOM_WEIGHT));
        }
        if (actualBathrooms > 0) {
            for (int i = 0; i < actualBathrooms; i++)
                spacePrototypes.add(new SpacePrototype(SpaceType.BATHROOM, BATHROOM_WEIGHT));
        }

        if (spacePrototypes.isEmpty()) {
            return Collections.emptyList();
        }

        // --- Distribute total area based on weights ---
        double totalWeight = 0;
        for (SpacePrototype sp : spacePrototypes) {
            totalWeight += sp.weight;
        }

        if (totalWeight == 0) return Collections.emptyList(); // Should not happen if prototypes exist

        List<Space> finalSpaces = new ArrayList<>();
        double areaSumForVerification = 0;
        for (int i = 0; i < spacePrototypes.size(); i++) {
            SpacePrototype sp = spacePrototypes.get(i);
            double calculatedArea;
            // Assign remaining area to the last space to ensure sum is exact
            if (i == spacePrototypes.size() - 1) {
                calculatedArea = totalArea - areaSumForVerification;
            } else {
                calculatedArea = (sp.weight / totalWeight) * totalArea;
            }
            // Ensure non-negative and round to reasonable precision (e.g., 2 decimal places)
            calculatedArea = Math.max(0, Math.round(calculatedArea * 100.0) / 100.0);
            finalSpaces.add(new Space(sp.type, calculatedArea));
            areaSumForVerification += calculatedArea;
        }
        // Due to rounding the last element might need slight adjustment if sum is off
        if (spacePrototypes.size() > 0 && Math.abs(areaSumForVerification - totalArea) > 0.001) {
            Space lastSpace = finalSpaces.get(finalSpaces.size() - 1);
            double adjustment = totalArea - areaSumForVerification;
            double newLastArea = Math.max(0, Math.round((lastSpace.getArea() + adjustment) * 100.0) / 100.0);
            finalSpaces.set(finalSpaces.size() - 1, new Space(lastSpace.getType(), newLastArea));
        }


        return finalSpaces;
    }

    private static class SpacePrototype {
        SpaceType type;
        double weight;

        SpacePrototype(SpaceType type, double weight) {
            this.type = type;
            this.weight = weight;
        }
    }

    // --- Example Usage (for testing) ---
    public static void main(String[] args) {
        SpaceCalculator calculator = new SpaceCalculator();

        System.out.println("--- Testing with new rules ---");

        BiConsumer<List<Space>, String> printSpaces = (spaces, label) -> {
            System.out.println("\n" + label + ":");
            if (spaces.isEmpty()) {
                System.out.println("  No spaces generated.");
                return;
            }
            double sumArea = 0;
            for (Space s : spaces) {
                System.out.printf("  %s: %.2f sqm%n", s.getType(), s.getArea());
                sumArea += s.getArea();
            }
            System.out.printf("  TOTAL CALCULATED AREA: %.2f sqm%n", sumArea);
        };

        // Rule: "If there is only 1 totalRooms it should be a bedroom..."
        printSpaces.accept(calculator.createSpaces(Optional.of("1"), Optional.empty(), 30.0),
                "Total: 1, Beds: empty, Area: 30.0");
        // Expected: 1 BR, 1 K, 1 BA. No LR.

        // Rule: "If there are 2 rooms provided one should be a bedroom and 1 bathroom."
        printSpaces.accept(calculator.createSpaces(Optional.of("2"), Optional.empty(), 45.0),
                "Total: 2, Beds: empty, Area: 45.0");
        // Expected: 1 BR, 1 LR, 1 K, 1 BA.

        printSpaces.accept(calculator.createSpaces(Optional.of("2"), Optional.of("1"), 50.0),
                "Total: 2, Beds: 1, Area: 50.0");
        // Expected: 1 BR, 1 LR, 1 K, 1 BA (Rule for total=2 is specific)

        // Rule: "If there There are no bedrooms provided and total rooms is more then 2 make rest of them as bedrooms..."
        printSpaces.accept(calculator.createSpaces(Optional.of("3"), Optional.empty(), 70.0),
                "Total: 3, Beds: empty, Area: 70.0");
        // Expected: 1 LR, 1 K, (3-1)=2 BR, 2 BA.

        printSpaces.accept(calculator.createSpaces(Optional.of("4"), Optional.of("0"), 90.0),
                "Total: 4, Beds: 0, Area: 90.0");
        // Expected: 1 LR, 1 K, (4-1)=3 BR, 3 BA.

        // Rule: "If there is more then 2 rooms provided and also you have number of bedrooms act accordingly"
        printSpaces.accept(calculator.createSpaces(Optional.of("3"), Optional.of("1"), 75.0),
                "Total: 3, Beds: 1, Area: 75.0");
        // Expected: 1 LR, 1 K. Beds: max(1, 3-1=2) = 2 BR. 2 BA.

        printSpaces.accept(calculator.createSpaces(Optional.of("4"), Optional.of("2"), 100.0),
                "Total: 4, Beds: 2, Area: 100.0");
        // Expected: 1 LR, 1 K. Beds: max(2, 4-1=3) = 3 BR. 3 BA.

        printSpaces.accept(calculator.createSpaces(Optional.of("3"), Optional.of("3"), 85.0),
                "Total: 3, Beds: 3, Area: 85.0");
        // Expected: 1 LR, 1 K. Beds: max(3, 3-1=2) = 3 BR. 3 BA.

        // Case: total_rooms missing/0, but bedrooms specified
        printSpaces.accept(calculator.createSpaces(Optional.empty(), Optional.of("1"), 40.0),
                "Total: empty, Beds: 1, Area: 40.0");
        // Expected: 1 LR, 1 K, 1 BR, 1 BA.

        // Case: total_rooms missing/0, bedrooms missing/0 (Studio-like)
        printSpaces.accept(calculator.createSpaces(Optional.empty(), Optional.empty(), 25.0),
                "Total: empty, Beds: empty, Area: 25.0 (Studio)");
        // Expected: 1 LR, 1 K, 1 BA. (0 BR)
        printSpaces.accept(calculator.createSpaces(Optional.of("0"), Optional.of("0"), 28.0),
                "Total: 0, Beds: 0, Area: 28.0 (Studio)");
        // Expected: 1 LR, 1 K, 1 BA. (0 BR)
    }

    // Added BiConsumer for cleaner main method
    @FunctionalInterface
    interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}