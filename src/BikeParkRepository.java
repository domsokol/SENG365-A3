import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory store of bike parks, keyed by id.
 */
public final class BikeParkRepository {

    private final Map<String, BikePark> parksById;

    /**
     * Creates a repository holding the given bike parks. Every park is checked before
     * the repository is built, so an invalid list never produces a partially populated repository.
     */
    public BikeParkRepository(List<BikePark> parks) {
        Map<String, BikePark> staged = new LinkedHashMap<>();
        for (BikePark park : parks) {
            if (park == null) {
                throw new IllegalArgumentException("Bike park must not be null");
            }
            if (staged.putIfAbsent(park.getId(), park) != null) {
                throw new IllegalArgumentException("Duplicate bike park id: " + park.getId());
            }
        }
        this.parksById = Collections.unmodifiableMap(staged);
    }

    public Collection<BikePark> findAll() {
        return parksById.values();
    }

    public Optional<BikePark> findById(String id) {
        return Optional.ofNullable(parksById.get(id));
    }
}
