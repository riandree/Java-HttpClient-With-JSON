package de.rieck.demo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Abstract base implementation for a REST client that uses the "zippopotam.us" Postal Codes and Zip Service
 * to request information about Zip-Codes.
 */
public abstract class AbstractZippoClient {

    enum Country {
        Deutschland("de", "Germany"), Daenemark("dk", "Denmark"), Niederlande("nl", "Netherland");
        public final String code;
        public final String name;

        Country(String countryCode, String countryName) {
            this.code = Objects.requireNonNull(countryCode);
            this.name = Objects.requireNonNull(countryName);
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    record ZippoPlace(
            @JsonProperty("place name") String placeName,
            String latitude,
            String longitude,
            String state, @JsonProperty("state abbreviation") String stateAbbreviation) {
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    record ZippoPostcodeData(@JsonProperty("post code") String postcode,
                             String country,
                             @JsonProperty("country abbreviation") String countryAbbreviation,
                             ZippoPlace[] places) {

        @Override
        public String toString() {
            var placesDesc = Arrays.stream(places).map(ZippoPlace::toString).collect(Collectors.joining(",", "[", "]"));
            return """
                    Country  : %s
                    Postcode : %s 
                    Places   : %s
                    """.formatted(country, postcode, placesDesc);
        }
    }

    /**
     * URL-Pattern for the "zippopotam.us" Postal Codes and Zip service.
     */
    static final String ZIPPOPOTAM_URI_PATTERN = "http://api.zippopotam.us/%s/%s";

    /**
     * Different implementations use different strategies to send the provided Request and handle the response.
     * @param zippoHttpRequest the request to send.
     * @return parsed "zippopotam.us" response
     */
    protected abstract ZippoPostcodeData requestPostcodeData(HttpRequest zippoHttpRequest);

    /**
     * Fetches the data for each of the provided zip-codes within the provided Country by requesting the zip-code
     * data from "hippopotam.us" and returns a list of parsed responses.
     */
    public List<ZippoPostcodeData> fetchCountryData(Country country, List<String> zips) {
        return zips.stream()
                .map(zipcode -> mkURI4(country, zipcode))
                .map(zipResourceURI -> HttpRequest.newBuilder()
                        .GET()
                        .uri(zipResourceURI)
                        .build())
                .map(this::requestPostcodeData)
                .collect(Collectors.toList());
    }

    /**
     * prepares the "zippopotam.us" Resource URI for requesting ZIP-Code data for the provided country and zip-code.
     */
    private URI mkURI4(Country country, String zipcode) {
        try {
            return new URI(ZIPPOPOTAM_URI_PATTERN.formatted(country.code, URLEncoder.encode(zipcode)));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
