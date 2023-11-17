package de.rieck.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * Uses an Async Request and a Lambda which uses a jackson ObjectMapper to parse the Raw JSON Response.
 */
public class HttpClientUsingAsyncRequest extends AbstractZippoClient {

    private final ObjectMapper jacksonMapper = new ObjectMapper();

    protected ZippoPostcodeData requestPostcodeData(HttpRequest zippoHttpRequest) {
        try {
            return HttpClient.newHttpClient()
                    .sendAsync(zippoHttpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .thenApply(this::parseJSONResponse)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private ZippoPostcodeData parseJSONResponse(HttpResponse<String> httpStringResponse) {
        try {
            return jacksonMapper.readValue(httpStringResponse.body(), ZippoPostcodeData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
