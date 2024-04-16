import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "IndexServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        ArrayList<String> previousTitles = (ArrayList<String>) session.getAttribute("previousTitles");
        ArrayList<Integer> previousCounts = (ArrayList<Integer>) session.getAttribute("previousCounts");

        if (previousItems == null) {
            previousItems = new ArrayList<String>();
            previousTitles = new ArrayList<String>();
            previousCounts = new ArrayList<Integer>();
        }

        String action = request.getParameter("action");
        if (action != null) {
            if (action.equals("modifyAmount")) {
                int toBeModified = previousItems.indexOf(request.getParameter("movieId"));
                previousCounts.set(toBeModified, Integer.parseInt(request.getParameter("amount")));
            }
            else if (action.equals("deleteMovie")) {
                int toBeDeleted = previousItems.indexOf(request.getParameter("movieId"));
                previousItems.remove(toBeDeleted);
                previousTitles.remove(toBeDeleted);
                previousCounts.remove(toBeDeleted);
            }
        }

        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");
        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);
        JsonArray previousTitlesJsonArray = new JsonArray();
        previousTitles.forEach(previousTitlesJsonArray::add);
        responseJsonObject.add("previousTitles", previousTitlesJsonArray);
        JsonArray previousCountsJsonArray = new JsonArray();
        previousCounts.forEach(previousCountsJsonArray::add);
        responseJsonObject.add("previousCounts", previousCountsJsonArray);

        System.out.println(responseJsonObject);
        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("movieId");
        String title = request.getParameter("movieTitle");
        System.out.println(item);
        HttpSession session = request.getSession();

        System.out.println("here");
        System.out.println(title);

        // get the previous items in a ArrayList
        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
        ArrayList<String> previousTitles = (ArrayList<String>) session.getAttribute("previousTitles");
        ArrayList<Integer> previousCounts = (ArrayList<Integer>) session.getAttribute("previousCounts");
        if (previousItems == null) {
            previousItems = new ArrayList<String>();
            previousItems.add(item);
            session.setAttribute("previousItems", previousItems);
            previousTitles = new ArrayList<String>();
            previousTitles.add(title);
            session.setAttribute("previousTitles", previousTitles);
            previousCounts = new ArrayList<Integer>();
            previousCounts.add(1);
            session.setAttribute("previousCounts", previousCounts);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            if (previousItems.contains(item)) {
                int toBeAdded = previousItems.indexOf(request.getParameter("movieId"));
                previousCounts.set(toBeAdded, previousCounts.get(toBeAdded) + 1);
            }
            else {
//                synchronized (previousItems) {
//                }
//                synchronized (previousTitles) {
//                }
                previousItems.add(item);
                previousTitles.add(title);
                previousCounts.add(1);
            }
        }
        for (String mT : previousTitles) {
            System.out.println(mT);
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);
        JsonArray previousTitlesJsonArray = new JsonArray();
        previousTitles.forEach(previousTitlesJsonArray::add);
        responseJsonObject.add("previousTitles", previousTitlesJsonArray);
        JsonArray previousCountsJsonArray = new JsonArray();
        previousCounts.forEach(previousCountsJsonArray::add);
        responseJsonObject.add("previousCounts", previousCountsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
        System.out.println(responseJsonObject);
    }
}
