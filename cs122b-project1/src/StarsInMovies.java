import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class StarsInMovies {
    private String starId;
    private String movieId;

    public StarsInMovies() { }

    public StarsInMovies(String starId, String movieId) {
        this.starId = starId;
        this.movieId = movieId;
    }

    public String getStarId() {return starId;}
    public void setStarId(String starId) {this.starId = starId;}
    public String getMovieId() {return movieId;}
    public void setMovieId(String movieId) {this.movieId = movieId;}

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("StarId:").append(getStarId());
        sb.append(", ");
        sb.append("MovieId:").append(getMovieId());
        sb.append(".");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StarsInMovies sim = (StarsInMovies) o;
        return Objects.equals(starId, sim.starId) &&
                Objects.equals(movieId, sim.movieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(starId, movieId);
    }
}
