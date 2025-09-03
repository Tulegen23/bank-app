package kz.nurbank.practice.customer_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "customer_activity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = false)
    private ZonedDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;
}