package io.procker.codeshare.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public class ShareUtils {

    public static String sendRequest(String url, JsonObject body) {
        try {
            // create request
            HttpUriRequest request = RequestBuilder
                    .post()
                    .setUri(URI.create(url))
                    .setEntity(new StringEntity(new Gson().toJson(body)))
                    .addHeader("Content-Type", "application/json")
                    .build();

            // create client
            HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).setUserAgent("Uranium Updater").build();

            // execute request
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == 201) {
                // convert response to json
                JsonObject responseJson = new Gson().fromJson(EntityUtils.toString(response.getEntity()), JsonObject.class);

                // check status
                if(responseJson.get("status").getAsString().equals("success")) {

                    // get result json object
                    JsonObject resultJson = responseJson.get("result").getAsJsonObject();
                    return resultJson.get("id").getAsString();

                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
