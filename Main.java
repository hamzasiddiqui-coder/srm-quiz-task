import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import org.json.*;

public class Main {

    static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    static final String REG_NO = "AP23110010412"; 

    public static void main(String[] args) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        // To remove duplicates
        Set<String> seen = new HashSet<>();

        // Store total score
        Map<String, Integer> scores = new HashMap<>();

        for (int i = 0; i < 10; i++) {

            String url = BASE_URL + "/quiz/messages?regNo=" + REG_NO + "&poll=" + i;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = new JSONObject(response.body());
            JSONArray events = json.getJSONArray("events");

            for (int j = 0; j < events.length(); j++) {
                JSONObject event = events.getJSONObject(j);

                String roundId = event.getString("roundId");
                String participant = event.getString("participant");
                int score = event.getInt("score");

                String key = roundId + "_" + participant;

                // Deduplication
                if (!seen.contains(key)) {
                    seen.add(key);
                    scores.put(participant, scores.getOrDefault(participant, 0) + score);
                }
            }

            // 5 sec delay
            Thread.sleep(5000);
        }

        // Create leaderboard
        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>(scores.entrySet());

        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        JSONArray leaderboardJson = new JSONArray();

        for (Map.Entry<String, Integer> entry : leaderboard) {
            JSONObject obj = new JSONObject();
            obj.put("participant", entry.getKey());
            obj.put("totalScore", entry.getValue());
            leaderboardJson.put(obj);
        }

        JSONObject finalBody = new JSONObject();
        finalBody.put("regNo", REG_NO);
        finalBody.put("leaderboard", leaderboardJson);

        // POST request
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/quiz/submit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(finalBody.toString()))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response: " + postResponse.body());
    }
}