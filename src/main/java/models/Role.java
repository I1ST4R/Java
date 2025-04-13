package models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;


@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name;
    public int getId() {
        return id;
    }

    public ERole getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(ERole name) {
        this.name = name;
    }
}