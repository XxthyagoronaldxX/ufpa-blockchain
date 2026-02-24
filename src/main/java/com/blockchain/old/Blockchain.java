package com.blockchain.old;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blockchain.utils.ProtocolUtil;

/**
 * Gerencia a blockchain completa e o pool de transações pendentes
 * Implementa as regras conforme especificações do trabalho:
 * - Bloco gênesis fixo
 * - Proof of Work com dificuldade fixa
 * - Validação de saldo (não permitir saldo negativo)
 * - Chain mais longa prevalece (consenso)
 */
public class Blockchain {
    private static final Logger logger = LoggerFactory.getLogger(Blockchain.class);
    private final List<Transaction> pendingTransactions;
    private List<Block> chain;
    private String difficulty;

    public Blockchain() {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = ProtocolUtil.DIFFICULTY;

        // Cria o bloco gênesis
        createGenesisBlock();
    }

    /**
     * Cria o primeiro bloco da blockchain (Bloco Gênesis fixo)
     * Conforme especificação: deve existir um bloco gênesis fixo
     */
    private void createGenesisBlock() {
        List<Transaction> genesisTransactions = new ArrayList<>();
        // Bloco gênesis vazio - início da blockchain

        Block genesis = new Block(0, genesisTransactions, "0");
        genesis.mineBlock(difficulty);
        chain.add(genesis);

        logger.info("Bloco Gênesis criado: {}", genesis.getHash());
    }

    /**
     * Retorna o último bloco da chain
     */
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    /**
     * Adiciona uma nova transação ao pool de pendentes
     * Valida as regras:
     * - Valores devem ser positivos
     * - Não permitir saldo negativo em nenhuma hipótese
     */
    public void addTransaction(Transaction transaction) {
        // Validação básica
        if (transaction.getSender() == null || transaction.getRecipient() == null) {
            throw new IllegalArgumentException("Transação deve ter remetente e destinatário");
        }

        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }

        // REGRA CRÍTICA: Não permitir saldo negativo em nenhuma hipótese
        // Calcula o saldo após aplicar todas as transações pendentes
        double currentBalance = getBalance(transaction.getSender());

        // Subtrai as transações pendentes do mesmo sender
        for (Transaction pending : pendingTransactions) {
            if (pending.getSender().equals(transaction.getSender())) {
                currentBalance -= pending.getAmount();
            }
        }

        // Verifica se haverá saldo suficiente
        if (currentBalance - transaction.getAmount() < 0) {
            throw new IllegalArgumentException(
                    String.format("Saldo insuficiente! Saldo atual: %.2f, Tentando enviar: %.2f",
                            currentBalance, transaction.getAmount()));
        }

        pendingTransactions.add(transaction);
        logger.debug("Transação adicionada: {}", transaction);
    }

    /**
     * Minera um novo bloco com as transações pendentes
     * SEM recompensa de mineração (não especificada no trabalho)
     */
    public Block minePendingTransactions() {
        if (pendingTransactions.isEmpty()) {
            throw new IllegalStateException("Não há transações pendentes para minerar");
        }

        // Cria novo bloco com transações pendentes
        Block newBlock = new Block(
                chain.size(),
                new ArrayList<>(pendingTransactions),
                getLatestBlock().getHash());

        // Minera o bloco (Proof of Work)
        logger.info("Minerando bloco {}...", newBlock.getIndex());
        newBlock.mineBlock(difficulty);

        // Adiciona à chain
        chain.add(newBlock);

        // Limpa transações pendentes
        pendingTransactions.clear();

        return newBlock;
    }

    /**
     * Adiciona um bloco já minerado à chain (vindo da rede)
     * Valida antes de aceitar conforme especificação
     */
    public boolean addBlock(Block block) {
        // Verifica se é o próximo bloco sequencial
        if (block.getIndex() != chain.size()) {
            logger.warn("Índice de bloco inválido");
            return false;
        }

        // Verifica se aponta para o bloco anterior correto
        if (!block.getPreviousHash().equals(getLatestBlock().getHash())) {
            logger.warn("Hash do bloco anterior não corresponde");
            return false;
        }

        // Verifica se o bloco é válido (PoW)
        if (!block.isValid(difficulty)) {
            logger.warn("Bloco inválido - PoW falhou");
            return false;
        }

        // Remove transações que já estão no bloco do pool de pendentes
        for (Transaction tx : block.getTransactions()) {
            pendingTransactions.removeIf(pending -> pending.getId().equals(tx.getId()));
        }

        chain.add(block);
        logger.info("Bloco adicionado: {}", block.getHash());
        return true;
    }

    /**
     * Verifica se toda a blockchain é válida
     * Conforme especificação: cadeia válida possui todos os blocos válidos
     */
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

            // Verifica se o hash do bloco é válido
            if (!currentBlock.isValid(difficulty)) {
                logger.warn("Bloco {} inválido", i);
                return false;
            }

            // Verifica se aponta para o bloco anterior correto
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                logger.warn("Bloco {} não aponta para o anterior", i);
                return false;
            }
        }

        return true;
    }

    /**
     * Substitui a chain atual por uma nova (sincronização)
     * Consenso: cadeia mais longa prevalece
     */
    public boolean replaceChain(List<Block> newChain) {
        // Verifica se a nova chain é mais longa
        if (newChain.size() <= chain.size()) {
            logger.debug("Chain recebida não é mais longa");
            return false;
        }

        // Valida a nova chain
        Blockchain tempBlockchain = new Blockchain();
        tempBlockchain.chain = new ArrayList<>(newChain);

        if (!tempBlockchain.isChainValid()) {
            logger.warn("Chain recebida é inválida");
            return false;
        }

        // Substitui a chain
        chain = new ArrayList<>(newChain);
        logger.info("Chain substituída com sucesso");
        return true;
    }

    /**
     * Calcula o saldo de um endereço
     * Usado para validar transações (não permitir saldo negativo)
     */
    public double getBalance(String address) {
        double balance = 0.0;

        for (Block block : chain) {
            for (Transaction tx : block.getTransactions()) {
                if (tx.getRecipient().equals(address)) {
                    balance += tx.getAmount();
                }
                if (tx.getSender().equals(address)) {
                    balance -= tx.getAmount();
                }
            }
        }

        return balance;
    }

    /**
     * Cria saldo inicial para endereços (útil para testes)
     * Simula "mineração prévia" ou "funding inicial"
     */
    public void fundAddress(String address, double amount) {
        List<Transaction> fundingTx = new ArrayList<>();
        fundingTx.add(new Transaction("GENESIS_FUND", address, amount));

        Block fundingBlock = new Block(chain.size(), fundingTx, getLatestBlock().getHash());
        fundingBlock.mineBlock(difficulty);
        chain.add(fundingBlock);

        logger.info("Endereço {} recebeu funding de {}", address, amount);
    }

    /**
     * Converte a blockchain para List<Map> para serialização JSON
     */
    public List<Map<String, Object>> toMapList() {
        return chain.stream()
                .map(Block::toMap)
                .collect(Collectors.toList());
    }

    /**
     * Cria uma Blockchain a partir de uma List<Map> (deserialização)
     */
    public static Blockchain fromMapList(List<Map<String, Object>> mapList) {
        Blockchain blockchain = new Blockchain();
        blockchain.chain.clear(); // Remove o bloco gênesis

        for (Map<String, Object> blockMap : mapList) {
            blockchain.chain.add(Block.fromMap(blockMap));
        }

        return blockchain;
    }

    // Getters
    public List<Block> getChain() {
        return new ArrayList<>(chain);
    }

    public List<Transaction> getPendingTransactions() {
        return new ArrayList<>(pendingTransactions);
    }

    public int getChainLength() {
        return chain.size();
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Blockchain (").append(chain.size()).append(" blocos):\n");
        for (Block block : chain) {
            sb.append("  ").append(block).append("\n");
        }
        return sb.toString();
    }
}
