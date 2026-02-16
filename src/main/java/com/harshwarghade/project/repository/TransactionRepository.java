package com.harshwarghade.project.repository;

import com.harshwarghade.project.entity.Transaction;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountId(Long accountId);

    Page<Transaction> findByAccountId(Long accountId, org.springframework.data.domain.Pageable pageable);
}
