package com.xu.investo.webhose;


import com.buzzilla.webhose.client.WebhoseQuery;
import com.buzzilla.webhose.client.WebhoseResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.net.URL;

/**
 * Client for webhose.io API
 *
 * @author Israel Tsadok, modded here for test purposes by AXu
 */
public class WebhoseClient {
    static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private final HttpRequestFactory requestFactory;
    private final String apiKey;

    public static class WebhoseUrl extends GenericUrl {
        public WebhoseUrl(String encodedUrl) {
            super(encodedUrl);
        }

        public WebhoseUrl(URL url) {
            super(url);
        }

        @Key
        public String token;

        @Key("q")
        public String query;

        @Key
        public String format = "json";

        @Key("ts")
        public Long fromTimestamp;
    }

    public WebhoseClient(String apiKey) {
        this.requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

        this.apiKey = apiKey;
    }

    public WebhoseResponse search(WebhoseQuery query) throws IOException {
        return search(query.toString());
    }

    public WebhoseResponse search(String query) throws IOException {
        return search(query, null);
    }

    public WebhoseResponse search(String query, Long ts) throws IOException {
        WebhoseUrl url = new WebhoseUrl("http://webhose.io/search");
        url.token = this.apiKey;
        url.query = query;
        url.fromTimestamp = ts;

        HttpRequest request = requestFactory.buildGetRequest(url);
        return request.execute().parseAs(WebhoseResponse.class);
    }

    public WebhoseResponse getMore(WebhoseResponse response) throws IOException {
        WebhoseUrl url = new WebhoseUrl("http://webhose.io" + response.next);

        HttpRequest request = requestFactory.buildGetRequest(url);

        return request.execute().parseAs(WebhoseResponse.class);
    }
}
