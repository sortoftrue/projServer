package com.james.projServer.Repositories;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Repository
public class ImageRepository {

	@Autowired
	private AmazonS3 s3;
	
	public List<String> uploadToOcean(MultipartFile[] photos) throws Exception {
		
		//final String digitalOceanURL = "https://james.sgp1.digitaloceanspaces.com/";
		
		List<String> UUIDList = new LinkedList<>();

		// Upload to digitalOcean

		for (MultipartFile photo : photos) {
			System.out.println(photo.getContentType());
		}

		for (MultipartFile photo : photos) {
			String uuid = UUID.randomUUID().toString().substring(0, 10);
			System.out.println("uploading photo" + photo.getSize());
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(photo.getContentType());
			metadata.setContentLength(photo.getSize());
			
			String fileUploadName = uuid;

			PutObjectRequest putRequest;

			try {
				putRequest = new PutObjectRequest(
						"james", fileUploadName,
						photo.getInputStream(), metadata);

				putRequest.withCannedAcl(CannedAccessControlList.PublicRead);
				s3.putObject(putRequest);
				UUIDList.add(fileUploadName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return UUIDList;

	}
}
