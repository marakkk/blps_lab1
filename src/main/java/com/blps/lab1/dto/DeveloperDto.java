package com.blps.lab1.dto;

import com.blps.lab1.enums.DevAccount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeveloperDto {
    private Long id;
    private String name;
    private String email;
    private boolean paymentProfile;
    private DevAccount accStatus;
    private double earnings;
    private List<AppDto> apps;
}
