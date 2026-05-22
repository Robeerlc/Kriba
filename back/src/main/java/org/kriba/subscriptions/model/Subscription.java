package org.kriba.subscriptions.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String externalSourceId;

    @Column(nullable = false)
    private String sourceName;
}
