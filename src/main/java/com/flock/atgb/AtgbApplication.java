package com.flock.atgb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SpringBootApplication
@RestController
public class AtgbApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtgbApplication.class, args);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/hello")
    public String hi(@RequestHeader MultiValueMap<String, String> headers, HttpServletRequest request) {
        System.out.println("Hey There, ATGB");
        for (String key : headers.keySet()) {
            System.out.println(key + " -- " + headers.get(key));
        }
        System.out.println(request);
        System.out.println("Hey There, ATGB");
        return "ATGB";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events")
    public String flockEvents(@RequestHeader MultiValueMap<String, String> headers, @RequestBody String payload,
                              HttpServletRequest request) {

        for (String key : headers.keySet()) {
            System.out.println(key + " -- " + headers.get(key));
        }
        List<String> x_flock_event_token = headers.get("x-flock-event-token");
        System.out.println("Payload : ");
        System.out.println(payload);
        return payload;
    }
}
