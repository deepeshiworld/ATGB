package com.flock.atgb.handler;

import com.flock.atgb.dto.FlockEvent;
import com.flock.atgb.service.FlockEventService;
import com.flock.atgb.util.FlockConstants;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SpringBootApplication(scanBasePackages = {"com.flock.atgb"})
@Controller
public class AtgbApplication implements IAuthenticatedUrlRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AtgbApplication.class.getCanonicalName());

    @Autowired
    FlockEventService flockEventService;

    public static void main(String[] args) {
        SpringApplication.run(AtgbApplication.class, args);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/events")
    @ResponseBody
    public ResponseEntity<Object> flockEventListener(@RequestHeader MultiValueMap<String, String> headers, @RequestBody String payload,
                                                     HttpServletRequest request) {

        logger.info("Flock Event Received : ", payload);
        try {
            String x_flock_event_token = headers.getFirst(FlockConstants.X_FLOCK_EVENT_TOKEN);
            if (StringUtils.isBlank(x_flock_event_token)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
            }

            // Authentication
            if (authenticate(x_flock_event_token)) {
                logger.info("Valid Flock Event Token", x_flock_event_token);
            } else {
                logger.info("Invalid Flock Event Token", x_flock_event_token);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Flock Event Token");
            }

            // Parsing payload
            JSONParser parser = new JSONParser();
            JSONObject jObj = (JSONObject) parser.parse(payload);
            String eventName = (String) jObj.get("name");
            logger.info("Event Name : ", eventName);

            FlockEvent flockEvent = FlockEvent.getFlockEventByName(eventName);

            if (flockEvent == FlockEvent.UNKNOWN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
            }

            switch (flockEvent) {
                case APP_INSTALL:

                    flockEventService.processAppInstall();
                    break;
                case APP_UNINSTALL:

                    break;
                default:

            }


        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }

        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/hello")
    @ResponseBody
    public ResponseEntity<String> hi(@RequestHeader MultiValueMap<String, String> headers, HttpServletRequest request) {
        logger.info("Hey There, ATGB");
        for (String key : headers.keySet()) {
            logger.info(key + " -- " + headers.get(key));
        }
        logger.info("Request", request);
        logger.info("Hey There, ATGB");
        return ResponseEntity.ok("Hey There, ATGB");
    }

    @Override
    public boolean authenticate(String flockEventToken) {
        return false;
    }
}
