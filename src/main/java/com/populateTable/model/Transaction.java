package com.populateTable.model;

import com.ethereum.model.Block;
import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    private String txHash;

    @ManyToOne
    @JoinColumn(name = "block_num")
    private Block block;

    private String fromAddress;
    private String toAddress;
    private Long nonce;
    private String data;
    private String value;
}
