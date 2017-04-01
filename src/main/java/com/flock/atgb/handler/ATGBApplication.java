package com.flock.atgb.handler;

import co.flock.FlockApiClient;
import co.flock.model.message.Message;
import co.flock.model.message.attachments.*;
import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.com.flock.atgb.google.MapRouteFinder;
import com.flock.atgb.dto.FlockEvent;
import com.flock.atgb.exception.FlockException;
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
import java.util.List;

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
                    flockEventService.processTrafficUpdateRequest(payload);
                    break;

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

    @RequestMapping(method = RequestMethod.GET, value = "/hello")
    @ResponseBody
    public ResponseEntity<String> hi(@RequestHeader MultiValueMap<String, String> headers, HttpServletRequest request) {
        logger.info("Request [{}]", request);

        FlockApiClient flockApiClient = new FlockApiClient(FlockConstants.BOT_TOKEN);
        Message message = new Message("u:g6ghgghe66h8rzyk", "Hey There!!");

        Attachment attachment = new Attachment();
        attachment.setForward(true);
        View view = new View();
        WidgetView widget = new WidgetView();
        widget.setHeight(400);
        widget.setWidth(400);
        widget.setSrc("https://api.myairtelapp.bsbportal.in/web/images/bonanza-claim-banner-old.jpg");

        //view.setWidget(widget);
        attachment.setViews(view);

        /*Download[] downloads = new Download[2];
        Download download = new Download();
        download.setFilename();
        download.setMime();*/

        Image image1 = new Image();
        image1.setSrc("https://api.myairtelapp.bsbportal.in/web/images/bonanza-claim-banner-old.jpg");
        image1.setHeight(300);
        image1.setWidth(300);

        Image image2 = new Image();
        image1.setSrc("https://api.myairtelapp.bsbportal.in/web/images/bonanza-postpaid-300mb.png");
        image1.setHeight(100);
        image1.setWidth(100);

        ImageView imageView = new ImageView();
        imageView.setOriginal(image1);
        imageView.setThumbnail(image2);
        imageView.setFilename("Bonanza");

        view.setImage(imageView);

        Attachment[] attachments = new Attachment[1];
        attachments[0] = attachment;

        message.setAttachments(attachments);
        try {
            flockApiClient.chatSendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*MapRouteFinder routeFinder = MapRouteFinder.createRouteFinder(28.4547268, 77.0737148, 18.4547268, 57.0737148);
        try {
            List<MapRoute> allRoutes = routeFinder.getAllRoutes();
            MapRoute bestRouteByDistance = routeFinder.getBestRouteByDistance();
            MapRoute bestRouteByDuration = routeFinder.getBestRouteByDuration();
            System.out.println();
        } catch (FlockException e) {
            e.printStackTrace();
        }*/

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
