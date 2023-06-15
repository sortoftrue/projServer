package com.james.projServer.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.james.projServer.Models.Post;
import com.james.projServer.Repositories.ImageRepository;
import com.james.projServer.Repositories.SQLRepo;
import com.james.projServer.Repositories.SQLUserRepo;

import jakarta.json.Json;

@RestController
public class UploadController {

    @Autowired
    ImageRepository imageRepo;
    @Autowired
    SQLRepo sqlRepo;
    @Autowired
    SQLUserRepo userRepo;
    @Autowired
    RedisTemplate<String,String> redis;

    @PostMapping(path = "/upload")
    public ResponseEntity<String> uploadPost(@RequestPart(required = false) MultipartFile[] images,
            @RequestPart String title, @RequestPart(required = false) String review,
            @RequestPart(required = false) String restName, @RequestPart(required = false) String restId,
            @RequestPart(required = false) String rating, @RequestPart String isGoogle) {

        System.out.println(title + review + restName + restId + rating + isGoogle);

        // for (MultipartFile image : images) {
        //     System.out.println(image.getOriginalFilename());
        // }

        // Either gplace_id or restaurant name. Check if gplace_id is in database.
        // If not, try to pull data from google.
        // If does not exist, create

        // Create post in SQL
        Integer createdPostId;
        Boolean chkGoogle = isGoogle.equals("1");
        if(chkGoogle){
            createdPostId = sqlRepo.createPostMap(title, review, restId, rating);
        } else {
            createdPostId = sqlRepo.createPostNoMap(title, review, restName, rating);
        }

        // Upload to Ocean and Create photos in SQL
        if (images!=null) {
            try {
                List<String> UUIDList;
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
    public ResponseEntity<String> getPost(@PathVariable String post_id) {

        System.out.println("request received!");
        Optional<Post> opt = sqlRepo.getPostById(Integer.parseInt(post_id));

        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(
                    Json.createObjectBuilder().add("error_message", "Cannot find post").build().toString());
        }

        return ResponseEntity.ok().body(opt.get().toJSON().toString());
    }

    @PostMapping(path="/user/login")
    public ResponseEntity<String> userLogin(@RequestHeader("user") String user, @RequestHeader("password") String password){
        String authToken;

        if(userRepo.verifyLogin(user, password)){
            authToken = UUID.randomUUID().toString();
            redis.opsForValue().set(user, authToken);
            return ResponseEntity.status(200).body(
                    Json.createObjectBuilder().add("loggedIn", "1").add("token", authToken).build().toString());
        }
        
        return ResponseEntity.status(401).body(
                    Json.createObjectBuilder().add("error", "Invalid user/password").build().toString());
    }

    @PostMapping(path="/user/create")
    public ResponseEntity<String> createUser(@RequestHeader("user") String user, @RequestHeader("password") String password){

        Boolean createdId = userRepo.createUser(user, password);
        if(createdId){
            ResponseEntity.ok().body("User created!");
        }

        return ResponseEntity.ok().body("User already exists");
    }

}
