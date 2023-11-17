package de.rieck.demo;

import de.rieck.demo.AbstractZippoClient.Country;
import de.rieck.demo.AbstractZippoClient.ZippoPlace;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static de.rieck.demo.AbstractZippoClient.Country.Daenemark;
import static de.rieck.demo.AbstractZippoClient.Country.Deutschland;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-Tests for the different implementations of the HttpClient for zippopotam.us.
 */
public class HttpClientIntegrationTest {

    public static final Map<Country, List<String>> COUNTRIES_AND_ZIPS = Map.of(
            Deutschland, List.of("22880", "22305"),
            Daenemark, List.of("2400"));
    static Map<String, AbstractZippoClient.ZippoPostcodeData> EXPECTED_RESULTS = Map.of(
            Daenemark + "2400", new AbstractZippoClient.ZippoPostcodeData("2400", Daenemark.name, Daenemark.code,
                    new ZippoPlace[]{
                            new ZippoPlace("København NV", "55.6777", "12.5709", "", "")
                    }
            ),
            Deutschland + "22880", new AbstractZippoClient.ZippoPostcodeData("22880", Deutschland.name, Deutschland.code,
                    new ZippoPlace[]{
                            new ZippoPlace("Wedel", "53.5837", "9.7234", "Schleswig-Holstein", "SH")
                    }
            ),
            Deutschland + "22305", new AbstractZippoClient.ZippoPostcodeData("22305", Deutschland.name, Deutschland.code,
                    new ZippoPlace[]{
                            new ZippoPlace("Hamburg Barmbek-Nord", "02000", "53.5961", "Hamburg", "HH")
                    }
            )
    );

    /**
     * Provides the different Client-Implementations for the parameterized tests.
     */
    static Stream<AbstractZippoClient> zippoClientProvider() {
        return Stream.of(
                new HttpClientUsingCustomBodyHandler(),
                new HttpClientUsingAsyncRequest(),
                new HttpClientUsingPredefinedBodyHandler());
    }

    /**
     * Asserts that the response received by one of the Clients is as expected.
     */
    private void assertClientResponse(Country country, List<String> requestedZips, List<AbstractZippoClient.ZippoPostcodeData> clientResponse) {
        assertEquals(requestedZips.size(), clientResponse.size());

        for (final String requestedZipCode : requestedZips) {
            AbstractZippoClient.ZippoPostcodeData dataForPostcode = clientResponse.stream()
                    .filter(zippoPostcodeData ->
                            zippoPostcodeData.country().equals(country.name) &&
                                    zippoPostcodeData.postcode().equals(requestedZipCode))
                    .findFirst()
                    .orElseThrow();
            var expectedPostcodeDate4Zipcode = EXPECTED_RESULTS.get(country + requestedZipCode);
            String testQualifier="for country '%s' and zipcode '%s' with expected result %s".formatted(country, requestedZipCode,expectedPostcodeDate4Zipcode);
            assertNotNull(expectedPostcodeDate4Zipcode, "No Expected data for "+testQualifier);
            assertEquals(expectedPostcodeDate4Zipcode.postcode(), dataForPostcode.postcode(),testQualifier);
            assertEquals(expectedPostcodeDate4Zipcode.country(), dataForPostcode.country(),testQualifier);
            var allResponsePlaces = asList(dataForPostcode.places());
            assertTrue(allResponsePlaces.containsAll(asList(expectedPostcodeDate4Zipcode.places())),testQualifier+"\n -- not expected all places found in\n: "+allResponsePlaces);
        }
    }

    /**
     * Tests each of the different client implementations using a Parameterized test with the different clients as
     * a parameter.
     */
    @ParameterizedTest
    @MethodSource("zippoClientProvider")
    public void testClientWithBodyHandler(AbstractZippoClient client) {
        for (final Map.Entry<Country, List<String>> countryWithZips : COUNTRIES_AND_ZIPS.entrySet()) {
            var postcodeDataResponse = client.fetchCountryData(countryWithZips.getKey(), countryWithZips.getValue());
            assertClientResponse(countryWithZips.getKey(), countryWithZips.getValue(), postcodeDataResponse);
        }
    }

}


