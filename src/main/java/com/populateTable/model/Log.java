package com.populateTable.model;

import com.ethereum.model.Transaction;
import lombok.Data;
import jakarta.persistence.*;

@Entity
@Table(name = "logs")
@Data
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long indexId;

    @ManyToOne
    @JoinColumn(name = "tx_hash")
    private Transaction transaction;

    private String data;
}
