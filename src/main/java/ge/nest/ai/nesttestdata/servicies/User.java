package ge.nest.ai.nesttestdata.servicies;

import ge.nest.ai.nesttestdata.dto.ListingCreationRequest;
import ge.nest.ai.nesttestdata.model.Property;
import ge.nest.ai.nesttestdata.req.dtos.RegistrationProgressDto;
import ge.nest.ai.nesttestdata.req.dtos.SpacesDto;
import ge.nest.ai.nesttestdata.req.dtos.TypeDto;
import ge.nest.commons.contract.dtos.*;
import ge.nest.contract.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;

@Service
@RequiredArgsConstructor
@Slf4j
public class User {

    private final RestClient restClient;
    private final S3UploaderClientService s3;


//    private RestClient post(String path, Object body) {
//        restClient.post().uri(path).headers(h -> headers()).body(body).retrieve();
//    }
//
//    private RestClient post(String path, Object body) {
//    }

    public UUID registerType(Property property, String jwt) {
        try {
            var path = "/nest-registration/registration";
            var body = new TypeDto(property.getType());
            RegistrationProgressDto response = restClient.post().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.debug("TYPE REGISTRATION SUCCESS for id: {} and type: {}", response.registrationId(), property.getType());
            return response.registrationId();
        } catch (Exception e) {
            log.error("ERROR ON STEP PROPERTY TYPE REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }


    public UUID registerAddress(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-registration/registration/%s/address".formatted(registrationId);
            var body = new AddressDto(UUID.randomUUID().toString(), property.getFullAddressName(), property.getCoordinates().lat(), property.getCoordinates().lng(), property.getCountry(), property.getCity(), property.getStreet());
            RegistrationProgressDto response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.debug("ADDRESS REGISTRATION SUCCESS for id: {} and type: {}", response.registrationId(), property.getType());
            return response.registrationId();
        } catch (Exception e) {
            log.error("ERROR ON STEP PROPERTY ADDRESS REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }


    public PresignedUrls registerPresignedUrls(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-registration/registration/%s/presigned-urls".formatted(registrationId);
            var photos = property.getImages().stream().map(p -> new OriginalPhoto(p.url(), SupportedMimeTypes.J_PEG)).toList();
            var body = new OriginalPhotos(photos);
            PresignedUrls response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(PresignedUrls.class);
            log.debug("PRESIGNEDURLS RECEIVED SUCCESSFULLY for id: {} and type: {}", response.data(), property.getType());
            return response;
        } catch (Exception e) {
            log.error("ERROR ON STEP PRESIGNEDURLS: {}", e.getMessage());
            throw e;
        }
    }

    private final String filePath = "/Users/bekachichua/Downloads/data-1/";

    public void uploadOnS3(PresignedUrls presignedUrls) {
        try {
            List<PresignedUrl> urls = presignedUrls.data();
            for (int i = 0; i < urls.size(); i += 2) {
                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    for (int j = i; j < i + 2 && j < urls.size(); j++) {
                        var presignedUrl = urls.get(j);
                        scope.fork(() -> {
                            var ps = new S3UploaderClientService.PresignedUrlInfo(
                                    presignedUrl.url(), presignedUrl.key(), "PUT"
                            );
                            File directory = new File("/Users/bekachichua/Downloads/data-1/images");
                            File resource = new File(directory, presignedUrl.originalName());
                            s3.uploadFileWithPresignedUrl(ps, resource, "image/jpeg");
                            return null;
                        });
                    }
                    scope.join();
                    scope.throwIfFailed();
                }
            }
        } catch (Exception e) {
            log.error("ONE OR MORE PHOTO UPLOADS FAILED: {}", e.getMessage(), e);
        }
    }

    public RegistrationProgressDto notifyFileUpload(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-registration/registration/%s/presigned-urls/notify".formatted(registrationId);
            RegistrationProgressDto response = restClient.post().uri(path).headers(h -> h.setBearerAuth(jwt)).retrieve().body(RegistrationProgressDto.class);
            log.debug("S3 NOTIFY SUCCESSFULLY for id: {} and type: {}", response.registrationId(), property.getType());
            return response;
        } catch (Exception e) {
            log.error("ERROR ON STEP NOTIFY S3: {}", e.getMessage());
            throw e;
        }
    }

    public void registerSpaces(UUID registrationId, List<String> pages, Property property, String jwt) {
        try {
            if (!pages.contains("spaces")) {
                return;
            }
            var path = "/nest-registration/registration/%s/spaces".formatted(registrationId);
            List<SpacesDto.SpaceRequest> list = property.getSpaces().stream().map(s -> new SpacesDto.SpaceRequest(s.getType(), s.getArea())).toList();
            SpacesDto body = new SpacesDto(list);
            RegistrationProgressDto response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.debug("SPACES UPLOADED SUCCESSFULLY for id: {} and type: {}", response.registrationId(), property.getType());
        } catch (Exception e) {
            log.error("ERROR ON STEP SPACES UPLOADED: {}", e.getMessage());
            throw e;
        }
    }


    public RegistrationProgressDto registerMeasurements(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-registration/registration/%s/measurements".formatted(registrationId);
            var body = new MeasurementsDto(property.getTotalArea(), property.getLivingArea(), property.getBalconyArea());
            RegistrationProgressDto response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.debug("MEASUREMENTS REGISTRATION SUCCESS for id: {} and type: {}", response.registrationId(), property.getType());
            return response;
        } catch (Exception e) {
            log.error("ERROR ON STEP MEASUREMENTS REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }


    public RegistrationProgressDto registerAmeniteis(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-registration/registration/%s/amenities".formatted(registrationId);
            var body = new AmenitiesDto(List.of());
            RegistrationProgressDto response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.debug("AMENITEIS REGISTRATION SUCCESS for id: {} and type: {}", response.registrationId(), property.getType());
            return response;
        } catch (Exception e) {
            log.error("ERROR ON STEP AMENITEIS REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }


    public RegistrationProgressDto registerDescription(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-ai/agent/%s/description".formatted(registrationId);
            log.debug("STARTED DESCRIPTION GENERATION FOR ID: {} and type: {}", registrationId, property.getType());
            String response = restClient.get().uri(uriBuilder -> uriBuilder
                    .path(path)
                    .queryParam("isRegistration", true)
                    .build()
            ).headers(h -> h.setBearerAuth(jwt)).retrieve().body(String.class);
            log.debug("DESCRIPTION IS GENERATED id: {} and type: {}", registrationId, property.getType());
            var path2 = "/nest-registration/registration/%s/description".formatted(registrationId);
            var body = new TitleAndDescriptionDto(property.getTitle(), new MultiLangTextDto("ქართული", response, ""));
            RegistrationProgressDto response2 = restClient.put().uri(path2).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.debug("DESCRIPTION IS REGISTERED SUCCESS for id: {} and type: {}", registrationId, property.getType());
            return response2;
        } catch (Exception e) {
            log.error("ERROR ON STEP DESCRIPTION REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }


    public void completeRegistration(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-registration/registration/%s/register".formatted(registrationId);
            String response = restClient.post().uri(path).headers(h -> h.setBearerAuth(jwt)).retrieve().body(String.class);
            log.debug("PROPERTY REGISTERED for id: {} and type: {}", registrationId, property.getType());
        } catch (Exception e) {
            log.error("ERROR ON PROPERTY REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }

    public boolean checkPropertyStatus(UUID registrationId, Property property, String jwt) {
        int maxRetries = 100;  // Maximum 100 attempts
        int retryCount = 0;
        int retryDelayMs = 1000;
        String path = "/nest-core/property/%s/measurements".formatted(registrationId);

        try {
            // Initial delay before starting checks
            Thread.sleep(1000);

            while (retryCount < maxRetries) {
                try {
                    // Use exchange() instead of retrieve() to handle 404 responses
                    ResponseEntity<String> response = restClient.get()
                            .uri(path)
                            .headers(h -> h.setBearerAuth(jwt))
                            .exchange((req, res) -> {
                                if (res.getStatusCode().is2xxSuccessful()) {
                                    return ResponseEntity.status(res.getStatusCode())
                                            .headers(res.getHeaders())
                                            .body(res.getBody().toString());
                                } else if (res.getStatusCode().is4xxClientError()) {
                                    // Simply pass through the error status
                                    return ResponseEntity.status(res.getStatusCode()).build();
                                } else {
                                    throw new RuntimeException("Unexpected server error: " + res.getStatusCode());
                                }
                            });

                    // Check if response was successful (2xx)
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.debug("PROPERTY IS READY TO USE for id: {} and type: {}",
                                registrationId, property.getType());
                        return true;
                    }

                    // If we're here, we got a 4xx error, typically 404
                    retryCount++;

                    if (retryCount > 3) {
                        log.warn("------------------------------------------------------------");
                        log.warn("PROPERTY IS LATE TO REGISTER WITH ID: [{}], attempt {}/{}",
                                registrationId, retryCount, maxRetries);
                        log.warn("------------------------------------------------------------");
                    }

                    // Wait before retrying
                    Thread.sleep(retryDelayMs);

                } catch (Exception e) {
                    // Handle other exceptions
                    retryCount++;
                    log.warn("Error checking property status (attempt {}/{}): {}",
                            retryCount, maxRetries, e.getMessage());
                    Thread.sleep(retryDelayMs);
                }
            }

            // If we've reached max attempts without success
            log.error("PROPERTY FAILED TO REGISTER after {} attempts: {}",
                    maxRetries, registrationId);
            return false;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Property status check interrupted for ID: {}", registrationId);
            return false;
        }
    }


    public void publishForListing(UUID registrationId, Property property, String jwt) {
        try {
            var path = "/nest-listing/property/%s/listing".formatted(registrationId);
            var body = new ListingCreationRequest(BigDecimal.valueOf(property.getPrice()), property.getListingType());
            String response = restClient.post().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(String.class);
            log.debug("PROPERTY LISTED for id: {} and type: {}", registrationId, property.getType());
        } catch (Exception e) {
            log.error("ERROR ON LISTING PROPERTY: {}", e.getMessage());
            throw e;
        }
    }


    public void processPropertyRegistrationAndListing(Property property, String jwt) {
        log.info("Thread {}: Starting processing for Property ID: {}", Thread.currentThread().getName(), property.getId());
        try {
            UUID registrationId = registerType(property, jwt);
            registrationId = registerAddress(registrationId, property, jwt);

            PresignedUrls presignedUrlsResponse = registerPresignedUrls(registrationId, property, jwt);
            uploadOnS3(presignedUrlsResponse); // Pass JWT if S3UploaderClientService needs it (it doesn't in current impl)

            var res = notifyFileUpload(registrationId, property, jwt); // Added property for logging consistency

            registerSpaces(registrationId, res.pages(), property, jwt);

            registerMeasurements(registrationId, property, jwt);
            registerAmeniteis(registrationId, property, jwt); // Property not directly used here, but pass for consistency if needed
            registerDescription(registrationId, property, jwt);
            completeRegistration(registrationId, property, jwt);
            checkPropertyStatus(registrationId, property, jwt);
            publishForListing(registrationId, property, jwt);

            log.info("Thread {}: Successfully processed Property ID: {}", Thread.currentThread().getName(), property.getId());
        } catch (Exception e) {
            log.error("Thread {}: Failed processing for Property ID: {}. Error: {}", Thread.currentThread().getName(), property.getId(), e.getMessage(), e);
            // Decide on error handling: rethrow, log, etc.
        }
    }

}
