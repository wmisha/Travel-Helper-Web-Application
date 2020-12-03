package hotelapp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import customLock.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadSafeParseFiles extends ParseFiles {
    private ReentrantReadWriteLock lock;
    private ExecutorService executor;
   // private static Logger logger = LogManager.getLogger();

    private Gson gson;

    public ThreadSafeParseFiles(int numOfThreads){
        super();
        lock = new ReentrantReadWriteLock();
        executor = Executors.newFixedThreadPool(numOfThreads);
        gson = new Gson();
    }

    @Override
    public void parseData(String hotelsFile, String reviewsDir, HotelDatabase db){
        this.db = db;
        long start;
        start = System.currentTimeMillis();
        // Parse hotel and reviews files
        parseHotels(hotelsFile);
        //System.out.println("Stage 1 (parseHotels): " + (System.currentTimeMillis()-start) + " ms");

        start = System.currentTimeMillis();
        if(reviewsDir != null) {
            try {
                traverseReviews(Paths.get(reviewsDir));
                shutdownExecutor();
              //  logger.debug("I finish load all data to HotelMap's review list");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * This method is for load data from hotel Json file by using JsonParser.
     *
     * @param filePath
     */
    @Override
    protected void parseHotels(String filePath) {
        // System.out.println("I am in ThreadSafe parseHotels");

        System.out.println("I am here now: " + filePath);
        try (FileReader br = new FileReader(filePath)) {

            String fileData = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(fileData);

            JsonArray jsonArr = jo.getAsJsonArray("sr"); // array of Json
            Hotel[] hotels = gson.fromJson(jsonArr, Hotel[].class);
            for (Hotel h : hotels) {
                try{
                    lock.lockWrite();
                    db.hotelMap.put(h.getId(), h);
                }
                finally {
                    lock.unlockWrite();
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read the file: " + e);
        }
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
        //  System.out.println("I am in ThreadSafe method parseReview");
        try (FileReader br = new FileReader(filePath)) {

            String fileData = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(fileData);
            JsonArray jsonArr = jo.getAsJsonObject("reviewDetails")
                    .getAsJsonObject("reviewCollection")
                    .getAsJsonArray("review");

            Review[] reviews = gson.fromJson(jsonArr, Review[].class);
            for (Review r : reviews) {
                //System.out.println(r.getHotelId());
                try {
                    lock.lockWrite();
                    lock.lockRead();
                    if (db.hotelMap.containsKey(r.getHotelId()))
                        db.hotelMap.get(r.getHotelId()).addReview(r); //??????????

                } finally {
                    lock.unlockRead();
                    lock.unlockWrite();
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read the file: " + e);
        }
    }

    /**
     * This method is for recursively traversing this directory to find all json files with reviews folder.
     * @param path
     * @throws IOException
     */
    protected void traverseReviews(Path path) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for(Path entry : stream){
                if (Files.isDirectory(entry)) {
                    traverseReviews(entry);
                }else{
               //     logger.debug("Create a new runnable task " + entry.toString());
                    executor.submit(new ReviewFileWorker(entry.toString()));
                    // lode data to reviewMap concurrently!!!
                }
            }
        }
    }
    private void combineHotelData(TreeMap<String,Hotel> localHotel){
        try{
            lock.lockWrite();
            db.hotelMap.putAll(localHotel);
        }
        finally {
            lock.unlockWrite();
        }
    }

    private class ReviewFileWorker implements Runnable{
        String filePath;
        TreeMap<String, Hotel> localHotel;


        public ReviewFileWorker(String filePath) {
            this.filePath = filePath;
            localHotel = new TreeMap<>();
            localHotel.putAll(db.hotelMap);
        }

        @Override
        public void run() {

            try (FileReader br = new FileReader(filePath)) {
                String fileData = new String(Files.readAllBytes(Paths.get(filePath)));
                JsonParser parser = new JsonParser();
                JsonObject jo = (JsonObject) parser.parse(fileData);
                JsonArray jsonArr = jo.getAsJsonObject("reviewDetails")
                        .getAsJsonObject("reviewCollection")
                        .getAsJsonArray("review");

                Review[] reviews = gson.fromJson(jsonArr, Review[].class);
                for (Review r : reviews) {
                    if (localHotel.containsKey(r.getHotelId()))
                        localHotel.get(r.getHotelId()).addReview(r);
                }
            } catch (IOException e) {
                System.out.println("Could not read the file: " + e);
            }

            combineHotelData(localHotel);
          //  logger.debug("this runnable task is done: " + filePath);

        }

    }
    public synchronized void shutdownExecutor(){
        executor.shutdown();
        try {
            executor.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
