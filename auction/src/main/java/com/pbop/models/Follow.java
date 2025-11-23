package com.pbop.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "user_follow", // Optional: specify table name
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"follower_id", "followee_id"}) // <--- CRITICAL ADDITION
        }
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id",nullable = false)
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id",nullable=false)
    private User followee;

    @Column(nullable = false,updatable = false)
    private LocalDateTime followTimestamp = LocalDateTime.now();
}
