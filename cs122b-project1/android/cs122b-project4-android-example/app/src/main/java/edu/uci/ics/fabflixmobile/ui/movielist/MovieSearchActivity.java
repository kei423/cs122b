package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityMoviesearchBinding;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class MovieSearchActivity extends AppCompatActivity {

    private EditText query;
    private TextView message;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
//    private final String host = "10.0.2.2";
//    private final String host = "13.57.130.118";
    private final String host = "ec2-13-57-130-118.us-west-1.compute.amazonaws.com";
    private final String port = "8443";
//    private final String domain = "cs122b_project1_war";
    private final String domain = "cs122b-fabflix";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
//    private final String baseURL = "https://fabflix.world:8443/cs122b-fabflix";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMoviesearchBinding binding = ActivityMoviesearchBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        query = binding.searchQuery;
        message = binding.message;
        final Button searchButton = binding.search;

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> search());
    }

    private void displayErrorMessage(String errorMessage) {
        // Use a Toast to display the error message on the client's mobile device
        Toast.makeText(MovieSearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    public void search() {
        message.setText("Trying to search");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        Log.d("search.test", "here");
        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movie-list?action=search&value=" + query.getText().toString() + "&mobile=mobile",
//                baseURL + "/api/movie-list?action=advancedSearch&title=" + query.getText().toString() + "&year=&director=&star=&mobile=mobile",
                response -> {
                    Log.d("search.success", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        String jsonArrayString = jsonArray.toString();
                        // Complete and destroy login activity once successful
                        finish();
                        // initialize the activity(page)/destination
                        Intent MovieListPage = new Intent(MovieSearchActivity.this, MovieListActivity.class);
                        // Put the json-array as a string as an extra param in the intent
                        MovieListPage.putExtra("jsonArray", jsonArrayString);
                        // activate the list page.
                        startActivity(MovieListPage);
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
                params.put("action", "search");
                params.put("value", query.getText().toString());
                params.put("mobile", "mobile");
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }
}
