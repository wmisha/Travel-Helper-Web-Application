package server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hotelapp.Hotel;
import hotelapp.Review;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

public class LoadJsonToTables {

    protected static final DatabaseHandler dbHandler = DatabaseHandler.getInstance();
    private Gson gson;
    private String hotelsFile;
    private String reviewsDir;

    public LoadJsonToTables(String hotelsFile, String reviewsDir) {
        this.gson = new Gson();
        this.hotelsFile = hotelsFile;
        this.reviewsDir = reviewsDir;
    }

    public  void parseHotels() {

        try (FileReader br = new FileReader(hotelsFile)) {

            String fileData = new String(Files.readAllBytes(Paths.get(hotelsFile)));
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(fileData);

            JsonArray jsonArr = jo.getAsJsonArray("sr"); // array of Json

            Hotel[] hotels = gson.fromJson(jsonArr, Hotel[].class);

            for (Hotel h : hotels) {
                dbHandler.insertValueToHotels(h.getId(),h.getF(),h.getFullAddress(),h.getCi(),
                        h.getPosition().getLatitude(),h.getPosition().getLongitude(),h.computeExpediaLink());
            }
        } catch (IOException e) {
            System.out.println("Could not read the file: " + e);
        }
    }
   public Path getPath(){
        return Paths.get(reviewsDir);
   }
    public void traverseReviews(Path path) {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    traverseReviews(entry);
                } else {
                   //System.out.println("File processing: " + entry.toString());
                    parseReview(entry.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void parseReview(String filePath) {
        Review[] reviews;

        try (FileReader br = new FileReader(filePath)) {

            String fileData = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(fileData);
            JsonArray jsonArr = jo.getAsJsonObject("reviewDetails")
                    .getAsJsonObject("reviewCollection")
                    .getAsJsonArray("review");

            reviews = gson.fromJson(jsonArr, Review[].class);
            for (Review r : reviews) {
                 dbHandler.insertValuesToReviews(r.getReviewId(), r.getHotelId(), r.getRatingOverall(),
                        r.getTitle(), r.getReviewText(), r.getUserNickname(), r.getDate(), r.getUserId());
            }
        } catch (IOException e) {
            System.out.println("Could not read the file: " + e);
        }

    }
    public static void main(String[] args){
        LoadJsonToTables loadJsonToTables = new LoadJsonToTables("input/hotels.json","input/reviews");
        Path path = loadJsonToTables.getPath();
        loadJsonToTables.parseHotels();
        loadJsonToTables.traverseReviews(path);


    }


}

