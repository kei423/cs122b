package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {

    private int index = 0;
    //    private final String host = "10.0.2.2";
//    private final String host = "13.57.130.118";
    private final String host = "ec2-13-57-130-118.us-west-1.compute.amazonaws.com";
    private final String port = "8443";
    //    private final String domain = "cs122b_project1_war";
    private final String domain = "cs122b-fabflix";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
//    private final String baseURL = "https://fabflix.world:8443/cs122b-fabflix";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);
        String jsonArrayString = getIntent().getStringExtra("jsonArray");

        JSONArray jsonArray;
        try {
            // Convert the string back to JSONArray
            jsonArray = new JSONArray(jsonArrayString);
            ArrayList<Movie> movies = getMovieList(jsonArray);
            updateMovieList(movies, jsonArray);
            setUpButtons(movies);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Movie> getMovieList(JSONArray jsonArray) throws JSONException {
        ArrayList<Movie> movies = new ArrayList<>();
        for (int i = 0; i < jsonArray.length() - 1; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String movieTitle = jsonObject.optString("movieTitle", "");
            int movieYear = jsonObject.optInt("movieYear", 0);
            String movieDirector = jsonObject.optString("movieDirector", "");
            String[] movieGenres = getArrayFromJsonArray(jsonObject.optString("movieGenres", ""));
            String[] movieStars = getArrayFromJsonArray(jsonObject.optString("movieStars", ""));
            Double tempRating = jsonObject.optDouble("movieRating", -0.1);
            String movieRating = (tempRating != -0.1) ? String.valueOf(tempRating) : "N/A";

            movies.add(new Movie(movieTitle, (short) movieYear, movieDirector, movieGenres, movieStars, movieRating));
        }
        return movies;
    }

    @SuppressLint("SetTextI18n")
    private void showNextMovie(TextView pageNumber) {
        search("next");
        index += 10;
        pageNumber.setText(String.valueOf(index / 10 + 1));
    }

    @SuppressLint("SetTextI18n")
    private void showPreviousMovie(TextView pageNumber) {
        search("prev");
        index -= 10;
        pageNumber.setText(String.valueOf(index / 10 + 1));
    }

    private String[] getArrayFromJsonArray(String array) {
        if (array == null || array.isEmpty()) {
            return new String[0];
        } else {
            return array.split(",");
        }
    }

    private void updateMovieList(ArrayList<Movie> movies, JSONArray jsonArray) {
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
            try {
                SingleMoviePage.putExtra("jsonArray", jsonArray.getJSONObject(position).toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            // activate the list page.
            startActivity(SingleMoviePage);
        });
    }

    private void setUpButtons(ArrayList<Movie> movies) {
        // Handle prev and next button clicks
        Button prevButton = findViewById(R.id.prev_button);
        TextView pageNumber = findViewById(R.id.page_number);
        Button nextButton = findViewById(R.id.next_button);
        prevButton.setOnClickListener(view -> showPreviousMovie(pageNumber));
        nextButton.setOnClickListener(view -> showNextMovie(pageNumber));
        System.out.println(index);
        prevButton.setEnabled(index != 0);
        nextButton.setEnabled(movies.size() == 10);
    }

    @SuppressLint("SetTextI18n")
    public void search(String query) {
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movie-list?action="+query+"&mobile=mobile",
                response -> {
                    Log.d("search.success", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        ArrayList<Movie> movies = getMovieList(jsonArray);
                        updateMovieList(movies, jsonArray);
                        setUpButtons(movies);
                    } catch (JSONException e) {
                        Log.e("search.error", "Error parsing JSON response: " + e.getMessage());
                    }
                },
                error -> {
                    // error
                    Log.d("search.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("action", query);
                params.put("mobile", "mobile");
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }
}