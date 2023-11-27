package com.ethereum.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "blocks")
public class Block {

    @Id
    private String hash;

    @Column(name = "parent_hash")
    private String parentHash;

    @Column(name = "sha3_uncles")
    private String sha3Uncles;

    private String miner;

    @Column(name = "state_root")
    private String stateRoot;

    @Column(name = "transactions_root")
    private String transactionsRoot;

    @Column(name = "receipts_root")
    private String receiptsRoot;

    @Column(name = "logs_bloom")
    private String logsBloom;

    private String difficulty;

    private String number;

    @Column(name = "gas_limit")
    private String gasLimit;

    @Column(name = "gas_used")
    private String gasUsed;

    private String timestamp;

    @Column(name = "extra_data")
    private String extraData;

    @Column(name = "mix_hash")
    private String mixHash;

    private String nonce;

    private Integer size;

    @Column(name = "total_difficulty")
    private String totalDifficulty;

    @Column(name = "base_fee_per_gas")
    private String baseFeePerGas;

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public void setSha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    public void setReceiptsRoot(String receiptsRoot) {
        this.receiptsRoot = receiptsRoot;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public void setMixHash(String mixHash) {
        this.mixHash = mixHash;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setTotalDifficulty(String totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public void setBaseFeePerGas(String baseFeePerGas) {
        this.baseFeePerGas = baseFeePerGas;
    }

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();
}
