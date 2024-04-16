import java.util.Objects;

public class Star {
    private String id;
    private String name;
    private Integer birthYear;

    public Star() { }

    public Star(String id, String name, int birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public Integer getBirthYear() {return birthYear;}
    public void setBirthYear(Integer birthYear) {this.birthYear = birthYear;}

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Movie Details - ");
        sb.append("Id:" + getId());
        sb.append(", ");
        sb.append("Name:" + getName());
        sb.append(", ");
        sb.append("BirthYear:" + getBirthYear());
        sb.append(".");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Star star = (Star) o;
        return Objects.equals(name, star.name) &&
                Objects.equals(birthYear, star.birthYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthYear);
    }
}
