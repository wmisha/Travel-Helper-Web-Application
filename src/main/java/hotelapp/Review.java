package hotelapp;

import com.google.gson.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is for storing review data from reviews folder
 */

public class Review implements Comparable<Review> {

    private String  hotelId;
    private String reviewId;
    private int ratingOverall;
    private String title;
    private String reviewText;
    private String reviewSubmissionTime;
    private String userNickname;

    public Review(String hotelId, String reviewId, int ratingOverall,
                  String title, String reviewText, String reviewSubmissionTime,String userNickname) {
        this.hotelId = hotelId;
        this.reviewId = reviewId;
        this.ratingOverall = ratingOverall;
        this.title = title;
        this.reviewText = reviewText;
        this.reviewSubmissionTime = reviewSubmissionTime.substring(0,10);
        this.userNickname = userNickname;

    }
    public int getRatingOverall(){
        return ratingOverall;
    }

    public String getReviewSubmissionTime() {
        return reviewSubmissionTime;
    }

    public String getHotelId() {
        return hotelId;
    }

    public String getTitle() {
        return title;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getReviewId() {
        return reviewId;
    }
    public String getUserNickname(){
        if(this.userNickname.equals(""))
            return "Anonymous";
        return userNickname;
    }
    public LocalDate getDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        // System.out.println("submission time: " + reviewSubmissionTime);
        return LocalDate.parse(reviewSubmissionTime.substring(0,10), formatter);
    }

    @Override
    public String toString() {
        return  "--------------------" + "\n" +
                "Review by " + getUserNickname() + " on " + getDate() + "\n" +
                "Rating: " + ratingOverall + "\n" +
                "ReviewId: " + reviewId + "\n" +
                title + "\n" +
                reviewText;
    }


    public JsonObject reviewToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("reviewId", getReviewId());
        jsonObject.addProperty("title", getTitle());
        jsonObject.addProperty("reviewText", getReviewText());
        jsonObject.addProperty("user", getUserNickname());
        jsonObject.addProperty("date", getDate().toString());
        jsonObject.addProperty("rating", getRatingOverall());
        return jsonObject;
    }

    @Override
    public int compareTo(Review o) {
//        OffsetDateTime date = OffsetDateTime.parse(this.reviewSubmissionTime);
//        OffsetDateTime dateOther = OffsetDateTime.parse(o.reviewSubmissionTime);

        // Using negative of compareTo in order to get newest first
       // reviewSubmissionTime = reviewSubmissionTime.substring(0,10);
        LocalDate date = getDate();

        //System.out.println("Date: " + date);
        int result = -date.compareTo(o.getDate());
        if (result != 0) {
            return result;
        }

        // Exact same time? sort using review ID string in ascending string order
        return this.getReviewId().compareTo(o.getReviewId());
        // reviews must be sorted by date (most recent one first),
        // and if dates are the same, by review id (in increasing order of review id).
    }
}
