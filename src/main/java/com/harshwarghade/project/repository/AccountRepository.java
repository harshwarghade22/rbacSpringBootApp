package com.harshwarghade.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.harshwarghade.project.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    // 🔥 Optimized for admin view (fetch user with account)
    @Query("SELECT a FROM Account a JOIN FETCH a.user")
    List<Account> findAllWithUser();

    Account findByAccountNumber(String accountNumber);
}

