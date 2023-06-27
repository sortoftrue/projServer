package com.james.projServer.Services;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;

@Service
public class CallGoogle {

    @Value("${GOOGLEAPIKEY}")
    String apiKey;

    public String[] getGplaceName(String gplaceId){

        String url = UriComponentsBuilder.fromUriString("https://maps.googleapis.com/maps/api/place/details/json").queryParam("place_id",gplaceId).queryParam("key",apiKey).queryParam("fields","name,editorial_summary").toUriString();

        //Create the /get request
        RequestEntity<Void> req = RequestEntity.get(url).build();

        RestTemplate template = new RestTemplate();

        ResponseEntity<String> resp = template.exchange(req, String.class);

        System.out.printf("status code:%d\n",resp.getStatusCodeValue());

        String payload = resp.getBody();

        System.out.println(payload);

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject json = reader.readObject();
        JsonObject headers = json.getJsonObject("result");
        String restName = headers.get("name").toString();
        String cutName = restName.substring(1, restName.length()-1);
        String summary;
        try{
            summary = headers.getJsonObject("editorial_summary").getString("overview");
        } catch(Exception e){
            summary = "";
        }
        

        System.out.println(restName + summary);
        String[] result = new String[]{cutName,summary};

        return result;
    }

}
