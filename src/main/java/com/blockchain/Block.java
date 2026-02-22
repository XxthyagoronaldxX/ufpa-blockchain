package com.blockchain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representa um bloco na blockchain com suporte a Proof of Work
 */
public class Block {
    private static final Logger logger = LoggerFactory.getLogger(Block.class);
    private int index;
    private long timestamp;
    private List<Transaction> transactions;
    private String previousHash;
    private int nonce;
    private String hash;
    
    /**
     * Construtor para criar um novo bloco (antes da mineração)
     */
    public Block(int index, List<Transaction> transactions, String previousHash) {
        this.index = index;
        this.timestamp = System.currentTimeMillis();
        this.transactions = new ArrayList<>(transactions);
        this.previousHash = previousHash;
        this.nonce = 0;
        this.hash = "";
    }
    
    /**
     * Construtor completo (para desserialização)
     */
    public Block(int index, long timestamp, List<Transaction> transactions, 
                 String previousHash, int nonce, String hash) {
        this.index = index;
        this.timestamp = timestamp;
        this.transactions = new ArrayList<>(transactions);
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.hash = hash;
    }
    
    /**
     * Calcula o hash SHA-256 do bloco
     */
    public String calculateHash() {
        String data = index + timestamp + transactionsToString() + previousHash + nonce;
        return applySHA256(data);
    }
    
    /**
     * Aplica SHA-256 a uma string
     */
    private String applySHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao calcular SHA-256", e);
        }
    }
    
    /**
     * Implementa o Proof of Work: encontra um nonce que resulte 
     * em um hash começando com a dificuldade especificada
     */
    public void mineBlock(String difficulty) {
        String target = new String(new char[difficulty.length()]).replace('\0', '0');
        
        // Calculate initial hash before checking
        hash = calculateHash();
        
        while (!hash.substring(0, difficulty.length()).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        
        logger.debug("Bloco minerado: " + hash);
    }
    
    /**
     * Verifica se o hash do bloco é válido
     */
    public boolean isValid(String difficulty) {
        String calculatedHash = calculateHash();
        String target = new String(new char[difficulty.length()]).replace('\0', '0');
        
        // Check if hash has sufficient length
        if (hash == null || hash.length() < difficulty.length()) {
            return false;
        }
        
        return calculatedHash.equals(hash) && 
               hash.substring(0, difficulty.length()).equals(target);
    }
    
    /**
     * Converte lista de transações para string
     */
    private String transactionsToString() {
        return transactions.stream()
            .map(t -> t.getSender() + t.getRecipient() + t.getAmount() + t.getTimestamp())
            .collect(Collectors.joining());
    }
    
    /**
     * Converte o bloco para Map para serialização JSON
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("index", index);
        map.put("timestamp", timestamp);
        map.put("transactions", transactions.stream()
            .map(Transaction::toMap)
            .collect(Collectors.toList()));
        map.put("previousHash", previousHash);
        map.put("nonce", nonce);
        map.put("hash", hash);
        return map;
    }
    
    /**
     * Cria um Block a partir de um Map (deserialização)
     */
    @SuppressWarnings("unchecked")
    public static Block fromMap(Map<String, Object> map) {
        int index = ((Number) map.get("index")).intValue();
        long timestamp = ((Number) map.get("timestamp")).longValue();
        
        List<Map<String, Object>> transactionMaps = (List<Map<String, Object>>) map.get("transactions");
        List<Transaction> transactions = transactionMaps.stream()
            .map(Transaction::fromMap)
            .collect(Collectors.toList());
        
        String previousHash = (String) map.get("previousHash");
        int nonce = ((Number) map.get("nonce")).intValue();
        String hash = (String) map.get("hash");
        
        return new Block(index, timestamp, transactions, previousHash, nonce, hash);
    }
    
    // Getters
    public int getIndex() {
        return index;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }
    
    public String getPreviousHash() {
        return previousHash;
    }
    
    public int getNonce() {
        return nonce;
    }
    
    public String getHash() {
        return hash;
    }
    
    @Override
    public String toString() {
        return String.format("Block{index=%d, timestamp=%d, txCount=%d, previousHash='%s', nonce=%d, hash='%s'}",
            index, timestamp, transactions.size(), previousHash, nonce, hash);
    }
}
