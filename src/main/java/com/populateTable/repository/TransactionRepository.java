package com.populateTable.repository;

import com.populateTable.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByFromAddress(String fromAddress);

    List<Transaction> findByToAddress(String toAddress);
}
