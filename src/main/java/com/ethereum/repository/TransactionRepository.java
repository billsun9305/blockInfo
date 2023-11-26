package com.ethereum.repository;

import com.ethereum.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByFrom(String fromAddress);

    List<Transaction> findByTo(String toAddress);
}
