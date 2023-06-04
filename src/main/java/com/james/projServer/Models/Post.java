package com.james.projServer.Models;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public class Post {
    
    Integer post_id;
    LocalDateTime post_date;
    String title;
    String review;
    Integer rating;
    Integer votes;
    String nonRegRestName;
    String restId;
    Integer userId;
    List<String> photoURLs = new LinkedList<>();

    public void populate (SqlRowSet rs, SqlRowSet photoRs){

        setPost_id(rs.getInt("post_id"));
        setPost_date(rs.getTimestamp("post_date").toLocalDateTime());
        setTitle(rs.getString("title"));
        setReview(rs.getString("review"));
        setRating(rs.getInt("rating"));
        setVotes(rs.getInt("votes"));
        setNonRegRestName(rs.getString("nonreg_rest_name"));
        setRestId(rs.getString("rest_id"));
        setUserId(rs.getInt("user_id"));

        while(photoRs.next()){
            photoURLs.add(photoRs.getString("photo_url"));
        }
    }

    public JsonObject toJSON(){

        JsonArrayBuilder ab = Json.createArrayBuilder();
        for(String photoURL : photoURLs){
            ab.add(photoURL);
        }
        JsonObject result = Json.createObjectBuilder()
				.add("post_id",post_id)
				.add("date", post_date.toString())
				.add("title", title)
				.add("review", review)
				.add("rating", rating)
                .add("votes",votes)
                //.add("nonRegRestName",nonRegRestName)
                //.add("restId",restId)
                .add("user_id",userId)
				.add("urls", ab)
				.build();

        return result;
    }

    public Integer getPost_id() {
        return post_id;
    }
    public void setPost_id(Integer post_id) {
        this.post_id = post_id;
    }
    public LocalDateTime getPost_date() {
        return post_date;
    }
    public void setPost_date(LocalDateTime post_date) {
        this.post_date = post_date;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getReview() {
        return review;
    }
    public void setReview(String review) {
        this.review = review;
    }
    public Integer getRating() {
        return rating;
    }
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public Integer getVotes() {
        return votes;
    }
    public void setVotes(Integer votes) {
        this.votes = votes;
    }
    public String getNonRegRestName() {
        return nonRegRestName;
    }
    public void setNonRegRestName(String nonRegRestName) {
        this.nonRegRestName = nonRegRestName;
    }
    public String getRestId() {
        return restId;
    }
    public void setRestId(String restId) {
        this.restId = restId;
    }
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    public List<String> getPhotoURLs() {
        return photoURLs;
    }
    public void setPhotoURLs(List<String> photoURLs) {
        this.photoURLs = photoURLs;
    }

    

}
