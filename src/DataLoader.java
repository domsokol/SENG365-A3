import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads bike park seed data from a local JSON file containing an array of objects of the form
 * {@code {"id": ..., "name": ..., "location": {"latitude": ..., "longitude": ...}}}.
 */
public final class DataLoader {

    /**
     * Loads every bike park in the file into a new repository. If any entry is invalid,
     * no repository is created and an {@link InitializationException} describes the problem.
     */
    public BikeParkRepository loadBikeParks(Path file) {
        JsonElement root;
        try {
            root = JsonParser.parseString(Files.readString(file));
        } catch (IOException e) {
            throw new InitializationException("Could not read bike park data from " + file, e);
        } catch (JsonParseException e) {
            throw new InitializationException("Malformed JSON in " + file + ": " + e.getMessage(), e);
        }

        if (!root.isJsonArray()) {
            throw new InitializationException("Expected a JSON array of bike parks in " + file);
        }

        JsonArray entries = root.getAsJsonArray();
        List<BikePark> parks = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            parks.add(parseBikePark(entries.get(i), i));
        }

        try {
            return new BikeParkRepository(parks);
        } catch (IllegalArgumentException e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }

    private BikePark parseBikePark(JsonElement element, int index) {
        String where = "Bike park at index " + index;
        try {
            JsonObject park = requireObject(element, where);
            JsonObject location = requireObject(park.get("location"), where + " field \"location\"");
            return new BikePark(
                    requireString(park, "id"),
                    requireString(park, "name"),
                    new Location(requireNumber(location, "latitude"), requireNumber(location, "longitude")));
        } catch (IllegalArgumentException e) {
            throw new InitializationException(where + ": " + e.getMessage(), e);
        }
    }

    private static JsonObject requireObject(JsonElement element, String description) {
        if (element == null || !element.isJsonObject()) {
            throw new InitializationException(description + " must be a JSON object");
        }
        return element.getAsJsonObject();
    }

    private static String requireString(JsonObject object, String field) {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("field \"" + field + "\" must be a string");
        }
        return value.getAsString().trim();
    }

    private static double requireNumber(JsonObject object, String field) {
        JsonElement value = object.get(field);
        if (value == null || !value.isJsonPrimitive() || !((JsonPrimitive) value).isNumber()) {
            throw new IllegalArgumentException("field \"" + field + "\" must be a number");
        }
        return value.getAsDouble();
    }
}
