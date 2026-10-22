import java.util.List;

/**
 * A cyclist's review of a bike park. Photos are stored as references, not bytes.
 */
public final class Review {

    private final int rating;
    private final String comment;
    private final List<String> photos;
    private final List<Integer> categoryRatings;

    public Review(int rating, String comment, List<String> photos, List<Integer> categoryRatings) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5: " + rating);
        }
        this.rating = rating;
        this.comment = comment;
        this.photos = List.copyOf(photos);
        this.categoryRatings = List.copyOf(categoryRatings);
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public List<Integer> getCategoryRatings() {
        return categoryRatings;
    }
}
