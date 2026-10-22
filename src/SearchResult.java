/**
 * A bike park found by a search, with its distance from the searched destination.
 */
public final class SearchResult {

    private final BikePark bikePark;
    private final double distanceMetres;

    public SearchResult(BikePark bikePark, double distanceMetres) {
        this.bikePark = bikePark;
        this.distanceMetres = distanceMetres;
    }

    public BikePark getBikePark() {
        return bikePark;
    }

    public double getDistanceMetres() {
        return distanceMetres;
    }
}
