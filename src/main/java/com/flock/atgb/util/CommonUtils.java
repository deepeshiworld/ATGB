package com.flock.atgb.util;

import co.flock.FlockApiClient;
import co.flock.model.message.Message;
import co.flock.model.message.attachments.*;
import com.flock.atgb.com.flock.atgb.google.MapRoute;
import com.flock.atgb.dto.SlashEvent;
import com.flock.atgb.dto.TrafficReminderDto;
import org.joda.time.DateTime;

/**
 * Created by B0095829 on 4/1/17.
 */
public class CommonUtils {

    private static String pattern = "yyyy-MM-dd HH:mm";

    public static void sendNotification(MapRoute bestRouteByDuration, SlashEvent slashEvent, TrafficReminderDto reminderDto) {

        Long currentTimeTakenSec = bestRouteByDuration.getDuration();

        DateTime nowDate = new DateTime();
        DateTime currEstimateDate = nowDate.plusSeconds(currentTimeTakenSec.intValue());
        DateTime arrivalDate = new DateTime(reminderDto.getArrivalDate());


        if (currEstimateDate.toLocalDate().isBefore(arrivalDate.toLocalDate())) {
            sendBotMessage(slashEvent.getUserId(),
                    "You Should Leave : Will Reach By : " + currEstimateDate.toString(pattern));
        } else {
            sendBotMessage(slashEvent.getUserId(),
                    "Leave Immediately : Will Reach By : " + currEstimateDate.toString(pattern));
        }

    }

    public static void sendBotMessage(String toUserId, String description) {
        FlockApiClient flockApiClient = new FlockApiClient(FlockConstants.BOT_TOKEN);
        Message message = new Message(toUserId, description);

        Attachment attachment = new Attachment();
        attachment.setForward(true);
        View view = new View();
        WidgetView widget = new WidgetView();
        widget.setHeight(400);
        widget.setWidth(400);
        widget.setSrc("https://api.myairtelapp.bsbportal.in/web/images/bonanza-claim-banner-old.jpg");

        view.setWidget(widget);
        attachment.setViews(view);

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
    }
}
