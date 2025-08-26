package ge.nest.ai.nesttestdata;

import ge.nest.ai.nesttestdata.repo.PropertyRepo;
import ge.nest.ai.nesttestdata.repo.RepositoryService;
import ge.nest.ai.nesttestdata.servicies.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootApplication
@Slf4j
public class NestTestDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(NestTestDataApplication.class, args);
    }


    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://10.0.0.10:8090")
                .build();
    }


    @Bean
    public CommandLineRunner loadTestExecutor(LoadTestService loadTestService) {
        return args -> {
            int concurrentUsers = 30;
            int propertiesToFetch = 1000;
            System.out.println("LOAD TEST PROFILE ACTIVE - STARTING LOAD TEST");

            // 1. Record the start time
            long startTime = System.currentTimeMillis();

            // 2. Run the method
            loadTestService.runLoadTest(concurrentUsers, propertiesToFetch);

            // 3. Record the end time
            long endTime = System.currentTimeMillis();

            // 4. Calculate the duration in milliseconds
            long durationMs = endTime - startTime;

            // 5. Convert to seconds (as a double for precision)
            double durationSeconds = durationMs / 1000.0;

            System.out.println("loadTestService.runLoadTest took " + durationSeconds + " seconds to complete.");
            System.out.println("LOAD TEST COMMANDLINE RUNNER COMPLETED");
        };
    }


//    @Bean
//    public CommandLineRunner loadData(RepositoryService repo) {
//        return args -> {
//            repo.init();
//        };
//    }


    @Bean
    S3Client s3Client() {
        return S3Client.builder()
                .region(Region.EU_CENTRAL_1) //todo move this to env's ...
                .build();
    }

}
