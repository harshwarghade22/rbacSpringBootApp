package com.harshwarghade.project.repository;

import com.harshwarghade.project.entity.TransactionCopy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionCopyRepository extends JpaRepository<TransactionCopy, Long> {
}
