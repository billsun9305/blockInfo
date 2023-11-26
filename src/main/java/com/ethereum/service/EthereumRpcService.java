package com.ethereum.service;

/**
 * Scheduled service to fetch blocks.
 */
import com.ethereum.model.Block;
import com.ethereum.model.Transaction;
import com.ethereum.repository.BlockRepository;
import com.ethereum.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final WebClient webClient;

//    private final long startBlockNumber = Long.parseLong("1F8A16F", 16);
    private long startBlockNumber;
    public record EthRequest(String jsonrpc, String method, List<Object> params, int id) {}
    public record EthBlockResponse(String jsonrpc, int id, JsonNode result) {}
    public record Result(
            String hash,
            String parentHash,
            String sha3Uncles,
            String miner,
            String stateRoot,
            String transactionsRoot,
            String receiptsRoot,
            String logsBloom,
            String difficulty,
            String number,
            String gasLimit,
            String gasUsed,
            String timestamp,
            String extraData,
            String mixHash,
            String nonce,
            String size,
            String totalDifficulty,
            List<Transaction> transactions
            ) {}
    Logger logger = LoggerFactory.getLogger(EthereumRpcService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @PostConstruct
    public void init() throws JsonProcessingException {
        initialBlockSynchronization();
    }


    public EthereumRpcService(WebClient.Builder webClientBuilder, BlockRepository blockRepository) throws JsonProcessingException {
        this.webClient = webClientBuilder.baseUrl("https://data-seed-prebsc-1-s1.binance.org:8545/").build();
        this.blockRepository = blockRepository;
        this.startBlockNumber = fetchLatestBlockNumberFromRpc() - 200;
    }

    @PostConstruct  // This will run just after the bean is initialized
    public void initialBlockSynchronization() throws JsonProcessingException {
        Long latestBlockInDb = findLatestBlock();

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
    }
    @Transactional
    public boolean fetchAndStoreBlock(long blockNumber) {
        try {
            String blockToFetch = "0x" + Long.toHexString(blockNumber);
            logger.info("Fetching block: {}", blockToFetch);

            EthBlockResponse response = webClient.post()
                    .bodyValue(new EthRequest("2.0", "eth_getBlockByNumber", List.of(blockToFetch, true), 1))
                    .retrieve()
                    .bodyToMono(EthBlockResponse.class)
                    .block();

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
                blockRepository.save(block); // This should cascade and save transactions as well
                return true;
            }
        } catch (JsonProcessingException e) {
            logger.info("Fail to save block.");
            logger.info(String.valueOf(e));
        }
        return false;
    }
    private long fetchLatestBlockNumberFromRpc() throws JsonProcessingException {
        EthBlockResponse response = webClient.post()
                .bodyValue(new EthRequest("2.0", "eth_getBlockByNumber", List.of("latest", false), 1))
                .retrieve()
                .bodyToMono(EthBlockResponse.class)
                .block();

        String numberStr = objectMapper.writeValueAsString(response.result().get("number")).replace("\"", "");
        logger.info("latest block from RPC: {}", numberStr);

        long number = Long.parseLong(numberStr.substring(2), 16);

        return number;
    }

    @Scheduled(fixedRate = 5000)  // 5000ms = 5 seconds
    public void regularBlockUpdate() throws JsonProcessingException {
        Long latestBlockInDb = findLatestBlock();

        logger.info("regularBlockUpdate latestBlockInDbBigInt: {}", latestBlockInDb);

        if (latestBlockInDb == null) {
            latestBlockInDb = startBlockNumber;
        }
        long nextBlockToFetch = latestBlockInDb + 1;
        logger.info("nextBlockToFetch: {}", nextBlockToFetch);

        long latestBlockFromRpc = fetchLatestBlockNumberFromRpc();
        logger.info("latestBlockFromRpc: {}", latestBlockFromRpc);

        while (nextBlockToFetch < latestBlockFromRpc && fetchAndStoreBlock(nextBlockToFetch)) {
            logger.info("fetching latest block: " + latestBlockFromRpc + "Next block to fetch: " + nextBlockToFetch);
            nextBlockToFetch++;
        }
    }
    public Long findLatestBlock() {
        String latestBlockHex = blockRepository.findLatestBlockNumberAsHex();
        if (latestBlockHex != null && latestBlockHex.startsWith("0x")) {
            return Long.parseLong(latestBlockHex.substring(2), 16);
        }
        return null; // or some default value, depending on how you want to handle this scenario
    }

}
