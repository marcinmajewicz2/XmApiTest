import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

public class FirstTest {

    final static String baseUrl="https://swapi.dev/";
    final static String filmEndPoint="api/films";
    private static Object Map;
    Response response;
    String releaseDate;
    String filmTitle;
    String characters;

    @Test(priority=1)
    public void xmFilmReleaseDateTest() {
            String apiEndpoint = "https://swapi.dev/api/films";

            // Send a GET request to the API endpoint
            response = RestAssured.get(apiEndpoint);

            // Extract the film details from the response
            filmTitle = response.jsonPath().getString("results.max { it.release_date }.title");
            releaseDate = response.jsonPath().getString("results.max { it.release_date }.release_date");

            System.out.println("Film with latest release date:");
            System.out.println("Title: " + filmTitle);
            System.out.println("Release Date: " + releaseDate);
            Assert.assertTrue(filmTitle.equals("Revenge of the Sith"), "found the film with latest release date: " + filmTitle + " but it should be: Revenge of the Sith");

    }

    @Test(priority=2)
    public void xmFilmReleaseDateTallestPersonTest() {
            List<String> charactersAPI = getApiEndpointList(response.jsonPath().getString("results.max { it.release_date }.characters"));
            //double maxHeight = findMaxHeight(charactersAPI);
            //System.out.println("Maximum height among the characters: " + maxHeight + " cm");
            Map.Entry<String, Double> lastEntry = findMaxNameAndHeight(charactersAPI);
            System.out.println("Maximum height among the characters: " + lastEntry.getValue() + " cm");
            System.out.println("Last key: " + lastEntry.getKey());
            System.out.println("Last value: " + lastEntry.getValue());
            Assert.assertTrue(lastEntry.getKey().equals("Tarfful"), "found the tallest person :" + lastEntry.getKey() + ": but it should be: Tarfful");
    }

    @Test(priority=3)
    public void xmFilmReleaseDateTallestPersonEverTest() {
        {
            //String apiEndpoint = "https://swapi.dev/api/films";
            //response = RestAssured.get(apiEndpoint);
            // GET all characters from films
            List<String> charactersAPI2 = getApiEndpointList(response.jsonPath().getString("results.characters"));
            Set<String> uniqueSet = new HashSet<>(charactersAPI2);
            List<String> charactersAPI =  new ArrayList<>();
            charactersAPI.addAll(uniqueSet);

            //double maxHeight = findMaxHeight(charactersAPI);
            //System.out.println("Maximum height among the characters EVER: " + maxHeight + " cm");
            Map.Entry<String, Double> lastEntry = findMaxNameAndHeight(charactersAPI);
            System.out.println("Maximum height among the characters EVER: " + lastEntry.getValue() + " cm");
            System.out.println("Last key: " + lastEntry.getKey());
            System.out.println("Last value: " + lastEntry.getValue());
            Assert.assertTrue(lastEntry.getKey().equals("Yarael Poof"), "found the tallest person ever :" + lastEntry.getKey() + ": but it should be: Yarael Poof");
        }
    }

    @Test(priority=4)
    public void validatePeopleEndpointSchema() {
        RestAssured.baseURI = "https://swapi.dev/api";

        RestAssured.given()
                .when()
                .get("/people")
                .then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("people-schema.json"));
    }


    public static double findMaxHeight(List<String> apiEndpoints) {
        double max = Double.MIN_VALUE;

        for (String endpoint : apiEndpoints) {
            double height = extractHeightFromEndpoint(endpoint);
            max = Math.max(max, height);
        }

        return max;
    }

    public static java.util.Map.Entry<String, Double> findMaxNameAndHeight(List<String> apiEndpoints) {
        LinkedHashMap<String, Double> maxNameHeight = new LinkedHashMap <>();
        for (String endpoint : apiEndpoints) {
            Map<String, Double> nameHeight = extractNameAndHeightFromEndpoint(endpoint);
            maxNameHeight.putAll(nameHeight);
        }
        return getLast(sortByValue(maxNameHeight));
    }

    public static LinkedHashMap<String, Double> sortByValue(LinkedHashMap<String, Double> myMap){
        List<Map.Entry<String, Double>> entryList = new ArrayList<>(myMap.entrySet());

        Collections.sort(entryList, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : entryList) {
            result.put(e.getKey(), e.getValue());
        }

        return result;
    }

    public static <K, V> Map.Entry<K, V> getLast(java.util.Map<String, Double> map) {
        Iterator<java.util.Map.Entry<String, Double>> iterator = map.entrySet().iterator();
        Map.Entry<K, V> result = null;
        while (iterator.hasNext()) {
            result = (java.util.Map.Entry<K, V>) iterator.next();
        }
        return result;
    }

    public static double extractHeightFromEndpoint(String endpoint) {
        Response response = RestAssured.get(endpoint);
        if (response.jsonPath().getString("height").equals("unknown"))
            return 0;
        else
            return Double.parseDouble(response.jsonPath().getString("height"));
    }

    public static Map<String, Double> extractNameAndHeightFromEndpoint(String endpoint) {
        Response response = RestAssured.get(endpoint);
        Double height = Double.MAX_VALUE;
        if(response.jsonPath().getString("height").equals("unknown"))
            height= 0.0;
        else
            height = Double.parseDouble(response.jsonPath().getString("height"));
        return Collections.singletonMap(response.jsonPath().getString("name"), height);

    }

    public static List<String> getApiEndpointList(String responseEndpoint) {
        String str = responseEndpoint.replaceAll("\\[","").replaceAll("\\]","");
        return List.of(str.split(","));
    }
}
