package de.tu_darmstadt.epool.pfoertner.common.architecture.webapi;

import static de.tu_darmstadt.epool.pfoertner.common.Config.SERVER_ADDR;

/**
 * JSON body of a request to a calendar webhook to google
 * https://developers.google.com/calendar/v3/push
 */
public class WebhookRequest {

    private String id;
    private String type = "web_hook";
    private String address = SERVER_ADDR+"notifications";

    public WebhookRequest(String id){
        this.id = id;
    }
}
