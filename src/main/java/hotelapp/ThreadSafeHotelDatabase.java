package hotelapp;


import com.google.gson.*;
import customLock.ReentrantReadWriteLock;


import java.time.OffsetDateTime;
import java.util.*;

public class ThreadSafeHotelDatabase extends HotelDatabase{

    private ReentrantReadWriteLock lock;
    private Gson gson;


    public ThreadSafeHotelDatabase() {
        super();
        lock =  new ReentrantReadWriteLock();
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public TreeMap<String,Hotel> getHotelMap(){
        TreeMap<String,Hotel> newMap = new TreeMap<>();
        newMap.putAll(hotelMap);
        return newMap;
    }

    @Override
    public void sortData(){
        long start;

        start = System.currentTimeMillis();
        putDataInWordMap();
        //System.out.println("Stage 3 (putDataInWordMap): " + (System.currentTimeMillis()-start) + " ms");

        start = System.currentTimeMillis();
        for (Hotel hotel : hotelMap.values()) {
            hotel.sortReviews();
        }
        //System.out.println("Stage 4 (sortReviews): " + (System.currentTimeMillis()-start) + " ms");

        start = System.currentTimeMillis();
        for (ArrayList<HotelDatabase.WordMapEntry> list : wordMap.values()) {
            Collections.sort(list);
        }
        //System.out.println("Stage 5 (sort wordMap): " + (System.currentTimeMillis()-start) + " ms");
    }

    @Override
    protected void putDataInWordMap(){
        Collection<Hotel> hotels = hotelMap.values();
        for(Hotel hotel: hotels) {
            List<Review> reviews = hotel.getReviews();
            if (reviews == null) {
                continue;
            }
            for (Review r : reviews) {
                // Add all the words from the title and review text to our dictionary
                // How to get all the words in a string
                StringTokenizer st = new StringTokenizer(r.getTitle() + " " + r.getReviewText(), " \t\n\r\f,.:;?![]'");

                HashMap<String, Integer> wordCounts = new HashMap<>();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().toLowerCase();
                    int count = wordCounts.containsKey(token) ? wordCounts.get(token) : 0;
                    wordCounts.put(token, count + 1);
                }

                OffsetDateTime reviewTime = OffsetDateTime.parse(r.getReviewSubmissionTime().substring(0, 19) + "Z");
                try {
                    lock.lockWrite();
                    lock.lockRead();
                    wordCounts.forEach((word, count) -> {
                        //System.out.println("Word '" + word + "' was found " + count + " times");
                        if (!wordMap.containsKey(word))
                            wordMap.put(word, new ArrayList<>());
                        wordMap.get(word).add(new WordMapEntry(r, count, reviewTime));
                    });
                } finally {
                    lock.unlockRead();
                    lock.unlockWrite();
                }

            }
        }
    }


    public void find(String arg) {
        try {
            Hotel hotel;
            try{
                lock.lockRead();
                hotel = hotelMap.get(arg);
            }
            finally {
                lock.unlockRead();
            }
            if (hotel == null) {
                System.out.println("This hotel does not exist.");
                return;
            }
            System.out.println(hotel);
        } catch (NumberFormatException n) {
            System.out.println("find(): Hotel ID must be an integer!");
        }
    }

    public void findReviews(String arg) {
        try {
            Hotel hotel;
            try{
                lock.lockRead();
                hotel = hotelMap.get(arg);
            }
            finally {
                lock.unlockRead();
            }
            if (hotel == null) {
                System.out.println("This hotel does not exist.");
                return;
            }
            hotel.printReviews();
        } catch (NumberFormatException n) {
            System.out.println("find(): Hotel ID must be an integer!");
        }
    }

    public void findWord(String arg) {
        ArrayList<WordMapEntry> matches;
        try{
            lock.lockRead();
            if (!wordMap.containsKey(arg)) {
                System.out.println("The word '" + arg + "' could not be found in any review.");
                return;
            }
            matches = wordMap.get(arg);
        }
        finally{
            lock.unlockRead();
        }
        for (WordMapEntry r : matches) {
            System.out.println(r.frequency + " times");
            System.out.println(r.review);
        }
    }

    public String searchHotelByCityAndKeyword(String city,String keyword){
        JsonObject jsonObject = new JsonObject();
        String jsonInString = "";


        return jsonInString;

    }

    public String putSuggestionHotelsInJson(String city, String keyword){
            ArrayList<HotelMapEntry> entries = getHotelsByCityAndKeyWord(city,keyword);
            JsonObject jsonObject = new JsonObject();
            String jsonInString = "";
        JsonArray entriesArray = new JsonArray();
        for(HotelMapEntry entry: entries){
            entriesArray.add(entry.putHotelMapEntryInJson());
        }
        jsonObject.add("reviews",entriesArray);
        return (gson.toJson(gson.toJsonTree(jsonObject)));
    }

    public String hotelInfo(String arg){
        JsonObject jsonObject = new JsonObject();
        String jsonInString = "";
        try {
            Hotel hotel;
            try{
                lock.lockRead();
                hotel = hotelMap.get(arg);
            }
            finally {
                lock.unlockRead();
            }

            if(hotel != null){
                return hotel.hotelToJson();
            }
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("hotelId", "invalid");
            JsonElement jsonElement = gson.toJsonTree(jsonObject);
            jsonInString = gson.toJson(jsonElement);
           // System.out.println(jsonInString);

        } catch (NumberFormatException n) {
            System.out.println("find(): Hotel ID must be an integer!");
        }
        return jsonInString;

    }
    public String reviews(String arg,String num){
       // int number = Integer.parseInt(num);
        JsonObject jsonObject = new JsonObject();
        try {
            Hotel hotel;
            try{
                lock.lockRead();
                hotel = hotelMap.get(arg);
            }
            finally {
                lock.unlockRead();
            }
            if(hotel == null){
                jsonObject.addProperty("success", Boolean.FALSE);
                jsonObject.addProperty("hotelId", "invalid");
            }else {
                JsonArray reviews = hotel.reviewsToJson(num);
                jsonObject.addProperty("success", Boolean.TRUE);
                jsonObject.addProperty("hotelId", hotel.getId());
                jsonObject.add("reviews",reviews);
            }
            return (gson.toJson(gson.toJsonTree(jsonObject)));
        } catch (NumberFormatException n) {
            System.out.println("find(): Hotel ID must be an integer!");
            return "find(): Hotel ID must be an integer!";
        }
    }
    public String index(String arg,String num){
        JsonObject jsonObject = new JsonObject();
        try{
            lock.lockRead();
            if (!wordMap.containsKey(arg)) {
                System.out.println( arg + "' could not be found in any review.");
                return null;
            }
        }
        finally{
            lock.unlockRead();
        }
        jsonObject.addProperty("success",Boolean.TRUE);
        jsonObject.addProperty("word",arg);
        jsonObject.add("reviews", super.parseReviewsToJsonArray(arg,num));
        return (gson.toJson(gson.toJsonTree(jsonObject)));

//        for (WordMapEntry r : matches) {
//            System.out.println(r.frequency + " times");
//            System.out.println(r.review);
//        }
    }

}
