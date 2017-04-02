package com.flock.atgb.handler;

import co.flock.FlockApiClient;
import co.flock.model.message.Message;
import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.com.flock.atgb.google.MapRouteFinder;
import com.flock.atgb.dto.FlockEvent;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.exception.FlockException;
import com.flock.atgb.service.FlockEventService;
import com.flock.atgb.util.CommonUtils;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@SpringBootApplication(scanBasePackages = {"com.flock.atgb"})
@Controller
public class ATGBApplication implements IAuthenticatedUrlRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ATGBApplication.class.getCanonicalName());

    @Autowired
    FlockEventService flockEventService;

    public static void main(String[] args) {
        SpringApplication.run(ATGBApplication.class, args);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events")
    @ResponseBody
    public ResponseEntity<Object> flockEventListener(@RequestHeader MultiValueMap<String, String> headers, @RequestBody String payload,
                                                     HttpServletRequest request) {

        logger.info("Flock Event Received [{}] ", payload);
        String responseMsg = StringUtils.EMPTY;
        boolean responseStatus = true;
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
            logger.info("Event Name [{}] ", eventName);

            FlockEvent flockEvent = FlockEvent.getFlockEventByName(eventName);

            if (flockEvent == FlockEvent.UNKNOWN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
            }

            switch (flockEvent) {
                case APP_INSTALL:

                    responseStatus = flockEventService.processAppInstall(payload);
                    if (responseStatus) {
                        responseMsg = "User Installed Successfully";
                    } else {
                        responseMsg = "Unable to install User Successfully";
                    }
                    break;
                case APP_UNINSTALL:
                    //TODO isActive False
                    responseStatus = flockEventService.processAppUninstall(jObj);
                    if (responseStatus) {
                        responseMsg = "User Installed Successfully";
                    } else {
                        responseMsg = "Unable to install User Successfully";
                    }
                    break;

                case CLIENT_SLASH_COMMAND:
                    responseStatus = flockEventService.processTrafficUpdateRequest(payload, false);
                    if (responseStatus) {
                        responseMsg = "Traffic Update Set Successfully";
                    } else {
                        responseMsg = "Unable to Set Traffic Update ";
                        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseMsg);
                    }

                    //break;

                case CLIENT_PRESS_BUTTON:
                    flockEventService.handlePressEvent(payload);
                    break;
                default:

            }

        } catch (ParseException e) {
            logger.error(e.getMessage(), e.getStackTrace());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
        }

        if (flockEventService.processAppInstall(payload)) {
            return ResponseEntity.status(HttpStatus.OK).body(responseMsg);
        }
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(responseMsg);
    }


    @RequestMapping(method = {RequestMethod.POST, RequestMethod.OPTIONS}, value = "/addTrafficUpdate")
    @ResponseBody
    public ResponseEntity<Object> addTrafficUpdate(@RequestBody String payload, HttpServletRequest request, HttpServletResponse response) {
        if (request.getMethod().equals(RequestMethod.OPTIONS)) {
            return ResponseEntity.status(HttpStatus.OK).body("");
        }

        logger.info("Flock Event Received [{}] ", payload);
        String responseMsg = StringUtils.EMPTY;
        boolean responseStatus = flockEventService.processTrafficUpdateRequest(payload, true);

        if (responseStatus) {
            responseMsg = "Traffic Update Set Successfully";
        } else {
            responseMsg = "Unable to Set Traffic Update ";
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(responseMsg);
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseMsg);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getUpdateList")
    public ResponseEntity<String> getSlashEvents(HttpServletRequest request) {
        String queryString = request.getQueryString();
        Map<String, String[]> parameterMap = request.getParameterMap();

        String[] flockEventJson;
        JSONObject flockEventObject = null;
        for (String key : parameterMap.keySet()) {

            if (key.equals("flockEvent")) {
                flockEventJson = parameterMap.get(key);
                String jObj = flockEventJson[0];
                JSONParser parser = new JSONParser();
                try {
                    flockEventObject = (JSONObject) parser.parse(jObj);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        logger.info(flockEventObject.toJSONString());
        String userId = (String) flockEventObject.get("userId");
        List<SlashEvent> upcomingTrafficUpdates = flockEventService.getUpcomingTrafficUpdates(userId);
        String updateListHtml = CommonUtils.getUpdateListHtml(upcomingTrafficUpdates);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_HTML).body(updateListHtml);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getLocationSelector")
    public ResponseEntity<String> getLocationSelector(HttpServletRequest request) {
        String queryString = request.getQueryString();
        Map<String, String[]> parameterMap = request.getParameterMap();

        String[] flockEventJson;
        JSONObject flockEventObject = null;
        for (String key : parameterMap.keySet()) {

            if (key.equals("flockEvent")) {
                flockEventJson = parameterMap.get(key);
                String jObj = flockEventJson[0];
                JSONParser parser = new JSONParser();
                try {
                    flockEventObject = (JSONObject) parser.parse(jObj);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        logger.info(flockEventObject.toJSONString());
        String userId = (String) flockEventObject.get("userId");

        String html = CommonUtils.getInlineHtml(userId);

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_HTML).body(html);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/hello")
    @ResponseBody
    public ResponseEntity<String> hi(@RequestHeader MultiValueMap<String, String> headers, HttpServletRequest request) {
        logger.info("Request [{}]", request);

        FlockApiClient flockApiClient = new FlockApiClient(FlockConstants.BOT_TOKEN);
        Message message = new Message("u:g6ghgghe66h8rzyk", "Hey There!!");

        MapRouteFinder routeFinder = MapRouteFinder.createRouteFinder(28.4547268, 77.0737148, 28.450897, 77.074916);
        try {
            List<MapRoute> allRoutes = routeFinder.getAllRoutes();
            MapRoute bestRouteByDistance = routeFinder.getBestRouteByDistance();
            MapRoute bestRouteByDuration = routeFinder.getBestRouteByDuration();

            String displayHtml = CommonUtils.getDataFromFile("src/main/resources/displayTraffic.html");
            displayHtml = displayHtml.replace("DURATION_TRIP", bestRouteByDuration.getDurationInWords());
            displayHtml = displayHtml.replace("SOURCE_LOCATION", bestRouteByDuration.getSourceName());
            displayHtml = displayHtml.replace("DESTINATION_LOCATION", bestRouteByDuration.getDestinationName());
            displayHtml = displayHtml.replace("SOURCE_LAT", bestRouteByDuration.getSourceLat() + "");
            displayHtml = displayHtml.replace("SOURCE_LNG", bestRouteByDuration.getSourceLng() + "");
            displayHtml = displayHtml.replace("DESTINATION_LAT", bestRouteByDuration.getDestinationLat() + "");
            displayHtml = displayHtml.replace("DESTINATION_LNG", bestRouteByDuration.getDestinationLng() + "");
            CommonUtils.sendBotMessage("u:g6ghgghe66h8rzyk", "Hey There!!", displayHtml);
        } catch (FlockException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("Hey There, ATGB");
    }

    @Override
    public boolean authenticate(String flockEventToken) {
        /*try {
            Claims claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(FlockConstants.APP_SECRET))
                    .parseClaimsJws(flockEventToken).getBody();
            logger.info("flock token id", claims.getId());
            logger.info("flock token subject", claims.getSubject());
            logger.info("flock token issuer", claims.getIssuer());
            logger.info("flock token expiration", claims.getExpiration());
        }
        catch(Exception ex){
            logger.error("failed to authenticate flock token", ex);
            return false;
        }*/
        return true;
    }
}
