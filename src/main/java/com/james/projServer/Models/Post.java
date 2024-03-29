package com.james.projServer.Models;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class Post {

    Integer post_id;
    LocalDateTime post_date;
    String title;
    String review;
    Integer rating;
    Integer votes;
    String nonRegRestName;
    String googRestName;
    String restId;
    Integer userId;
    String username;
    List<String> photoURLs = new LinkedList<>();
    Integer viewerVote;

    public void populate(SqlRowSet rs, SqlRowSet photoRs) {

        setPost_id(rs.getInt("post_id"));
        setPost_date(rs.getTimestamp("post_date").toLocalDateTime());
        setTitle(rs.getString("title"));
        setReview(rs.getString("review"));
        setRating(rs.getInt("rating"));
        setVotes(rs.getInt("votes"));
        setNonRegRestName(rs.getString("nonreg_rest_name"));
        setGoogRestName(rs.getString("name"));
        setRestId(rs.getString("rest_id"));
        setUserId(rs.getInt("user_id"));
        setUsername(rs.getString("username"));
        try {
            setViewerVote(rs.getInt("vote"));
            System.out.println("viewer votes" + viewerVote);
        } catch (Exception e) {
        }

        while (photoRs.next()) {
            photoURLs.add(photoRs.getString("photo_url"));
        }
    }

    public void populateWithoutPhotos(SqlRowSet rs) {
        setPost_id(rs.getInt("post_id"));
        setPost_date(rs.getTimestamp("post_date").toLocalDateTime());
        setTitle(rs.getString("title"));
        setReview(rs.getString("review"));
        setRating(rs.getInt("rating"));
        setVotes(rs.getInt("votes"));
        setNonRegRestName(rs.getString("nonreg_rest_name"));
        setGoogRestName(rs.getString("name"));
        setRestId(rs.getString("rest_id"));
        setUserId(rs.getInt("user_id"));
        setUsername(rs.getString("username"));
        try {
            setViewerVote(rs.getInt("vote"));
            System.out.println("viewer votes" + viewerVote);
        } catch (Exception e) {
        }
    }

    public void addPhotoUrl(SqlRowSet rs) {
        String url = rs.getString("photo_url");
        if (url != null) {
            photoURLs.add(rs.getString("photo_url"));
        }
    }

    public JsonObject toJSON() {

        JsonArrayBuilder ab = Json.createArrayBuilder();
        for (String photoURL : photoURLs) {
            ab.add(photoURL);
        }
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("post_id", post_id)
                .add("date", post_date.toString())
                .add("title", title)
                .add("review", review)
                .add("rating", rating)
                .add("votes", votes)
                // .add("nonRegRestName",nonRegRestName)
                // .add("restId",restId)
                .add("user_id", userId)
                .add("urls", ab)
                .add("username",username);

        System.out.println("json viewer votes" + viewerVote);
        if (viewerVote != null) {
            builder = builder.add("viewerVote", viewerVote);
        }
        if (nonRegRestName != null) {
            builder = builder.add("nonRegRestName", nonRegRestName);
        }
        if (googRestName != null) {
            builder = builder.add("googRestName", googRestName);
        }

        JsonObject result = builder.build();

        return result;
    }

    public Integer getViewerVote() {
        return viewerVote;
    }

    public void setViewerVote(Integer viewerVote) {
        this.viewerVote = viewerVote;
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

    public String getGoogRestName() {
        return googRestName;
    }

    public void setGoogRestName(String googRestName) {
        this.googRestName = googRestName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    

}
