package hotelapp;

import com.google.gson.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is for storing hotel data from hotelJson file.
 */

public class Hotel{

    private String id;
    private String f;
    private String ad;
    private String ci;
    private String pr;
    private String c;
    private Position ll;

    private String link = "https://www.expedia.com/";
    private List<Review> reviews;
    private HashSet<String> reviewIds;

    public Hotel(String id, String f, String ad, String ci, String pr, String c,Position ll) {
        this.id = id;
        this.f = f;
        this.ad = ad;
        this.ci = ci;
        this.pr = pr;
        this.c = c;
        this.ll = ll;
    }
    public String getLink(){
        String city= ci.replaceAll(" ","-");
        return link + city +"-Hotels.h" + id +".Hotel-Information";
    }
    public int getAverageScore(){
        int averageScore = 0;
        int total = 0;
        int count = 0;
        if(reviews == null)
            return 0;
        for(Review review: reviews){
            count++;

            total += review.getRatingOverall();
        }
        averageScore = total / count;
        return averageScore;
    }

    @Override
    public String toString() {
        String print = "";
        print = print + "********************" + '\n' +
                f + ": " + id +'\n' +
                ad + '\n' +
                ci + ", " + pr;
        return print;
    }

    public String getId() {
        return id;
    }

    public String getF() {
        return f;
    }

    public String getAd() {
        return ad;
    }

    public String getCi() {
        return ci;
    }

    public String getPr() {
        return pr;
    }

    public String getC() {
        return c;
    }

    public List<Review> getReviews() {
//        List<Review> newList = new LinkedList<>();
//        for(Review r: reviews){
//            newList.add(r);
//        }
        return reviews;
    }
    public Position getPosition(){
        return ll;
    }
    public String getFullAddress(){
        return getAd()+" ," + getCi() + " ,"+ getPr();
    }

    public void addReview(Review r) {
        if (this.reviews == null) {
            this.reviews = new LinkedList<>();
        }
        if(reviewIds == null)
            reviewIds = new HashSet<>();
        //  keep in order of newest first
        if (this.reviewIds.contains(r.getReviewId())) {
            return;
        }
        this.reviews.add(r);
        reviewIds.add(r.getReviewId());
    }


//    public void sortReviews() {
//        if (reviews != null) {
//            //System.out.println("This review: " + reviews);
//            Collections.sort(reviews);
//        }
//    }

    public void printReviews() {
        for (Review r : this.reviews) {
            System.out.println(r);
        }
    }
    public String reviewsToString(){
        if (this.reviews == null)
            return "";
        StringBuilder reviews = new StringBuilder();
        for (Review r : this.reviews) {
            reviews.append(r.toString() + '\n');
        }
        return reviews.toString();

    }
// change the method name
    public String hotelToJson(){
        JsonObject jsonObject = new JsonObject();
        String jsonInString = "";
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("hotelId",getId());
            jsonObject.addProperty("name", getF());
            jsonObject.addProperty("addr", getAd());
            jsonObject.addProperty("city", getCi());
            jsonObject.addProperty("state", getPr());
            jsonObject.addProperty("lat",getPosition().getLatitude());
            jsonObject.addProperty("lng",getPosition().getLongitude());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = gson.toJsonTree(jsonObject);
        jsonInString = gson.toJson(jsonElement);
        return jsonInString;
    }


    public String newHotelToJson(String num){

        JsonObject jsonObject = new JsonObject();
        String jsonInString = "";

        jsonObject.addProperty("name", getF());
        jsonObject.addProperty("addr", getAd());

        JsonArray reviews = reviewsToJson(num);
        jsonObject.add("reviews",reviews);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = gson.toJsonTree(jsonObject);
        jsonInString = gson.toJson(jsonElement);
        return jsonInString;

    }
    public JsonArray reviewsToJson(String num){
        int end =0;
        if(num == null)
            end = reviews.size();
        else
            end = Integer.parseInt(num);

        JsonArray reviewsArray = new JsonArray();
        for(Review r: reviews.subList(0,end)){
            reviewsArray.add(r.reviewToJson());
        }
        return reviewsArray;
    }

}
