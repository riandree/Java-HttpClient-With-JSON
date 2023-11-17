package de.rieck.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Uses a custom generic BodyHandler to parse the HttpResponse such that the returned response object is of type
 * Response&lt;ZippoPostcodeData&gt; instead of Response&lt;String&gt;
 */
public class HttpClientUsingCustomBodyHandler extends AbstractZippoClient {

    static class JSONBodyHandler<J> implements HttpResponse.BodyHandler<J> {

        private Class<J> targetClass;

        public JSONBodyHandler(Class<J> targetClass) {
            this.targetClass = targetClass;
        }

        @Override
        public HttpResponse.BodySubscriber<J> apply(HttpResponse.ResponseInfo responseInfo) {
            HttpResponse.BodySubscriber<String> upstreamSubscriber = HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8);   // ACHTUNG !!! StandardCharsets.UTF_8

            HttpResponse.BodySubscriber<J> jsonSubscriber = HttpResponse.BodySubscribers.mapping(upstreamSubscriber, (bodyAsString) -> {
                try {
                    var mapper = new ObjectMapper();
                    return mapper.readValue(bodyAsString, targetClass);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });

            return jsonSubscriber;
        }
    }

    protected ZippoPostcodeData requestPostcodeData(HttpRequest zippoHttpRequest) {
        HttpResponse<ZippoPostcodeData> response = null;
        try {
            response = HttpClient.newHttpClient().send(zippoHttpRequest, new JSONBodyHandler<>(ZippoPostcodeData.class));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response.body();
    }

}
