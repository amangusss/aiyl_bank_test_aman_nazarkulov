package com.github.amangusss.aiyl_bank_test_aman_nazarkulov.entity.transfer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "transfer")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Transfer {

    @Id
    @Column(name = "transfer_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    Long id;

    @Column(name = "sender_id")
    Long senderId;

    @Column(name = "receiver_id")
    Long receiverId;

    @Column(name = "amount")
    double amount;

    @Column(name = "currency")
    String currency;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    TransferStatus status;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
