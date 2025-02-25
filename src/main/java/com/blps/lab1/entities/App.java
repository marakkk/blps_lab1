package com.blps.lab1.entities;

import com.blps.lab1.enums.AppStatus;
import com.blps.lab1.enums.MonetizationType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class App {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private double version;

    @Enumerated(EnumType.STRING)
    private AppStatus status;

    private int downloads;
    private double revenue;

    private boolean inAppPurchases;
    private boolean isNotFree;
    private double appPrice;

    @Enumerated(EnumType.STRING)
    private MonetizationType monetizationType;

    private boolean correctPermissions;

    private boolean correctMetadata;

    private boolean isViolatesGooglePlayPolicies;
    private boolean isChildrenBadPolicy;

    @ManyToOne
    @JoinColumn(name = "developer_id")
    private Developer developer;
}
