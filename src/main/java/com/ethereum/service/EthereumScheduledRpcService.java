package com.ethereum.service;

import com.ethereum.repository.BlockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Scheduled service to fetch blocks.
 */
@Service
public class EthereumScheduledRpcService extends EthereumRpcService {
    public EthereumScheduledRpcService(WebClient.Builder webClientBuilder, BlockRepository blockRepository) throws JsonProcessingException {
        super(webClientBuilder, blockRepository);
    }

    @Scheduled(fixedRate = 5000)  // 5000ms = 5 seconds
    public void regularBlockUpdate() throws JsonProcessingException {
        if (!isInitialSyncComplete) {
            return;
        }

        Long latestBlockInDb = findLatestBlockNumberFromDB();
        logger.info("regularBlockUpdate latestBlockInDbBigInt: {}", latestBlockInDb);

        if (latestBlockInDb == null) {
            latestBlockInDb = startBlockNumber;
        }
        long nextBlockToFetch = latestBlockInDb + 1;
        logger.info("nextBlockToFetch: {}", nextBlockToFetch);

        long latestBlockFromRpc = fetchLatestBlockNumberFromRpc();
        logger.info("regularBlockUpdate() latestBlockFromRpc: {}", latestBlockFromRpc);

        while (nextBlockToFetch <= latestBlockFromRpc && fetchAndStoreBlock(nextBlockToFetch)) {
            logger.info("fetching to latest block. Latest block is: {}", latestBlockFromRpc);
            nextBlockToFetch++;
            logger.info("Next block to fetch: {}", nextBlockToFetch);

        }
    }
}

