package ge.nest.ai.nesttestdata;

import ge.nest.ai.nesttestdata.model.Property;
import ge.nest.ai.nesttestdata.repo.PropertyRepo;
import ge.nest.ai.nesttestdata.servicies.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTestService {

    private final PropertyRepo propertyRepo;
    private final User userService; // Assuming User is a Spring bean

    // A fixed JWT for all test users. In a real scenario, each user might have their own.
    private final String TEST_USER_JWT = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI3WlhPc2VVR20xckU0clJERXlVcEdZbFo1MUpTV25SWldzbGJZTzloZWJZIn0.eyJleHAiOjE3NTM4NjQzMDYsImlhdCI6MTc1MzgyODk1NCwiYXV0aF90aW1lIjoxNzUzODI4MzA2LCJqdGkiOiJvbnJ0YWM6NmE5ZjFlNDItY2ZiZS00OGFiLWViMDQtMjE2YTAzNGU3MzA0IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDg4L3JlYWxtcy9uZXN0IiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjA1M2JkMTIwLTIyMmUtNDI3Yi04NDYxLWZlMWNhNGRlOGIyZSIsInR5cCI6IkJlYXJlciIsImF6cCI6Im5lc3QtY2xpZW50Iiwic2lkIjoiMGFjM2E2ZjQtZWY5YS00MDM2LWE3NGEtZDZiMzlmZmQ1YmQ4IiwiYWNyIjoiMCIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwOTUiLCJodHRwOi8vbG9jYWxob3N0OjgwOTQiLCJodHRwOi8vbG9jYWxob3N0OjgwOTEiLCJodHRwOi8vbG9jYWxob3N0OjgwOTAiLCJodHRwOi8vbG9jYWxob3N0OjgwOTMiLCJodHRwOi8vbG9jYWxob3N0OjgwOTIiLCJodHRwOi8vbG9jYWxob3N0OjMwMDAiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iLCJkZWZhdWx0LXJvbGVzLW5lc3QiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiTmlrb2xveiBDaGljaHVhIiwicHJlZmVycmVkX3VzZXJuYW1lIjoibmlrY2hpY2h1YSIsImdpdmVuX25hbWUiOiJOaWtvbG96IiwiZmFtaWx5X25hbWUiOiJDaGljaHVhIiwiZW1haWwiOiJuaWsuY2hpY2h1YUBnbWFpbC5jb20ifQ.RJv-7yXPAIwOq4cTL5-IzYfZoSJ0Kpyd9RH37sb4yKmoMNHYHQnaq1zn51bNRkdPFSaKb0zgDB8qKrgQeZk8SfayBJYM74Mu836viWog64ELSgbWDhBGxOjTxkDY7ZZApLJlEhyKAIfiQQMkzhk6jK2Vkv6BH90G-vtCWHkM1bRsgksB4yqCC6MXe3gnLxtK0a1rtVJJ0Iv0u5uJVxT6xqufpaZuQxtiZSfEp3eIbyz5IMLA1hdHJHgkv0cTey9tDyGeQPwmRNyC9k4c6sL9mIy7XEtPBPksS4hWrNmbjVtrawLSmjlTkVwl__70kphnUdpfGbCBNmvFKwEvN-O7VA";

    public void runLoadTest(int numberOfConcurrentUsers, int totalPropertiesToProcess) { // Renamed parameter for clarity
        log.info("Starting load test with {} concurrent worker threads, processing up to {} properties.",
                numberOfConcurrentUsers, totalPropertiesToProcess);

        // Fetch the exact number of distinct properties needed for the test
        Pageable pageable = PageRequest.of(0, totalPropertiesToProcess);
        List<Property> propertiesToTest = propertyRepo.findAllBy(pageable);

        if (propertiesToTest.isEmpty()) {
            log.warn("No properties found in the repository to run the load test.");
            return;
        }

        if (propertiesToTest.size() < totalPropertiesToProcess) {
            log.warn("Warning: Fetched only {} properties from the repository, but requested to process {}. " +
                     "The test will run with {} properties.",
                    propertiesToTest.size(), totalPropertiesToProcess, propertiesToTest.size());
            // No need to adjust totalPropertiesToProcess, as we will iterate over propertiesToTest
        }

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfConcurrentUsers);
        AtomicInteger processedCount = new AtomicInteger(0); // To count processed properties for logging

        log.info("Submitting {} tasks to process {} properties using {} worker threads.",
                propertiesToTest.size(), propertiesToTest.size(), numberOfConcurrentUsers);

        // For each property we want to process, submit a task to the executor service
        for (Property propertyToProcess : propertiesToTest) {
            executorService.submit(() -> {
                // The thread name is managed by the executor service,
                // but you can add more specific logging if needed.
                // String threadName = Thread.currentThread().getName();
                log.info("Thread {} starting to process Property ID: {}", Thread.currentThread().getName(), propertyToProcess.getId());
                try {
                    // Each thread calls the orchestrating method on the userService bean
                    userService.processPropertyRegistrationAndListing(propertyToProcess, TEST_USER_JWT);
                    int currentProcessedCount = processedCount.incrementAndGet();
                    log.info("Thread {} successfully processed Property ID: {}. Total processed so far: {}",
                            Thread.currentThread().getName(), propertyToProcess.getId(), currentProcessedCount);
                } catch (Exception e) {
                    log.error("Thread {} encountered an error processing Property ID {}: {}",
                            Thread.currentThread().getName(), propertyToProcess.getId(), e.getMessage(), e);
                }
                log.info("Thread {} finished processing Property ID: {}", Thread.currentThread().getName(), propertyToProcess.getId());
            });
        }

        executorService.shutdown();
        try {
            // Wait for all tasks to complete or timeout
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) { // Adjust timeout as needed
                log.warn("Load test tasks did not complete within the timeout period. Forcing shutdown.");
                executorService.shutdownNow();
            } else {
                log.info("All load test tasks completed successfully.");
            }
        } catch (InterruptedException e) {
            log.error("Load test was interrupted.", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt(); // Preserve interrupt status
        }

        log.info("Load test finished. Attempted to process {} properties. {} properties were successfully processed according to counter.",
                propertiesToTest.size(), processedCount.get());
    }
}