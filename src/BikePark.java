import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A bike parking location and the reviews cyclists have left for it.
 */
public final class BikePark {

    private final String id;
    private final String name;
    private final Location location;
    private final List<Review> reviews = new ArrayList<>();

    public BikePark(String id, String name, Location location) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Bike park id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Bike park name must not be blank");
        }
        if (location == null) {
            throw new IllegalArgumentException("Bike park location must not be null");
        }
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }
}
