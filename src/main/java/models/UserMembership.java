package models;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import jakarta.persistence.Id;


import java.time.LocalDateTime;

@Entity
@Table(name = "user_memberships")
@Data
@NoArgsConstructor
public class UserMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "membership_id")
    private GymMembership membership;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
}