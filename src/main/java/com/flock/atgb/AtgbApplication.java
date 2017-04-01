package com.flock.atgb;

import com.flock.atgb.Util.FlockConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
@Controller
public class AtgbApplication {

    private static final Logger logger = LoggerFactory.getLogger(AtgbApplication.class.getCanonicalName());

    public static void main(String[] args) {
        SpringApplication.run(AtgbApplication.class, args);
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

    @RequestMapping(method = RequestMethod.POST, value = "/events")
    @ResponseBody
    public ResponseEntity<String> flockEvents(@RequestHeader MultiValueMap<String, String> headers, @RequestBody String payload,
                                              HttpServletRequest request) {

        String x_flock_event_token = headers.getFirst(FlockConstants.X_FLOCK_EVENT_TOKEN);
        if (StringUtils.isBlank(x_flock_event_token)) {
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }

        logger.info("Payload : ");
        logger.info(payload);
        return ResponseEntity.status(HttpStatus.OK).body(payload);
    }
}
