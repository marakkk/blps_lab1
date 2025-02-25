package com.blps.lab1.repo;

import com.blps.lab1.entities.App;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRepository extends JpaRepository<App, Long> {}
