package models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import jakarta.persistence.Id;


import java.util.List;

@Entity
@Table(name = "trainers")
@Data
@NoArgsConstructor
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String bio;
    private String photoUrl;

    @ManyToOne
    @JoinColumn(name = "specialization_id")
    private TrainerSpecialization specialization;

    @OneToMany(mappedBy = "trainer")
    private List<Users> clients;
}

