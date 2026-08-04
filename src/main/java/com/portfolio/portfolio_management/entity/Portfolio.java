package com.portfolio.portfolio_management.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolio")
@Getter
@Setter
@NoArgsConstructor
public class Portfolio {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long portfolioId;

        @Column(nullable = false)
        private String portfolioName;

        @OneToMany(mappedBy = "portfolio",
                cascade = CascadeType.ALL,
                orphanRemoval = true)
        @JsonManagedReference
        private List<Investment> investments = new ArrayList<>();
}

