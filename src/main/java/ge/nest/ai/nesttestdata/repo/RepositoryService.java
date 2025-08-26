package ge.nest.ai.nesttestdata.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.nest.ai.nesttestdata.dto.PropertyData;
import ge.nest.ai.nesttestdata.model.Property;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepositoryService {

    private final PropertyRepo propertyRepo;
    private final ObjectMapper objectMapper;

    @Value("/Users/bekachichua/Downloads/data-1") // Or "classpath:data.json"
    private Resource dataFileResource;
    private final ResourceLoader resourceLoader;

    @Transactional
    public void init() {
        loadJsonUsingResourceLoader();
    }

    private void loadJsonUsingResourceLoader() {
        try {
            String filePath = "file:/Users/bekachichua/Downloads/data-1/data-1.json";
            // For Windows, it would be like: "file:C:/Users/YourUser/Downloads/data.json"

            Resource resource = resourceLoader.getResource(filePath); // Path within resources
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    var result = objectMapper.readValue(inputStream, PropertyData.class);
                    log.info("Successfully loaded DTO (ResourceLoader): {}",  result.data().size());
                    List<Property> list = result
                            .data()
                            .stream()
                            .filter(d -> !(d.getPropertyType().equals("HOSPITALITY")))
                            .filter(d -> !(d.getPropertyType().equals("COMMERCIAL_SPACE")))
                            .map(Property::createProperty).toList();
                    propertyRepo.saveAll(list);
                    log.info("Successfully saved {} properties to the database", list.size());
                }
            } else {
                System.err.println("Resource not found: classpath:static/data.json");
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file (ResourceLoader): " + e.getMessage());
            // Handle exception (e.g., throw a custom exception, log, set default DTO)
        }
    }



}

