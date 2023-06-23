package com.james.projServer.Services;

import java.io.StringReader;

import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class GoogleCalendar {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public JsonArray getCalendars(String credentials) {

        System.out.println(credentials);

        String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/calendar/v3/users/me/calendarList")
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + credentials);

        // Create the /get request
        RequestEntity<Void> req = RequestEntity.get(url).headers(headers).build();

        RestTemplate template = new RestTemplate();

        ResponseEntity<String> resp = template.exchange(req, String.class);

        System.out.printf("status code:%d\n", resp.getStatusCodeValue());

        String payload = resp.getBody();

        System.out.println(payload);

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject json = reader.readObject();
        JsonArray calendarList = json.getJsonArray("items");

        return calendarList;

    }

    public Boolean insertEvent(String calendarId, String date, String time, String location, String credentials) {

        parseDate(date, time);



        return null;
    }

    private String parseDate(String passedDate, String time) {

        String result = "";
        String month;
        String hour;

        String monthText = passedDate.substring(4, 7);
        switch (monthText) {
            case "Jan":
                month = "01";
                break;
            case "Feb":
                month = "02";
                break;
            case "Mar":
                month = "03";
                break;
            case "Apr":
                month = "04";
                break;
            case "May":
                month = "05";
                break;
            case "Jun":
                month = "06";
                break;
            case "Jul":
                month = "07";
                break;
            case "Aug":
                month = "08";
                break;
            case "Sep":
                month = "09";
                break;
            case "Oct":
                month = "10";
                break;
            case "Nov":
                month = "11";
                break;
            case "Dec":
                month = "12";
                break;
            default:
            month="01";
        }
        String day = passedDate.substring(8, 10);
        String year = passedDate.substring(11, 15);
        String[] splitTime = time.split(" ");
        String am_pm = splitTime[1];
        int parsedhour = Integer.parseInt(splitTime[0].split(":")[0]);
        if(parsedhour == 12){
            parsedhour = 0;
        }

        if(am_pm.equals("PM")){
            parsedhour += 12;
            System.out.println("parsed hour = " + parsedhour);    
        }

        if(parsedhour < 10){
            hour = "0"+parsedhour;
        } else hour = Integer.toString(parsedhour);

        System.out.println(hour);

        String minutes = splitTime[0].split(":")[1];

        result = year+"-"+month+"-"+day+"T"+hour+":"+minutes+":00+08:00";
        System.out.println(result);
        return result;
    }

}
