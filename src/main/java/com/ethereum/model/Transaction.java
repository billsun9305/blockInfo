package com.ethereum.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import jakarta.persistence.*;

import java.math.BigInteger;

@Entity
@Table(name = "transactions")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @Id
    @Column(name = "hash")
    private String hash;

    @Column(name = "block_number")
    private String blockNumber;

    @Column(name = "\"from\"")
    private String from;

    private String gas;

    @Column(name = "gas_price")
    private String gasPrice;

//    private String input;

    private String nonce;

    @Column(name = "\"to\"")
    private String to;

    @Column(name = "transaction_index")
    private String transactionIndex;

    private String value;

    private String type;

    private String v;

    private String r;

    private String s;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_hash", referencedColumnName = "hash", insertable = false, updatable = false)
    private Block block;
}