package com.pbop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @Column(length = 200,nullable = false)
    private String title;

    @Column(length = 2000,nullable = false)
    private String reviewText;

    @Column(nullable = true)
    private int rating;
}
