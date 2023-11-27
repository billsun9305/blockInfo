package com.ethereum.controller;

import com.ethereum.model.Block;
import com.ethereum.service.EthereumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EthereumController {

    @Autowired
    private EthereumService ethereumService;

    @GetMapping("/blocks")
    public List<Block> getLatestNBlocks(@RequestParam("limit") int limit) {
        return ethereumService.getLatestNBlocks(limit);
    }

    @GetMapping("/blocks/{id}")
    public List<Block> getBlockById(@PathVariable String id) {
        return ethereumService.getBlockById(id);
    }

//    @GetMapping("/transaction/{txHash}")
//    public Transaction getTransactionByTxHash(@PathVariable String txHash) {
//        return ethereumService.getTransactionByTxHash(txHash);
//    }
}
