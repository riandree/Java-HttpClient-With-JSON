package de.rieck.demo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Uses the predefined HttpResponse.BodyHandlers.ofString() body handler and parses the String valued result
 * using Jackson after the response has been returned.
 */
public class HttpClientUsingPredefinedBodyHandler extends AbstractZippoClient {

    private final ObjectMapper jacksonMapper = new ObjectMapper();

    protected ZippoPostcodeData requestPostcodeData(HttpRequest zippoHttpRequest) {
        try {
            HttpResponse<String> jsonResponse = HttpClient.newHttpClient()
                    .send(zippoHttpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return jacksonMapper.readValue(jsonResponse.body(), ZippoPostcodeData.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
