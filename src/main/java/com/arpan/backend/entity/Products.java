package com.arpan.backend.entity;


import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "products")
@Builder
@ToString(exclude = {"imageData", "user"})
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prodId;

    private int stockQuantity;
    private BigDecimal price;
    private String name;
    private String description;
    private String brand;
    private String category;
    private boolean productAvailable;
    private LocalDateTime releaseDate;

    //Image
    private String imageName;
    private String imageType;
    // Large Object
    @Lob
    @Column(name = "image_data")
    @Basic(fetch = FetchType.LAZY)
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;


}
