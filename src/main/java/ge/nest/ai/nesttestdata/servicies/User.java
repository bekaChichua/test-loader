package ge.nest.ai.nesttestdata;

import ge.nest.ai.nesttestdata.model.Photo;
import ge.nest.ai.nesttestdata.model.Property;
import ge.nest.ai.nesttestdata.req.dtos.RegistrationProgressDto;
import ge.nest.ai.nesttestdata.req.dtos.TypeDto;
import ge.nest.commons.contract.dtos.AddressDto;
import ge.nest.commons.valueobjects.property.PropertyType;
import ge.nest.contract.OriginalPhoto;
import ge.nest.contract.OriginalPhotos;
import ge.nest.contract.PresignedUrls;
import ge.nest.contract.SupportedMimeTypes;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class User {

    private final RestClient restClient;

    @Setter
    private Property property;
    @Setter
    private String jwt;


//    private RestClient post(String path, Object body) {
//        restClient.post().uri(path).headers(h -> headers()).body(body).retrieve();
//    }
//
//    private RestClient post(String path, Object body) {
//    }

    public UUID registerType() {
        try {
            var path = "/property-registration/property";
            var body = new TypeDto(property.getType());
            RegistrationProgressDto response = restClient.post().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.info("TYPE REGISTRATION SUCCESS for id: {} and type: {}", response.registrationId(), property.getType());
            return response.registrationId();
        } catch (Exception e) {
            log.info("ERROR ON STEP PROPERTY TYPE REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }


    public UUID registerAddress(UUID registrationId) {
        try {
            var path = "/property-registration/property/%s/address".formatted(registrationId);
            var body = new AddressDto(UUID.randomUUID().toString(), property.getFullAddressName(), property.getCoordinates().lat(), property.getCoordinates().lng(), property.getCountry(), property.getCity(), property.getStreet());
            RegistrationProgressDto response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(RegistrationProgressDto.class);
            log.info("ADDRESS REGISTRATION SUCCESS for id: {} and type: {}", response.registrationId(), property.getType());
            return response.registrationId();
        } catch (Exception e) {
            log.info("ERROR ON STEP PROPERTY ADDRESS REGISTRATION: {}", e.getMessage());
            throw e;
        }
    }


    public PresignedUrls registerPresignedUrls(UUID registrationId) {
        try {
            var path = "/property-registration/property/%s/presigned-urls".formatted(registrationId);
            var photos = property.getImages().stream().map(p -> new OriginalPhoto(p.url(), SupportedMimeTypes.J_PEG)).toList();
            var body = new OriginalPhotos(photos);
            PresignedUrls response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(PresignedUrls.class);
            log.info("PRESIGNEDURLS RECEIVED SUCCESSFULLY for id: {} and type: {}", response.data(), property.getType());
            return response;
        } catch (Exception e) {
            log.info("ERROR ON STEP PRESIGNEDURLS: {}", e.getMessage());
            throw e;
        }
    }

    public PresignedUrls uploadOnS3(PresignedUrls PresignedUrls) {
        try {
            var path = "/property-registration/property/%s/presigned-urls".formatted(registrationId);
            var photos = property.getImages().stream().map(p -> new OriginalPhoto(p.url(), SupportedMimeTypes.J_PEG)).toList();
            var body = new OriginalPhotos(photos);
            PresignedUrls response = restClient.put().uri(path).headers(h -> h.setBearerAuth(jwt)).body(body).retrieve().body(PresignedUrls.class);
            log.info("PRESIGNEDURLS RECEIVED SUCCESSFULLY for id: {} and type: {}", response.data(), property.getType());
            return response;
        } catch (Exception e) {
            log.info("ERROR ON STEP PRESIGNEDURLS: {}", e.getMessage());
            throw e;
        }
    }

}
