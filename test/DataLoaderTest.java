import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataLoaderTest {

    private static final Path PROVIDED_DATA = Path.of("data", "bikeparks.json");

    @TempDir
    Path tempDir;

    private final DataLoader loader = new DataLoader();

    private Path writeData(String contents) throws IOException {
        Path file = tempDir.resolve("bikeparks.json");
        Files.writeString(file, contents);
        return file;
    }

    @Test
    void loadsProvidedDataFile() {
        BikeParkRepository repository = loader.loadBikeParks(PROVIDED_DATA);

        assertEquals(5, repository.findAll().size());
        BikePark library = repository.findById("bp-001").orElseThrow();
        assertEquals("Library Bike Shelter", library.getName());
        assertEquals(-43.5235, library.getLocation().getLatitude());
        assertEquals(172.5826, library.getLocation().getLongitude());
        assertTrue(library.getReviews().isEmpty());
    }

    @Test
    void unknownIdIsNotFound() {
        BikeParkRepository repository = loader.loadBikeParks(PROVIDED_DATA);

        assertTrue(repository.findById("missing").isEmpty());
    }

    @Test
    void rejectsFileWithInvalidLatitudeAfterValidEntry() throws IOException {
        Path file = writeData("""
                [
                  {"id": "bp-001", "name": "Library Bike Shelter",
                   "location": {"latitude": -43.5235, "longitude": 172.5826}},
                  {"id": "bp-002", "name": "Bad Park",
                   "location": {"latitude": -123.0, "longitude": 172.5843}}
                ]
                """);

        assertThrows(InitializationException.class, () -> loader.loadBikeParks(file));
    }

    @Test
    void rejectsNonNumericCoordinate() throws IOException {
        Path file = writeData("""
                [{"id": "bp-001", "name": "Library Bike Shelter",
                  "location": {"latitude": "abc", "longitude": 172.5826}}]
                """);

        assertThrows(InitializationException.class, () -> loader.loadBikeParks(file));
    }

    @Test
    void rejectsMissingName() throws IOException {
        Path file = writeData("""
                [{"id": "bp-001",
                  "location": {"latitude": -43.5235, "longitude": 172.5826}}]
                """);

        assertThrows(InitializationException.class, () -> loader.loadBikeParks(file));
    }

    @Test
    void rejectsMissingLocation() throws IOException {
        Path file = writeData("""
                [{"id": "bp-001", "name": "Library Bike Shelter"}]
                """);

        assertThrows(InitializationException.class, () -> loader.loadBikeParks(file));
    }

    @Test
    void rejectsDuplicateIds() throws IOException {
        Path file = writeData("""
                [
                  {"id": "bp-001", "name": "Library Bike Shelter",
                   "location": {"latitude": -43.5235, "longitude": 172.5826}},
                  {"id": "bp-001", "name": "Engineering Core Racks",
                   "location": {"latitude": -43.5218, "longitude": 172.5843}}
                ]
                """);

        assertThrows(InitializationException.class, () -> loader.loadBikeParks(file));
    }

    @Test
    void rejectsMalformedJson() throws IOException {
        Path file = writeData("[{\"id\": \"bp-001\", ");

        assertThrows(InitializationException.class, () -> loader.loadBikeParks(file));
    }

    @Test
    void rejectsNonArrayRoot() throws IOException {
        Path file = writeData("{\"id\": \"bp-001\"}");

        assertThrows(InitializationException.class, () -> loader.loadBikeParks(file));
    }

    @Test
    void rejectsMissingFile() {
        assertThrows(InitializationException.class,
                () -> loader.loadBikeParks(tempDir.resolve("does-not-exist.json")));
    }
}
