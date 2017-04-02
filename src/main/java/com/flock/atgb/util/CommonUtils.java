package com.flock.atgb.util;

import co.flock.FlockApiClient;
import co.flock.model.message.Message;
import co.flock.model.message.attachments.Attachment;
import co.flock.model.message.attachments.HtmlView;
import co.flock.model.message.attachments.View;
import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.db.MongoDBManager;
import com.flock.atgb.dto.MongoDBConfig;
import com.flock.atgb.dto.SlashEvent;
import com.mongodb.BasicDBObject;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by B0095829 on 4/1/17.
 */
public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class.getCanonicalName());
    private static String pattern = "yyyy-MM-dd HH:mm";

    public static void sendNotification(MapRoute bestRouteByDuration, SlashEvent slashEvent, Date finalDestinationDate) {

        Long currentTimeTakenSec = bestRouteByDuration.getDuration();

        DateTime nowDate = new DateTime();
        DateTime currEstimateDate = nowDate.plusSeconds(currentTimeTakenSec.intValue());
        DateTime finalDestDate = new DateTime(finalDestinationDate);

        String inlineHtml = getInlineHtml(bestRouteByDuration);

        if (currEstimateDate.getMillis() <= finalDestDate.getMillis()) {
            sendBotMessage(slashEvent.getUserId(),
                    "You Should Leave : Will Reach By : " + currEstimateDate.toString(pattern), inlineHtml);
        } else {

            long diff = (currEstimateDate.getMillis() - finalDestDate.getMillis()) / (1000 * 60);
            String description = StringUtils.EMPTY;
            if (diff > 5) {
                description = "Leave Immediately : Delayed By : " + diff + "min Will Reach By : " + currEstimateDate.toString(pattern);
            } else {
                description = "Leave Immediately , Will Reach By : " + currEstimateDate.toString(pattern);
            }
            sendBotMessage(slashEvent.getUserId(), description, inlineHtml);
        }

    }

    public static void sendBotMessage(String toUserId, String description, String htmlInline) {
        FlockApiClient flockApiClient = new FlockApiClient(FlockConstants.BOT_TOKEN);
        Message message = new Message(toUserId, description);

        Attachment attachment = new Attachment();
        attachment.setForward(true);
        View view = new View();

        /*WidgetView widget = new WidgetView();
        widget.setHeight(400);
        widget.setWidth(400);
        widget.setSrc("https://api.myairtelapp.bsbportal.in/web/images/bonanza-claim-banner-old.jpg");

        view.setWidget(widget);*/

        // HTML
        HtmlView htmlView = new HtmlView();
        htmlView.setInline(htmlInline);
        htmlView.setHeight(400);
        htmlView.setWidth(600);

        view.setHtml(htmlView);

        attachment.setViews(view);

        /*Image image1 = new Image();
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

        view.setImage(imageView);*/

        Attachment[] attachments = new Attachment[1];
        attachments[0] = attachment;

        message.setAttachments(attachments);
        try {
            flockApiClient.chatSendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Reads file and return string object */
    public static String getDataFromFile(String filename) {
        String content = null;
        try {
            Path pwd = Paths.get("").toAbsolutePath();
            content = new String(Files.readAllBytes(Paths.get(filename)));
        } catch (Exception e) {
            logger.error("Error [{}] while reading file [{}]", e.toString(), filename);
        }

        return content;
    }

    public static String getInlineHtml(MapRoute bestRouteByDuration) {
        String displayHtml = CommonUtils.getDataFromFile("src/main/resources/displayTraffic.html");
        displayHtml = displayHtml.replace("DURATION_TRIP", bestRouteByDuration.getDurationInWords());
        displayHtml = displayHtml.replace("SOURCE_LOCATION", bestRouteByDuration.getSourceName());
        displayHtml = displayHtml.replace("DESTINATION_LOCATION", bestRouteByDuration.getDestinationName());
        displayHtml = displayHtml.replace("SOURCE_LAT", bestRouteByDuration.getSourceLat() + "");
        displayHtml = displayHtml.replace("SOURCE_LNG", bestRouteByDuration.getSourceLng() + "");
        displayHtml = displayHtml.replace("DESTINATION_LAT", bestRouteByDuration.getDestinationLat() + "");
        displayHtml = displayHtml.replace("DESTINATION_LNG", bestRouteByDuration.getDestinationLng() + "");

        return displayHtml;
    }


    public static String getInlineHtml(String uid) {
        String displayHtml = CommonUtils.getDataFromFile("src/main/resources/locationSelector.html");
        displayHtml = displayHtml.replace("USER_ID", uid);

        return displayHtml;
    }

    public static String getUpdateListHtml(List<SlashEvent> upcomingTrafficUpdates) {
        if (upcomingTrafficUpdates.size() != 0) {
            StringBuilder builder = new StringBuilder();
            String name = "Bot";
            if (upcomingTrafficUpdates.get(0) != null && StringUtils.isNotBlank(upcomingTrafficUpdates.get(0).getUserName())) {
                name = upcomingTrafficUpdates.get(0).getUserName();
            }
            builder.append("<!doctype html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Traffic Update</title><link href=\"https://api.myairtelapp.bsbportal.in/web/css/jackpot.css\" type=\"text/css\" rel=\"stylesheet\"><script src=\"https://api.myairtelapp.bsbportal.in/web/js/jquery.min.js\"></script></head><body><section class=\"body-block\"><div class=\"top-header\">");
            builder.append("<p class=\"header\">" + name + " : Upcoming Traffic Update Events</p><div class=\"table\"><div class=\"data-consumed\"><span class=\"grey\"></span><span class=\"green\"></span></div></div></div><div id=\"top-div\">");


            for (SlashEvent slashEvent : upcomingTrafficUpdates) {

                long minutes = (slashEvent.getTimenTakenSec() % 3600) / 60;
                String reachTime = minutes + "";
            /*TrafficReminderDto reminderDto = new TrafficReminderDto();
            reminderDto.parse(slashEvent.getText());
            Date finalTimeToReach = reminderDto.getFinalDestinationDate();*/
                String finalTimeToReach = slashEvent.getFinalTimeToReach();
                builder.append("<article class=\"data-block\"><div class=\"inner-header\"><div class=\"refer red\"><span class=\"icon\"><img src=\"https://cdn0.iconfinder.com/data/icons/geo-points/154/time-512.png\" width=\"25\" alt=\"refer\"></span><p style=\"font-size:10px;white-space:pre-wrap;text-align:justify;padding:0px;\">" + slashEvent.getSourceName() + " -> " + slashEvent.getDestinationName() + "</p></div><div class=\"data\"><span class=\"mb\"><a href=\"#\">" + reachTime + " minutes</a></span></div></div><div class=\"content\">Time to Reach:" + finalTimeToReach + "</div></article>");
            }
            builder.append("</section></div></body></html>");

            return builder.toString();
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("<!doctype html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Traffic Update</title><link href=\"https://api.myairtelapp.bsbportal.in/web/css/jackpot.css\" type=\"text/css\" rel=\"stylesheet\"><script src=\"https://api.myairtelapp.bsbportal.in/web/js/jquery.min.js\"></script></head><body><section class=\"body-block\"><div class=\"top-header\">");
            builder.append("<p class=\"header\"> No Updates Available</p><div class=\"table\"><div class=\"data-consumed\"><span class=\"grey\"></span><span class=\"green\"></span></div></div></div><div id=\"top-div\">");

            return builder.toString();
        }
    }


    public static boolean delete(Map<String, Object> paramsMap) {
        try {
            MongoDBConfig config = new MongoDBConfig();
            config.setMongoDBName(FlockConstants.FLOCK_DB);
            config.setMongodbHost("127.0.0.1");
            config.setMongodbPort(27017);
            config.setMongodbThreadsAllowedToBlock(50);
            config.setMongodbConnectionsPerHost(500);
            MongoDBManager mongoDBManager = new MongoDBManager(config);
            BasicDBObject dbObj = new BasicDBObject();
            dbObj.putAll(paramsMap);

            return mongoDBManager.deleteObject(FlockConstants.TRAFFIC_DB, dbObj);
        } catch (Exception e) {
            logger.error("Error creating MyAirtelAppUserPreferences for : " + ". Error : " + e.getMessage(), e);
            return false;
        }
    }
}
