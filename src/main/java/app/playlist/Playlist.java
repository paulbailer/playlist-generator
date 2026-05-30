package app.playlist;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String link;

    public Playlist() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Playlist that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(link, that.link);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name, link); }

    @Override
    public String toString() { return "Playlist{id=" + id + ", name='" + name + "', link='" + link + "'}"; }
}