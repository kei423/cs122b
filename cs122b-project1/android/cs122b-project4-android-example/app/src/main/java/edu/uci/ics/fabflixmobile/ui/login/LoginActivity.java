package edu.uci.ics.fabflixmobile.ui.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieSearchActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
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

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        username = binding.username;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> login());
    }

    private void displayErrorMessage(String errorMessage) {
        // Use a Toast to display the error message on the client's mobile device
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        message.setText("Trying to login");
        Log.d("login.test", "here");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/login",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("login.success", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            // Use cookie manager to check when the user is logged in.
                            CookieManager cookieManager = CookieManager.getInstance();
                            cookieManager.setCookie(baseURL, "logged in");
                            // Complete and destroy login activity once successful
                            finish();
                            // initialize the activity(page)/destination
                            Intent MovieSearchPage = new Intent(LoginActivity.this, MovieSearchActivity.class);
                            // activate the list page.
                            startActivity(MovieSearchPage);
                        }
                        else {
                            // display error message on client mobile device
                            displayErrorMessage(jsonObject.getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e("login.json.error", "Error parsing JSON response: " + e.getMessage());
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("mobile", "mobile");
                Log.d("login.request", "Params: " + params.toString());
                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}