package com.james.projServer.Controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.james.projServer.Models.GoogleUser;
import com.james.projServer.Models.Post;
import com.james.projServer.Repositories.ImageRepository;
import com.james.projServer.Repositories.SQLRepo;
import com.james.projServer.Repositories.SQLUserRepo;
import com.james.projServer.Services.GoogleCalendar;
import com.james.projServer.Services.appToken;
import com.james.projServer.Services.VerifyGoogleToken;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

@RestController
public class UploadController {

    @Autowired
    ImageRepository imageRepo;
    @Autowired
    SQLRepo sqlRepo;
    @Autowired
    SQLUserRepo userRepo;
    @Autowired
    RedisTemplate<String, String> redis;
    @Autowired
    VerifyGoogleToken googleTokenSvc;
    @Autowired
    appToken appTokenSvc;
    @Autowired
    GoogleCalendar calendarSvc;

    @PostMapping(path = "/upload")
    public ResponseEntity<String> uploadPost(@RequestPart(required = false) MultipartFile[] images,
            @RequestPart String title, @RequestPart(required = false) String review,
            @RequestPart(required = false) String restName, @RequestPart(required = false) String restId,
            @RequestPart(required = false) String rating, @RequestPart String isGoogle, @RequestPart String userId) {

        System.out.println(title + review + restName + restId + rating + isGoogle + userId);

        // for (MultipartFile image : images) {
        // System.out.println(image.getOriginalFilename());
        // }

        // Either gplace_id or restaurant name. Check if gplace_id is in database.
        // If not, try to pull data from google.
        // If does not exist, create

        // Create post in SQL
        Integer createdPostId;
        Boolean chkGoogle = isGoogle.equals("1");
        if (chkGoogle) {
            System.out.println("creating google");
            createdPostId = sqlRepo.createPostMap(title, review, restId, rating, Integer.parseInt(userId));
        } else {
            System.out.println("creating non-google");
            createdPostId = sqlRepo.createPostNoMap(title, review, restName, rating, Integer.parseInt(userId));
        }

        // Upload to Ocean and Create photos in SQL
        if (images != null) {
            try {

                System.out.println("Uploading images");
                sqlRepo.createPhotos(imageRepo.uploadToOcean(images), createdPostId);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return null;
    }

    @GetMapping(path = "/post/{post_id}")
    public ResponseEntity<String> getPost(@PathVariable String post_id,
            @RequestHeader(value = "token", required = false) String token) {

        System.out.println("post request received!" + token);

        // check Redis if token is still valid. if valid, retrieve username from Repo
        // if not valid, erase client's token.

        if (token != null) {
            String returnedId = redis.opsForValue().get(token);

            if (returnedId != null) {
                Optional<Post> opt = sqlRepo.getPostByIdWithUserId(Integer.parseInt(post_id),
                        Integer.parseInt(returnedId));

                if (opt.isEmpty()) {
                    Optional<Post> noViewerPost = sqlRepo.getPostById(Integer.parseInt(post_id));

                    if (noViewerPost.isEmpty()) {
                        return ResponseEntity.status(404).body(
                                Json.createObjectBuilder().add("error_message", "Cannot find post").build().toString());
                    }
                    return ResponseEntity.ok().body(noViewerPost.get().toJSON().toString());
                }
                return ResponseEntity.ok().body(opt.get().toJSON().toString());
            }

            Optional<Post> opt = sqlRepo.getPostById(Integer.parseInt(post_id));

            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Json.createObjectBuilder().add("error_message", "Cannot find post").build().toString());
            }
            return ResponseEntity.ok().body(opt.get().toJSON().toString());

        } else {
            // if no token, get post without ID
            Optional<Post> opt = sqlRepo.getPostById(Integer.parseInt(post_id));

            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body(
                        Json.createObjectBuilder().add("error_message", "Cannot find post").build().toString());
            }
            return ResponseEntity.ok().body(opt.get().toJSON().toString());
        }

    }

    @PostMapping(path = "/user/login")
    public ResponseEntity<String> userLogin(@RequestHeader("user") String user,
            @RequestHeader("password") String password) {

        // check username and password in repo
        Integer retrievedId = userRepo.verifyLogin(user, password);

        if (retrievedId != 0) {
            String authToken = appTokenSvc.createAndStoreToken(retrievedId);
            return ResponseEntity.status(200).body(
                    Json.createObjectBuilder().add("userId", Integer.toString(retrievedId)).add("token", authToken)
                            .build().toString());
        }

        return ResponseEntity.status(401).body(
                Json.createObjectBuilder().add("userId", "0").add("error", "Invalid user/password").build().toString());
    }

    @PostMapping(path = "/user/create")
    public ResponseEntity<String> createUser(@RequestHeader("user") String user,
            @RequestHeader("password") String password) {

        Boolean createdId = userRepo.createUser(user, password);
        System.out.println(createdId);
        if (createdId) {
            System.out.println("Return success");
            return ResponseEntity.ok().body(Json.createObjectBuilder().add("status","user created")
                            .build().toString());
        }

        return ResponseEntity.status(400).body(Json.createObjectBuilder().add("status","user exists")
                            .build().toString());
    }

    @GetMapping(path = "/user/verifyToken")
    public ResponseEntity<String> verifyLogIn(@RequestHeader("token") String token) {
        System.out.println("Verifying token");

        // check Redis if token is still valid. if valid, retrieve username from Repo
        // if not valid, erase client's token.

        String returnedId = redis.opsForValue().get(token);

        if (returnedId != null) {
            String username = userRepo.getUsername(returnedId);
            return ResponseEntity.ok().body(
                    Json.createObjectBuilder().add("userId", returnedId).add("username", username).build().toString());
        } else {
            return ResponseEntity.ok().body(Json.createObjectBuilder().add("userId", "0").build().toString());
        }

    }

    // @PostMapping(path="/user/googleLogin")
    // public ResponseEntity<String> googleLogin(@RequestParam String credential){

    // System.out.println(credential);
    // return ResponseEntity.ok().body("logged in");
    // }

    @PostMapping(path = "/user/googleLogin")
    public ResponseEntity<String> googleLogin(@RequestBody String credentials) {

        System.out.println(credentials);
        GoogleUser user;
        try {
            Optional<GoogleUser> opt = googleTokenSvc.verify(credentials);

            if (opt.isPresent()) {
                user = opt.get();

                Integer retrievedId = userRepo.getUserIdFromGmail(user.getEmail());

                // check if gmail exists in user database
                // if doesnt, create account, then issue token
                // if it does, issue token
                if (retrievedId == 0) {
                    Integer createdId = userRepo.createGoogleUser(user.getName(), user.getEmail());

                    String authToken = appTokenSvc.createAndStoreToken(createdId);

                    return ResponseEntity.ok()
                            .body(Json.createObjectBuilder().add("status", "User Found").add("name", user.getName())
                                    .add("token", authToken).build().toString());
                } else {
                    String authToken = appTokenSvc.createAndStoreToken(retrievedId);

                    return ResponseEntity.ok()
                            .body(Json.createObjectBuilder().add("status", "User Found").add("name", user.getName())
                                    .add("token", authToken).build().toString());
                }

            } else {
                return ResponseEntity.badRequest().body(
                        Json.createObjectBuilder().add("status", "Not able to verify Google user").build().toString());
            }
        } catch (GeneralSecurityException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(Json.createObjectBuilder().add("status", "Unable to verify").build().toString());
        }

    }

    @GetMapping(path = "/calendar/list")
    public ResponseEntity<String> getGoogleCalendars(@RequestHeader String token) {

        JsonArray calendarList = calendarSvc.getCalendars(token);

        return ResponseEntity.ok().body(calendarList.toString());
    }

    @PostMapping(path = "/calendar/insert")
    public ResponseEntity<String> insertCalendarEvent(@RequestPart String time, @RequestPart String date,
            @RequestPart String calendarId, @RequestHeader String token, @RequestPart String location) {

        System.out.println(time + date + calendarId);

        calendarSvc.insertEvent(calendarId, date, time, location, token);

        return null;
    }

    @PostMapping(path = "/post/vote")
    public void postVote(@RequestHeader String vote, @RequestHeader String token, @RequestHeader String postId, @RequestHeader String postVoteChange) {

        Integer parsedVote = Integer.parseInt(vote);

        System.out.println("PARSEDVOTE"+ parsedVote);
        // check Redis if token is still valid. if valid, retrieve username from Repo

        String returnedId = redis.opsForValue().get(token);
        if(returnedId!=null){
            sqlRepo.handleVote(parsedVote,Integer.parseInt(postId),Integer.parseInt(returnedId),Integer.parseInt(postVoteChange));
        }

    }

    @GetMapping(path="/home/posts")
    public ResponseEntity<String> getHomePosts(@RequestHeader String pageNo){
        List<Post> postList = sqlRepo.getHomePosts(Integer.parseInt(pageNo));

        JsonArrayBuilder arrayBlder = Json.createArrayBuilder();
        for(Post post : postList){
            arrayBlder = arrayBlder.add(post.toJSON());
        }

        JsonArray result = arrayBlder.build();

        return ResponseEntity.ok().body(result.toString());
    }

    @GetMapping(path="/restaurants")
    public ResponseEntity<String> getRestDetails(@RequestHeader String restId, @RequestHeader String pageNo){

        JsonObject postList = sqlRepo.getRestDetails(restId, Integer.parseInt(pageNo));

        return ResponseEntity.ok().body(postList.toString());
    }

}
