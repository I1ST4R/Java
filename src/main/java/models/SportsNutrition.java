package models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;


@Entity
@Table(name = "sports_nutrition")
@Data
@NoArgsConstructor
public class SportsNutrition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private NutritionCategory category;
}