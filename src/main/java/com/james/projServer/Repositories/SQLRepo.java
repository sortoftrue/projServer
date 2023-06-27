package com.james.projServer.Repositories;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.james.projServer.Models.Post;
import com.james.projServer.Services.CallGoogle;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

@Repository
public class SQLRepo {

    @Autowired
    CallGoogle googSvc;

    @Autowired
    JdbcTemplate template;

    private final String INSERT_POST = """
            insert into posts (post_date,title,review,rating,user_id,rest_id,isGoogle) values
            (?,?,?,?,?,?,?)
                """;

    private final String INSERT_POST_NOMAP = """
            insert into posts (post_date,title,review,rating,user_id,nonreg_rest_name,isGoogle) values
            (?,?,?,?,?,?,?)
                """;

    private final String INSERT_PHOTO = """
            insert into photos (photo_id, photo_url, post_id) values
            (?,?,?)
                """;

    private final String INSERT_REST = """
            insert into restaurants (gplace_id, name, rest_desc) values
            (?,?,?)
                """;

    private final String INSERT_VOTE = """
            insert into votes (post_id, user_id, vote) values (?,?,?)
            """;

    private final String UPDATE_USER_VOTE = """
            update votes set vote = ? where post_id = ? and user_id = ?
            """;

    private final String UPDATE_POST_VOTE = """
            update posts set votes = votes + ? where post_id = ?;
            """;

    private final String GET_POST = """
            SELECT *
            from posts
            left join restaurants on posts.rest_id = restaurants.rest_id
            left join users on posts.user_id = users.user_id
            where post_id = ?
                """;

    private final String GET_POST_WITH_ID = """
            select *
            from posts
            join votes on posts.post_id = votes.post_id
            left join restaurants on posts.rest_id = restaurants.rest_id
            left join users on posts.user_id = users.user_id
            where votes.user_id = ? and votes.post_id = ?
                        """;

    private final String GET_HOME_POSTS = """
            select *
            from (
            select post_id, post_date, title, review, posts.rating, votes, nonreg_rest_name, posts.rest_id,posts.user_id, gplace_id, rest_desc, name, username
            from posts
            left join restaurants on posts.rest_id = restaurants.rest_id
            left join users on posts.user_id = users.user_id
            order by votes desc
            limit ?,6
            ) as t1
            left join photos on t1.post_id = photos.post_id
                                    """;

    private final String GET_GPLACE = """
            SELECT rest_id from restaurants where gplace_id = ?;
                """;

    private final String GET_PHOTOS = """
            SELECT * from photos where post_id = ?;
                """;

    private final String GET_REST_DETAILS = """
            select *
            from (
            select post_id, post_date, title, review, posts.rating, votes, nonreg_rest_name, posts.rest_id,posts.user_id, gplace_id, rest_desc, name, username
            from restaurants
            left join posts on posts.rest_id = restaurants.rest_id
            left join users on posts.user_id = users.user_id
            where restaurants.gplace_id =?
            order by votes desc
            limit ?,6
            ) as t1
            left join photos on t1.post_id = photos.post_id
            """;

    public void createPhotos(List<String> photoUUIDs, Integer postID) {

        final String digitalOceanURL = "https://james.sgp1.digitaloceanspaces.com/";

        for (String photoUUID : photoUUIDs) {
            template.update(INSERT_PHOTO, photoUUID, digitalOceanURL + photoUUID, postID);
        }

    }

    public Integer createPostMap(String title, String review, String gplaceId, String rating, Integer userId) {
        // check if gplace is created
        int rest_id;
        Optional<Integer> restOpt = getRestId(gplaceId);
        if (restOpt.isPresent()) {
            rest_id = restOpt.get();
        } else {
            rest_id = createRest(gplaceId);
        }

        // Create GeneratedKeyHolder object
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        // To insert data, you need to pre-compile the SQL and set up the data yourself.
        int rowsAffected = template.update(conn -> {

            // Pre-compiling SQL
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_POST, Statement.RETURN_GENERATED_KEYS);

            // Set parameters
            preparedStatement.setString(1,
                    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")));
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, review);
            preparedStatement.setInt(4, Integer.parseInt(rating));
            preparedStatement.setInt(5, userId);
            preparedStatement.setInt(6, rest_id);
            preparedStatement.setInt(7, 1);

            return preparedStatement;

        }, generatedKeyHolder);

        // Get auto-incremented ID
        Integer id = generatedKeyHolder.getKey().intValue();

        System.out.println(id);

        return id;
    }

    public Integer createPostNoMap(String title, String review, String restName, String rating, Integer userId) {

        // Create GeneratedKeyHolder object
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        // To insert data, you need to pre-compile the SQL and set up the data yourself.
        int rowsAffected = template.update(conn -> {

            // Pre-compiling SQL
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_POST_NOMAP,
                    Statement.RETURN_GENERATED_KEYS);

            // Set parameters
            preparedStatement.setString(1,
                    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")));
            preparedStatement.setString(2, title);
            preparedStatement.setString(3, review);
            preparedStatement.setInt(4, Integer.parseInt(rating));
            preparedStatement.setInt(5, userId);
            preparedStatement.setString(6, restName);
            preparedStatement.setInt(7, 0);

            return preparedStatement;

        }, generatedKeyHolder);

        // Get auto-incremented ID
        Integer id = generatedKeyHolder.getKey().intValue();

        System.out.println(id);

        return id;
    }

    public Optional<Post> getPostById(Integer postId) {
        Post post = new Post();

        final SqlRowSet rs = template.queryForRowSet(GET_POST, postId);
        final SqlRowSet photoRs = template.queryForRowSet(GET_PHOTOS, postId);
        if (rs.first()) {
            post.populate(rs, photoRs);
            return Optional.of(post);
        }

        return Optional.empty();
    }

    public Optional<Post> getPostByIdWithUserId(Integer postId, Integer userId) {

        System.out.println("GETTING POST WITH POST" + postId + "USERID" + userId);
        Post post = new Post();

        final SqlRowSet rs = template.queryForRowSet(GET_POST_WITH_ID, userId, postId);
        final SqlRowSet photoRs = template.queryForRowSet(GET_PHOTOS, postId);
        if (rs.first()) {
            post.populate(rs, photoRs);
            return Optional.of(post);
        } else {
            return getPostById(postId);
        }

    }

    public Optional<Integer> getRestId(String gplace_id) {
        final SqlRowSet rs = template.queryForRowSet(GET_GPLACE, gplace_id);
        if (rs.first()) {
            System.out.println("found rest!" + rs.getInt(1));
            return Optional.of(rs.getInt(1));
        }
        System.out.println("rest not found!");
        return Optional.empty();
    }

    public Integer createRest(String gplace_id) {
        // Get gplace name
        String[] googleResp = googSvc.getGplaceName(gplace_id);
        String restName = googleResp[0];
        String summary = googleResp[1];

        // Create GeneratedKeyHolder object
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        // To insert data, you need to pre-compile the SQL and set up the data yourself.
        int rowsAffected = template.update(conn -> {

            // Pre-compiling SQL
            PreparedStatement preparedStatement = conn.prepareStatement(INSERT_REST, Statement.RETURN_GENERATED_KEYS);

            // Set parameters
            preparedStatement.setString(1, gplace_id);
            preparedStatement.setString(2, restName);
            preparedStatement.setString(3, summary);

            return preparedStatement;

        }, generatedKeyHolder);

        // Get auto-incremented ID
        Integer id = generatedKeyHolder.getKey().intValue();

        return id;
    }

    public void handleVote(Integer vote, Integer postId, Integer userId, Integer postVoteChange) {
        // check if vote entry exists
        final SqlRowSet rs = template.queryForRowSet(GET_POST_WITH_ID, userId, postId);

        if (rs.first()) {
            System.out.println("Vote found, updating vote");
            if (vote == -1) {
                template.update(UPDATE_USER_VOTE, -1, postId, userId);
            } else if (vote == 1) {
                template.update(UPDATE_USER_VOTE, 1, postId, userId);
            } else if (vote == 0) {
                template.update(UPDATE_USER_VOTE, 0, postId, userId);
            }
        } else {
            System.out.println("Vote not found, creating vote");
            // create vote with vote.
            if (vote == -1) {
                template.update(INSERT_VOTE, postId, userId, -1);
            } else if (vote == 1) {
                template.update(INSERT_VOTE, postId, userId, 1);
            }
        }

        template.update(UPDATE_POST_VOTE, postVoteChange, postId);

    }

    public List<Post> getHomePosts(Integer page) {
        List<Post> result = new ArrayList<>();
        Integer offset = (page) * 6;

        final SqlRowSet rs = template.queryForRowSet(GET_HOME_POSTS, offset);

        Integer currentPostId = null;
        Post currentPost = null;
        while (rs.next()) {
            Integer postId = rs.getInt("post_id");
            if (postId != currentPostId) {

                // append previous Created Post to Post List
                if (currentPost != null) {
                    result.add(currentPost);
                }

                // create new post
                currentPostId = postId;
                currentPost = new Post();
                currentPost.populateWithoutPhotos(rs);
                currentPost.addPhotoUrl(rs);
            }

            if (rs.isLast()) {
                result.add(currentPost);
            }
        }

        return result;
    }

    public JsonObject getRestDetails(String restId, Integer page) {
        JsonObjectBuilder blder = Json.createObjectBuilder();
        Integer offset = (page) * 6;
        System.out.println("OFFSET" + offset);

        List<Post> postList = new ArrayList<>();

        final SqlRowSet rs = template.queryForRowSet(GET_REST_DETAILS, restId, offset);

        float score = 0;
        float totalscore = 0;
        float votes = 0;
        Integer currentPostId = null;
        Post currentPost = null;
        if (rs.next()) {
            rs.previous();
            while (rs.next()) {
                Integer postId = rs.getInt("post_id");
                System.out.println("POST ID" + postId);

                try {
                    blder = blder.add("desc", rs.getString("rest_desc"));
                } catch (Exception e) {
                }

                blder = blder.add("name", rs.getString("name"));

                if (rs.isFirst() && postId == 0) {
                    System.out.println("NO POSTS, BREAKING");
                    break;
                }
                
                if (postId != currentPostId) {

                    // append previous Created Post to Post List
                    if (currentPost != null) {
                        postList.add(currentPost);
                        votes += (currentPost.getVotes()+1);
                        totalscore += (currentPost.getVotes()+1)*currentPost.getRating();
                    }

                    // create new post
                    currentPostId = postId;
                    currentPost = new Post();
                    currentPost.populateWithoutPhotos(rs);
                    currentPost.addPhotoUrl(rs);
                }

                if (rs.isLast()) {
                    System.out.println("LSAT ROW" + currentPost.getPost_id());
                    postList.add(currentPost);
                    votes += (currentPost.getVotes()+1);
                        totalscore += (currentPost.getVotes()+1)*currentPost.getRating();
                }

                if (postId == 0) {
                    System.out.println("NO POSTS, BREAKING");
                    break;
                }
            }
        } else if (offset==0){
            System.out.println("REST NOT FOUND,CREATING REST");
            createRest(restId);
            return getRestDetails(restId, offset);
        }

        score = totalscore/votes;
        System.out.println("STRING VALUE" + String.valueOf(score));

        // create Json Object
        JsonArrayBuilder arrayBlder = Json.createArrayBuilder();
        for (Post post : postList) {
            arrayBlder = arrayBlder.add(post.toJSON());
        }
        JsonArray postArray = arrayBlder.build();
        System.out.println(postArray.toString());

        blder = blder.add("postArray", postArray);
        if(String.valueOf(score).equals("NaN")){
            blder = blder.add("score","-");
        } else{
            blder = blder.add("score",String.format("%.1f",score));
        }
        

        JsonObject result = blder.build();

        System.out.println(result.toString());

        return result;
    }
}
