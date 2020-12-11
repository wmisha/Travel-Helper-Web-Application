package hotelapp;

import com.google.gson.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is for storing review data from reviews folder
 */

public class Review  {

    private String  hotelId;
    private String reviewId;
    private int ratingOverall;
    private String title;
    private String reviewText;
    private String reviewSubmissionTime;
    private String userNickname;
    private String hotelName;
    private String hotelAddress;
    private String date;
    private int userId;

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


    public Review(String hotelId, int ratingOverall, String title, String reviewText,String userNickname, String reviewSubmissionTime) {
        this.hotelId = hotelId;
        this.ratingOverall = ratingOverall;
        this.title = title;
        this.reviewText = reviewText;
        this.userNickname = userNickname;
        this.reviewSubmissionTime = reviewSubmissionTime;
    }

    public Review(String hotelId,String hotelName,String title,String reviewText,String customer,String date){
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.title = title;
        this.reviewText = reviewText;
        this.userNickname = customer;
        this.date = date;
    }
    public Review(int rating,String title,String reviewText,String customer,String date){
        this.ratingOverall = rating;
        this.title = title;
        this.reviewText = reviewText;
        this.userNickname = customer;
        this.date = date;
    }
    public Review(String reviewId,int rating,String title,String reviewText,String customer,String date,int userId){
        this.reviewId = reviewId;
        this.ratingOverall = rating;
        this.title = title;
        this.reviewText = reviewText;
        this.userNickname = customer;
        this.date = date;
        this.userId = userId;
    }

    public String getHotelName(){
        return this.hotelName;
    }
    public int getUserId(){
        return -1;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public void setRatingOverall(int ratingOverall) {
        this.ratingOverall = ratingOverall;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public void setReviewSubmissionTime(String reviewSubmissionTime) {
        this.reviewSubmissionTime = reviewSubmissionTime;
    }

    public void setUserNickname(String userNickname) {
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
    public String getDate(){
      return this.date;
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

//    @Override
//    public int compareTo(Review o) {
////        OffsetDateTime date = OffsetDateTime.parse(this.reviewSubmissionTime);
////        OffsetDateTime dateOther = OffsetDateTime.parse(o.reviewSubmissionTime);
//
//        // Using negative of compareTo in order to get newest first
//       // reviewSubmissionTime = reviewSubmissionTime.substring(0,10);
//        LocalDate date = getDate();
//
//        //System.out.println("Date: " + date);
//        int result = -date.compareTo(o.getDate());
//        if (result != 0) {
//            return result;
//        }
//
//        // Exact same time? sort using review ID string in ascending string order
//        return this.getReviewId().compareTo(o.getReviewId());
//        // reviews must be sorted by date (most recent one first),
//        // and if dates are the same, by review id (in increasing order of review id).
//    }
}
