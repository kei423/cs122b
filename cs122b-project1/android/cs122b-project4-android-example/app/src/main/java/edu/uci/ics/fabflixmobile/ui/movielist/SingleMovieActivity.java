package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.databinding.SinglemovieBinding;
import org.json.JSONException;
import org.json.JSONObject;

public class SingleMovieActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SinglemovieBinding binding = SinglemovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        JSONObject jsonArrayString;
        try {
            jsonArrayString = new JSONObject(getIntent().getStringExtra("jsonArray"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
//        setContentView(R.layout.singlemovie);
        Movie movie = makeMovieObject(jsonArrayString);
        System.out.println(jsonArrayString);

        // Create TextViews dynamically and add them to the layout
        TextView titleTextView = findViewById(R.id.title);
        TextView yearTextView = findViewById(R.id.year);
        TextView ratingTextView = findViewById(R.id.rating);
        TextView directorTextView = findViewById(R.id.director);
        TextView genresTextView = findViewById(R.id.genres);
        TextView starsTextView = findViewById(R.id.stars);
        titleTextView.setText("Title: " + movie.getName());
        yearTextView.setText("Year: " + movie.getYear());
        ratingTextView.setText("Rating: " + movie.getRating());
        directorTextView.setText("Director: " + movie.getDirector());
        genresTextView.setText("Genres: " + String.join(", ", movie.getGenres()));
        starsTextView.setText("Stars: " + String.join(", ", movie.getStars()));
    }

    private Movie makeMovieObject(JSONObject jsonObject) {
        String movieTitle = jsonObject.optString("movieTitle", "");
        int movieYear = jsonObject.optInt("movieYear", 0);
        String movieDirector = jsonObject.optString("movieDirector", "");
        String[] movieGenres = getArrayFromJsonArray(jsonObject.optString("movieGenres", ""));
        String[] movieStars = getArrayFromJsonArray(jsonObject.optString("movieStars", ""));
        Double tempRating = jsonObject.optDouble("movieRating", -0.1);
        String movieRating = (tempRating != -0.1) ? String.valueOf(tempRating) : "N/A";

        return new Movie(movieTitle, (short) movieYear, movieDirector, movieGenres, movieStars, movieRating);
    }

    private String[] getArrayFromJsonArray(String array) {
        if (array == null || array.isEmpty()) {
            return new String[0];
        } else {
            return array.split(",");
        }
    }
}
