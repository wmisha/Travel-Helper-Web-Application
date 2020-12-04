package hotelapp;

import com.google.gson.*;
import org.eclipse.jetty.util.HostMap;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Parsing Json files and prove find(), findReviews(), findWord() mothods.
 */
public class HotelDatabase {
    /**
     * This is helper class for findWord(), This class stores review Object, frequency for given
     * word and the submitTime for later sorting if frequency is the same.
     */
    protected class WordMapEntry implements Comparable<WordMapEntry> {
        Review review;
        Integer frequency;
        OffsetDateTime reviewTime; //used for Sorting

        public WordMapEntry(Review review, Integer frequency, OffsetDateTime reviewTime) {
            this.review = review;
            this.frequency = frequency;
            this.reviewTime = reviewTime;
        }

        public Review getReview(){
            return review;
        }
        @Override
        public int compareTo(WordMapEntry o) {
            int result = o.frequency - this.frequency;

            // Exact same frequency? sort using submitTime
            if (result != 0)
                return result;
            // Using negative of compareTo in order to get newest first
            OffsetDateTime date = this.reviewTime;
            OffsetDateTime dateOther = o.reviewTime;
            return -date.compareTo(dateOther);
        }
    }
    protected class HotelMapEntry implements Comparable<HotelMapEntry>{
        private String hotelId;
        private String hotelName;
        private int averageRating;

        public HotelMapEntry(String hotelId, String hotelName, int averageRating) {
            this.hotelId = hotelId;
            this.hotelName = hotelName;
            this.averageRating = averageRating;
        }

        public int getAverageRating(){
            return averageRating;
        }

        public String getHotelId() {
            return hotelId;
        }

        public String getHotelName() {
            return hotelName;
        }

        @Override
        public int compareTo(HotelMapEntry o) {
            return this.averageRating - o.averageRating;
        }

        public String putHotelMapEntryInJson(){
            JsonObject jsonObject = new JsonObject();
            String jsonInString = "";

            jsonObject.addProperty("hotelId",getHotelId());
            jsonObject.addProperty("name", getHotelName());
            jsonObject.addProperty("rating", getAverageRating());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = gson.toJsonTree(jsonObject);
            jsonInString = gson.toJson(jsonElement);
            return jsonInString;
        }
    }

    // ***have to make private
     protected TreeMap<String,Hotel>  hotelMap;
     protected HashMap<String, ArrayList<WordMapEntry>> wordMap;
     protected HashMap<String, ArrayList<HotelMapEntry>> cityHotelMap;


    public HotelDatabase() {
        hotelMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        wordMap = new HashMap<>();
        cityHotelMap = new HostMap<>();

    }

   public JsonArray parseReviewsToJsonArray(String arg,String num){
        int end =0;
       JsonArray jsonArray = new JsonArray();
       ArrayList<WordMapEntry> entries = wordMap.get(arg);
       if(num == null)
           end = entries.size();
       else
           end = Integer.parseInt(num);
       for(WordMapEntry entry: entries.subList(0,end)){
           jsonArray.add(entry.review.reviewToJson());
       }
       return jsonArray;
   }
    public void sortData(){
        for (Hotel hotel : hotelMap.values()) {
            hotel.sortReviews();
        }
        putDataInWordMap();
        for (ArrayList<HotelDatabase.WordMapEntry> list : wordMap.values()) {
            Collections.sort(list);
        }
        putDataInCityHotelMap();
        for (ArrayList<HotelDatabase.HotelMapEntry> list : cityHotelMap.values()) {
            Collections.sort(list);
        }
    }
    protected void putDataInCityHotelMap(){
        Collection<Hotel> hotels = hotelMap.values();
        for(Hotel hotel: hotels){
            String city = hotel.getCi();
            HotelMapEntry hotelMapEntry = new HotelMapEntry(hotel.getId(),hotel.getF(),hotel.getAverageScore());
            if(!cityHotelMap.containsKey(city))
                cityHotelMap.put(city,new ArrayList<>());
            cityHotelMap.get(city).add(hotelMapEntry);
        }
    }
    public ArrayList<HotelMapEntry> getHotelsByCityAndKeyWord(String city, String keyword){
        ArrayList<HotelMapEntry> entries = new ArrayList<>();
        if(keyword == null)
            return cityHotelMap.get(city);
        for(HotelMapEntry hotelMapEntry: cityHotelMap.get(city)){
            if(hotelMapEntry.getHotelName().contains(keyword)){
                entries.add(hotelMapEntry);
            }
        }
        return entries;
    }




    protected void putDataInWordMap(){

        Collection<Hotel> hotels = hotelMap.values();
        for(Hotel hotel: hotels) {
            LinkedList<Review> reviews = (LinkedList<Review>) hotel.getReviews();
            if(reviews == null)
                continue;
            for (Review r : reviews) {
                // Add all the words from the title and review text to our dictionary
                // How to get all the words in a string
                StringTokenizer st = new StringTokenizer(r.getTitle() + " " + r.getReviewText(), " \t\n\r\f,.:;?![]'");

                HashMap<String, Integer> wordCounts = new HashMap<>();
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().toLowerCase();
                    //System.out.println("Found token: " + token);
                    int count = wordCounts.containsKey(token) ? wordCounts.get(token) : 0;
                    wordCounts.put(token, count + 1);
                }
                // System.out.println(wordCounts);

                OffsetDateTime reviewTime = OffsetDateTime.parse(r.getReviewSubmissionTime().substring(0, 19) + "Z");
                wordCounts.forEach((word, count) -> {
                    //System.out.println("Word '" + word + "' was found " + count + " times");
                    if (!wordMap.containsKey(word))
                        wordMap.put(word, new ArrayList<>());

                    wordMap.get(word).add(new WordMapEntry(r, count, reviewTime));
                });
            }
        }


    }



    public void find(String arg) {
        try {
            Hotel hotel = hotelMap.get(arg);
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
            Hotel hotel = hotelMap.get(arg);
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
        if (!wordMap.containsKey(arg)) {
            System.out.println("The word '" + arg + "' could not be found in any review.");
            return;
        }
        ArrayList<WordMapEntry> matches = wordMap.get(arg);
        for (WordMapEntry r : matches) {
            System.out.println(r.frequency);
            System.out.println(r.review);
        }
    }
}

