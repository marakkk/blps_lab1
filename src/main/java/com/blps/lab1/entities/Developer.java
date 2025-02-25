package com.blps.lab1.entities;

import com.blps.lab1.enums.DevAccount;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Developer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private boolean paymentProfile;

    @Enumerated(EnumType.STRING)
    private DevAccount accStatus;

    private double earnings;

    @OneToMany(mappedBy = "developer")
    private List<App> apps;
}