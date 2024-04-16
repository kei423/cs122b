import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XMLParser {
    private long startTime;
    private long endTime;

    private XMLParser() {
        startTime = 0;
        endTime = 0;
    }

    private void parse(boolean optimized) throws SQLException, ClassNotFoundException {
        if (!optimized) {
            runNormal();
        } else {
            runOptimized();
        }
    }

    private void runNormal() throws SQLException, ClassNotFoundException {
        XMLParserMovies parserMovies = new XMLParserMovies();
        parserMovies.run(false);
        HashMap<String, String> x = parserMovies.getMapXmlMovieIdToDbMovieId();

        XMLParserStars parserStars = new XMLParserStars(x);
        parserStars.run(false);
    }


    private void runOptimized() throws SQLException, ClassNotFoundException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        // XML parsing for movies
        XMLParserMovies parserMovies = new XMLParserMovies();
        Runnable moviesTask = () -> {
            try {
                parserMovies.run(true);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
        executorService.submit(moviesTask);

        // XML parsing for stars
        HashMap<String, String> movieIds = parserMovies.getMapXmlMovieIdToDbMovieId(); // Assuming getMovieIds() is a getter method

        XMLParserStars parserStars = new XMLParserStars(movieIds);
        Runnable starsTask = () -> {
            try {
                parserStars.run(true);
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
        executorService.submit(starsTask);

        // Shutdown the executor service when all tasks are completed
        executorService.shutdown();

    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        boolean optimized = true;
        XMLParser x = new XMLParser();
        x.parse(optimized);
    }
}
