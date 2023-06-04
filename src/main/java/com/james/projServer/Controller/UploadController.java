package com.james.projServer.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.james.projServer.Models.Post;
import com.james.projServer.Repositories.ImageRepository;
import com.james.projServer.Repositories.SQLRepo;

import jakarta.json.Json;

@RestController
public class UploadController {
    
    @Autowired
    ImageRepository imageRepo;
    @Autowired
    SQLRepo sqlRepo;


    @PostMapping(path="/upload")
	public ResponseEntity<String> uploadPost(@RequestPart(required = false) MultipartFile[] images,
	@RequestPart String title, @RequestPart(required = false) String review, @RequestPart(required = false) String restName, @RequestPart(required = false) String rating){

        System.out.println(title + review + restName + rating);

        for(MultipartFile image: images){
            System.out.println(image.getOriginalFilename());
        }

        //Create post in SQL
        Integer createdPostId = sqlRepo.createPost(title, review, restName, rating);

        try {
            List<String> UUIDList;
            System.out.println("Uploading images");
            sqlRepo.createPhotos(imageRepo.uploadToOcean(images), createdPostId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping(path="/post/{post_id}")
    public ResponseEntity<String> getPost(@PathVariable String post_id){

        System.out.println("request received!");
        Optional<Post> opt = sqlRepo.getPostById(Integer.parseInt(post_id));

        if(opt.isEmpty()){
            return ResponseEntity.status(404).body(
                Json.createObjectBuilder().add("error_message","Cannot find post").build().toString()
            );
        }

        return ResponseEntity.ok().body(opt.get().toJSON().toString());
    }

}
