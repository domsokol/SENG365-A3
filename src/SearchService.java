import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Finds bike parks near a destination.
 */
public final class SearchService {

    private static final double EARTH_RADIUS_METRES = 6_371_000;

    private final BikeParkRepository repository;

    public SearchService(BikeParkRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository must not be null");
        }
        this.repository = repository;
    }

    /**
     * Returns the bike parks within {@code radiusMetres} of the destination, ordered from
     * nearest to farthest. Parks at the same distance are ordered by id. Returns an empty
     * list if no bike parks are within the radius.
     */
    public List<SearchResult> searchParks(Location destination, double radiusMetres) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination must not be null");
        }
        if (!Double.isFinite(radiusMetres) || radiusMetres <= 0) {
            throw new IllegalArgumentException("Radius must be a positive number of metres: " + radiusMetres);
        }

        List<SearchResult> results = new ArrayList<>();
        for (BikePark park : repository.findAll()) {
            double distanceMetres = distance(destination, park.getLocation());
            if (distanceMetres <= radiusMetres) {
                results.add(new SearchResult(park, distanceMetres));
            }
        }
        results.sort(Comparator.comparingDouble(SearchResult::getDistanceMetres)
                .thenComparing(result -> result.getBikePark().getId()));
        return results;
    }

    /**
     * Returns the great-circle distance in metres between two locations, using the haversine formula.
     */
    public double distance(Location from, Location to) {
        double fromLatitude = Math.toRadians(from.getLatitude());
        double toLatitude = Math.toRadians(to.getLatitude());
        double latitudeDelta = toLatitude - fromLatitude;
        double longitudeDelta = Math.toRadians(to.getLongitude() - from.getLongitude());

        double a = Math.pow(Math.sin(latitudeDelta / 2), 2)
                + Math.cos(fromLatitude) * Math.cos(toLatitude) * Math.pow(Math.sin(longitudeDelta / 2), 2);
        return 2 * EARTH_RADIUS_METRES * Math.asin(Math.min(1, Math.sqrt(a)));
    }
}
