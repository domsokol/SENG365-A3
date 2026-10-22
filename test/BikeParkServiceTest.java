import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.javalin.testtools.JavalinTest;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BikeParkServiceTest {

    private static SearchService searchService() {
        return new SearchService(new DataLoader().loadBikeParks(Path.of("data", "bikeparks.json")));
    }

    @Test
    void searchReturnsNearbyParksNearestFirst() {
        JavalinTest.test(BikeParkService.createApp(searchService()), (server, client) -> {
            var response = client.get("/bikeparks?latitude=-43.5235&longitude=172.5826&radius=300");
            String body = response.body().string();

            assertEquals(200, response.code());
            assertTrue(body.startsWith("[{\"id\":\"bp-001\""), body);
            assertTrue(body.contains("\"distanceMetres\""), body);
        });
    }

    @Test
    void searchReturnsEmptyListWhenNothingInRadius() {
        JavalinTest.test(BikeParkService.createApp(searchService()), (server, client) -> {
            var response = client.get("/bikeparks?latitude=0&longitude=0&radius=100");

            assertEquals(200, response.code());
            assertEquals("[]", response.body().string());
        });
    }

    @Test
    void searchRejectsMissingParameter() {
        JavalinTest.test(BikeParkService.createApp(searchService()), (server, client) -> {
            var response = client.get("/bikeparks?latitude=-43.5235&longitude=172.5826");

            assertEquals(400, response.code());
            assertTrue(response.body().string().contains("radius"));
        });
    }

    @Test
    void searchRejectsNonNumericParameter() {
        JavalinTest.test(BikeParkService.createApp(searchService()), (server, client) -> {
            assertEquals(400, client.get("/bikeparks?latitude=abc&longitude=172.5826&radius=300").code());
        });
    }

    @Test
    void searchRejectsOutOfRangeCoordinates() {
        JavalinTest.test(BikeParkService.createApp(searchService()), (server, client) -> {
            assertEquals(400, client.get("/bikeparks?latitude=-123&longitude=172.5826&radius=300").code());
        });
    }

    @Test
    void searchRejectsNonPositiveRadius() {
        JavalinTest.test(BikeParkService.createApp(searchService()), (server, client) -> {
            assertEquals(400, client.get("/bikeparks?latitude=-43.5235&longitude=172.5826&radius=0").code());
        });
    }
}
