package com.ethereum.service;

/**
 * Scheduled service to fetch blocks.
 */
import com.ethereum.model.Block;
import com.ethereum.model.Transaction;
import com.ethereum.repository.BlockRepository;
import com.ethereum.repository.TransactionRepository;
import com.ethereum.service.strategies.DataFetchingStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class EthereumRpcService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    private DataFetchingStrategy fetchingStrategy;

    @Autowired
    private ApplicationContext context; // Injecting the ApplicationContext to access beans dynamically

    private WebClient webClient;

    private long startBlockNumber;

    private volatile boolean isInitialSyncComplete = false;

    public record EthRequest(String jsonrpc, String method, List<Object> params, int id) {}
    public record EthBlockResponse(String jsonrpc, int id, JsonNode result) {}
    Logger logger = LoggerFactory.getLogger(EthereumRpcService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public EthereumRpcService(WebClient.Builder webClientBuilder, BlockRepository blockRepository) throws JsonProcessingException {
        this.webClient = webClientBuilder.baseUrl("https://data-seed-prebsc-1-s1.binance.org:8545/").build();
        this.blockRepository = blockRepository;
//        this.startBlockNumber = fetchLatestBlockNumberFromRpc() - 200;
        this.startBlockNumber =  35447221 - 400;
    }

    @PostConstruct
    public void init() throws JsonProcessingException {
        long startTime = System.currentTimeMillis();

        initialBlockSynchronization();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.info("initialBlockSynchronization() completed in {} ms", duration);
    }

    @PostConstruct  // This will run just after the bean is initialized
    public void initialBlockSynchronization() throws JsonProcessingException {
        Long latestBlockInDb = findLatestBlockNumberFromDB();

        // If the database is empty or has blocks less than our desired start number
        if (latestBlockInDb == null || latestBlockInDb < startBlockNumber) {
            latestBlockInDb = startBlockNumber;
        }
        logger.info("initialBlockSynchronization() latestBlockInDb: {}", latestBlockInDb);

        long latestBlockFromRpc = fetchLatestBlockNumberFromRpc();
        logger.info("initialBlockSynchronization() latestBlockFromRpcBigInt: {}", latestBlockFromRpc);

        while (latestBlockInDb < latestBlockFromRpc && fetchAndStoreBlock(latestBlockInDb)) {
            logger.info("getting block sync with: {}", latestBlockInDb);
            latestBlockInDb++;
        }
        isInitialSyncComplete = true;
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
        logger.info("latestBlockFromRpc: {}", latestBlockFromRpc);

        while (nextBlockToFetch < latestBlockFromRpc && fetchAndStoreBlock(nextBlockToFetch)) {
            logger.info("fetching to latest block. Latest block is: {}", latestBlockFromRpc);
            nextBlockToFetch++;
            logger.info("Next block to fetch: {}", nextBlockToFetch);

        }
    }

    @Transactional
    public boolean fetchAndStoreBlock(long blockNumber) {
        try {
            long startRpcFetchBlockTime = System.currentTimeMillis();

            String blockToFetch = "0x" + Long.toHexString(blockNumber);
            logger.info("Fetching block: {}", blockToFetch);

            setFetchingStrategy("SpecificBlockFetchingStrategy");
            EthBlockResponse response = fetchData("0x" + Long.toHexString(blockNumber));


            long endRpcFetchBlockTime = System.currentTimeMillis();
            long duration = endRpcFetchBlockTime - startRpcFetchBlockTime;
            logger.info("fetching block number {} with Hex {} completed in {} ms", blockNumber, blockToFetch, duration);


            long startStoringBlockTime = System.currentTimeMillis();
            if (response != null && response.result() != null) {
                Block block = new Block();
                block.setHash(response.result().get("hash").asText());
                block.setParentHash(response.result().get("parentHash").asText());
                block.setNumber(response.result().get("number").asText());
                block.setTimestamp(response.result().get("timestamp").asText());

                List<Transaction> blockTransactions = new ArrayList<>();
                JsonNode transactions = response.result().get("transactions");
                for (JsonNode node : transactions) {
                    String jsonTransaction = objectMapper.writeValueAsString(node);
                    Transaction transaction = objectMapper.readValue(jsonTransaction, Transaction.class);
                    transaction.setBlock(block); // Associate the transaction with the block
                    blockTransactions.add(transaction);
                }

                block.setTransactions(blockTransactions);
                blockRepository.save(block);

                long endStoringBlockTime = System.currentTimeMillis();
                long StoreDuration = endStoringBlockTime - startStoringBlockTime;
                logger.info("Storing block number {} with Hex {} completed in {} ms", blockNumber, blockToFetch, StoreDuration);
                return true;
            }
        } catch (JsonProcessingException e) {
            logger.info("Fail to save block.");
            logger.info(String.valueOf(e));
        }
        return false;
    }

    private long fetchLatestBlockNumberFromRpc() throws JsonProcessingException {
        setFetchingStrategy("latestBlockFetchingStrategy");
        EthBlockResponse response = fetchData("latest");

        String numberStr = objectMapper.writeValueAsString(response.result().get("number")).replace("\"", "");
        logger.info("latest block from RPC: {}", numberStr);

        long number = Long.parseLong(numberStr.substring(2), 16);

        return number;
    }

    public Long findLatestBlockNumberFromDB() {
        String latestBlockHex = blockRepository.findLatestBlockNumberAsHex();
        if (latestBlockHex != null && latestBlockHex.startsWith("0x")) {
            return Long.parseLong(latestBlockHex.substring(2), 16);
        }
        return null;
    }

    public void setFetchingStrategy(String strategyBeanName) {
        DataFetchingStrategy dataFetchingStrategy = context.getBean(strategyBeanName, DataFetchingStrategy.class);
        dataFetchingStrategy.setWebClient(this.webClient);
        this.fetchingStrategy = dataFetchingStrategy;
    }

    public EthBlockResponse fetchData(String blockNumberOrLatest) {
        if (fetchingStrategy == null) {
            throw new IllegalStateException("Fetching strategy has not been set");
        }
        EthBlockResponse ethBlockResponse = fetchingStrategy.fetchData(blockNumberOrLatest);
//        logger.info("Getting response from fetch data, response: {}", ethBlockResponse.result().toPrettyString());
        return ethBlockResponse;
    }
}
