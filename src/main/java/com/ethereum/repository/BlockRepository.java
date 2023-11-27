package com.ethereum.repository;

import com.ethereum.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, String> {

//    @Query(value = "SELECT CAST(SUBSTRING(b.number FROM 2) AS BIGINT) FROM blocks b ORDER BY b.number DESC LIMIT 1", nativeQuery = true)
    @Query(value = "SELECT b.number FROM blocks b ORDER BY b.number DESC LIMIT 1", nativeQuery = true)
    String findLatestBlockNumberAsHex();

    @Query(value = "SELECT b FROM Block b WHERE b.number = ?1")
    List<Block> findBlockByNumber(String number);
}
