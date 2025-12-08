package com.example.KutupahaneOtomasyonu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fine_id")
    private Integer fineId;

    @OneToOne
    @JoinColumn(name = "borrow_id", nullable = false)
    private Borrowing borrowing;

    @Column(name = "fine_amount")
    private Double fineAmount;

    @Column(name = "fine_date")
    private LocalDateTime fineDate;

    private boolean paid;
}