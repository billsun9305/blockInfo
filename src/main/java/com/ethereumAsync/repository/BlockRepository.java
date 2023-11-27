package com.ethereumAsync.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<com.ethereumAsync.model.Block, Long> {

    @Query("SELECT b FROM Block b ORDER BY b.blockNum DESC")
    List<com.ethereumAsync.model.Block> findLatestBlocks();
}
