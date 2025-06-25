package sulhoe.ajouhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
public class Notice {

    @Id
    private String id;

    private String number;
    private String category;
    private String title;
    private String department;
    private String date;
    private String link;

    @ManyToMany(mappedBy = "savedNotices")
    private Set<User> savedByUsers = new HashSet<>();

    // Default constructor (for using JPA)
    protected Notice() {}

    public Notice(String number, String category, String title, String department, String date, String link) {
        this.number = number;
        this.category = category;
        this.title = title;
        this.department = department;
        this.date = date;
        this.link = link;
    }
}