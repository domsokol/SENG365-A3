import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchServiceTest {

    private static final Location DESTINATION = new Location(-43.5225, 172.5810);

    private static BikePark park(String id, double latitude, double longitude) {
        return new BikePark(id, "Park " + id, new Location(latitude, longitude));
    }

    private static List<String> ids(List<SearchResult> results) {
        return results.stream().map(result -> result.getBikePark().getId()).toList();
    }

    @Test
    void returnsParksWithinRadiusOrderedNearestToFarthest() {
        SearchService service = new SearchService(new BikeParkRepository(List.of(
                park("far", -43.5265, 172.5810),
                park("near", -43.5230, 172.5810),
                park("middle", -43.5245, 172.5810))));

        List<SearchResult> results = service.searchParks(DESTINATION, 1000);

        assertEquals(List.of("near", "middle", "far"), ids(results));
    }

    @Test
    void excludesParksOutsideRadius() {
        SearchService service = new SearchService(new BikeParkRepository(List.of(
                park("inside", -43.5230, 172.5810),
                park("outside", -43.5400, 172.5810))));

        List<SearchResult> results = service.searchParks(DESTINATION, 500);

        assertEquals(List.of("inside"), ids(results));
    }

    @Test
    void returnsEmptyListWhenNoParksWithinRadius() {
        SearchService service = new SearchService(new BikeParkRepository(List.of(
                park("outside", -43.5400, 172.5810))));

        assertTrue(service.searchParks(DESTINATION, 500).isEmpty());
    }

    @Test
    void reportsDistanceToEachPark() {
        SearchService service = new SearchService(new BikeParkRepository(List.of(
                park("bp", -43.5315, 172.5810))));

        SearchResult result = service.searchParks(DESTINATION, 2000).get(0);

        // 0.009 degrees of latitude is about 1000.75 metres.
        assertEquals(1000.75, result.getDistanceMetres(), 1.0);
    }

    @Test
    void parksAtSameDistanceAreOrderedById() {
        SearchService service = new SearchService(new BikeParkRepository(List.of(
                park("b", -43.5235, 172.5810),
                park("a", -43.5235, 172.5810))));

        assertEquals(List.of("a", "b"), ids(service.searchParks(DESTINATION, 500)));
    }

    @Test
    void rejectsNonPositiveRadius() {
        SearchService service = new SearchService(new BikeParkRepository(List.of()));

        assertThrows(IllegalArgumentException.class, () -> service.searchParks(DESTINATION, 0));
        assertThrows(IllegalArgumentException.class, () -> service.searchParks(DESTINATION, -5));
        assertThrows(IllegalArgumentException.class, () -> service.searchParks(DESTINATION, Double.NaN));
    }

    @Test
    void rejectsMissingDestination() {
        SearchService service = new SearchService(new BikeParkRepository(List.of()));

        assertThrows(IllegalArgumentException.class, () -> service.searchParks(null, 500));
    }

    @Test
    void searchesProvidedDataLoadedAtStartup() {
        BikeParkRepository repository = new DataLoader().loadBikeParks(Path.of("data", "bikeparks.json"));
        SearchService service = new SearchService(repository);

        List<SearchResult> results = service.searchParks(new Location(-43.5235, 172.5826), 300);

        assertEquals("bp-001", results.get(0).getBikePark().getId());
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getDistanceMetres() <= results.get(i).getDistanceMetres());
        }
    }
}
