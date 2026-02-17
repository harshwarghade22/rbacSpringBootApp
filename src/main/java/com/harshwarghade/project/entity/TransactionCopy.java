package com.harshwarghade.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions_copy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private LocalDateTime timestamp;

    private String type;

    @Column(name = "account_id")
    private Long accountId;

    // Prevent duplicate migration
    @Column(name = "source_txn_id", unique = true)
    private Long sourceTxnId;
}
