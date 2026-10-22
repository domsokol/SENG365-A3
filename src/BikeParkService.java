import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Starts the service: loads bike park data into memory and serves the HTTP API with Javalin.
 */
public final class BikeParkService {

    private static final Path DEFAULT_DATA_FILE = Path.of("data", "bikeparks.json");
    private static final int PORT = 7070;

    public static void main(String[] args) {
        Path dataFile = args.length > 0 ? Path.of(args[0]) : DEFAULT_DATA_FILE;
        BikeParkRepository repository;
        try {
            repository = new DataLoader().loadBikeParks(dataFile);
        } catch (InitializationException e) {
            System.err.println("Initialization error: " + e.getMessage());
            System.exit(1);
            return;
        }
        System.out.println("Loaded " + repository.findAll().size() + " bike parks from " + dataFile);

        createApp(new SearchService(repository)).start(PORT);
    }

    /**
     * Builds the Javalin app and its routes without starting it, so tests can run it on any port.
     */
    public static Javalin createApp(SearchService searchService) {
        return Javalin.create(config -> {
            config.routes.get("/bikeparks", ctx -> searchParks(ctx, searchService));
            config.routes.exception(IllegalArgumentException.class, (e, ctx) ->
                    ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("error", e.getMessage())));
        });
    }

    private static void searchParks(Context ctx, SearchService searchService) {
        Location destination = new Location(
                requireNumber(ctx, "latitude"),
                requireNumber(ctx, "longitude"));
        List<SearchResult> results = searchService.searchParks(destination, requireNumber(ctx, "radius"));
        ctx.json(results.stream().map(BikeParkService::toSummary).toList());
    }

    private static Map<String, Object> toSummary(SearchResult result) {
        BikePark park = result.getBikePark();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", park.getId());
        summary.put("name", park.getName());
        summary.put("location", park.getLocation());
        summary.put("distanceMetres", result.getDistanceMetres());
        return summary;
    }

    private static double requireNumber(Context ctx, String name) {
        String value = ctx.queryParam(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing query parameter: " + name);
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Query parameter " + name + " must be a number: " + value);
        }
    }
}
