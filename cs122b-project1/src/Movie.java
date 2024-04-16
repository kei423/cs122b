import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String id;
    private String title;
    private int year;
    private String director;
    private final List<String> genreList;
    private List<String> starList;

    public Movie() {
        genreList = new ArrayList<>();
    }

    public Movie(String id, String title, int year, String director) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        genreList = new ArrayList<>();
        starList = new ArrayList<>();
    }

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public int getYear() {return year;}

    public void setYear(int year) {this.year = year;}

    public String getDirector() {return director;}

    public void setDirector(String director) {this.director = director;}

    public void addGenre(String genre) {genreList.add(genre);}

    public List<String> getGenres() {return genreList;}

    public void addStar(String star) {starList.add(star);}

    public List<String> getStars() {return starList;}

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("ID:" + getId());
        sb.append(", ");
        sb.append("Title:" + getTitle());
        sb.append(", ");
        sb.append("Year:" + getYear());
        sb.append(", ");
        sb.append("Director:" + getDirector());
        sb.append(", ");
        sb.append("Genres:" + getGenres());
        sb.append(".");
        return sb.toString();
    }
}
