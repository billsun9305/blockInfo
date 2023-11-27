package com.ethereumAsync.model;

import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "blocks")
@Data
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long blockNum;
    private String blockHash;
    private Long blockTime;
    private String parentHash;
}
