package com.populateTable.repository;

import com.populateTable.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByTransaction_TxHash(String txHash);
}
