package com.populateTable.service;

import com.ethereum.model.Block;
import com.ethereum.model.Transaction;
import com.ethereum.repository.BlockRepository;
import com.ethereum.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EthereumService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Block> getLatestNBlocks(int limit) {
        // For simplicity, fetching all and then limiting,
        // but in production, you'd use a query to limit records
        List<Block> blocks = blockRepository.findAll();
        if(blocks.size() > limit) {
            return blocks.subList(0, limit);
        }
        return blocks;
    }

    public Block getBlockById(Long id) {
        Optional<Block> block = blockRepository.findById(id);
        return block.orElse(null); // return block or null if not found
    }

    public Transaction getTransactionByTxHash(String txHash) {
        Optional<Transaction> transaction = transactionRepository.findById(txHash);
        return transaction.orElse(null); // return transaction or null if not found
    }
}
