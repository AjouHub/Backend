package sulhoe.ajouhub.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class User {
    @Id
    private Long id;

    private String email;
    private String department;

    @ManyToMany
    @JoinTable(
            name = "user_saved_notices",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "notice_id"))
    private Set<Notice> savedNotices = new HashSet<>();
}