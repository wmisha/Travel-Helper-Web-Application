package hotelapp;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.*;

public class ParseFiles {

    private String hotelsFile;
    private String reviewDir;
    protected HotelDatabase db;
    private Gson gson;

    public void PassFile(){
        gson = new Gson();

    }
    public void parseData(String hotelsFile, String reviewsDir, HotelDatabase db){
        this.db = db;
        // Parse hotel and reviews files
        parseHotels(hotelsFile);

        try {
            traverseReviews(Paths.get(reviewsDir));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method is for load data from hotel Json file by using JsonParser.
     *
     * @param filePath
     */

    protected void parseHotels(String filePath) {

        try (FileReader br = new FileReader(filePath)) {

            String fileData = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(fileData);

            JsonArray jsonArr = jo.getAsJsonArray("sr"); // array of Json

            Hotel[] hotels = gson.fromJson(jsonArr, Hotel[].class);
            for (Hotel h : hotels) {
                db.hotelMap.put(h.getId(), h);
            }
        } catch (IOException e) {
            System.out.println("Could not read the file: " + e);
        }
        System.out.println("finish parsing hotel");
    }
    /**
     * This method is for recursively traversing this directory to find all json files with reviews folder.
     *
     * @param path
     * @throws IOException
     */
    protected void traverseReviews(Path path) throws IOException {

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    traverseReviews(entry);
                } else {
                    parseReview(entry.toString());
                }
            }
        }
        System.out.println("finish parsing review............");
    }

    /**
     * This method does few different work.
     * first it reads data from one specific Joson file, then put it into array of review object for each hotel;
     * second, count the number of times for every word in title and review text in each Review object, then
     * each word as a key, the counts as value, store them in a hashMap.
     * third, after putting each word and frenquncy in HashMap wordCounts, then we need to check whether each word we have found in review
     * object exists in wordMap object or not.If it isn't exist yet, we need put word as key into it and create a new linkedList of
     * WordMapEntry object, then create WordMapEntry object and add it to LinkedLink in wordMap.
     *
     * @param filePath
     */
    protected void parseReview(String filePath) {
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
                //System.out.println(r.getHotelId());
                if (db.hotelMap.containsKey(r.getHotelId()))
                    db.hotelMap.get(r.getHotelId()).addReview(r);
            }
        } catch (IOException e) {
            System.out.println("Could not read the file: " + e);
        }
    }



}
