package com.blockchain.helpers;

import java.util.ArrayList;
import java.util.List;

import com.blockchain.pojos.BlockPojo;
import com.blockchain.pojos.BlockchainPojo;
import com.blockchain.pojos.TransactionPojo;
import com.blockchain.utils.ProtocolUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BlockchainHelper {
    private BlockchainHelper() {
    }

    /**
     * Adiciona uma nova transação ao pool de pendentes
     * Valida as regras:
     * - Valores devem ser positivos
     * - Não permitir saldo negativo em nenhuma hipótese
     */
    public static void addTransaction(BlockchainPojo blockchainPojo, TransactionPojo transaction) {
        if (transaction.getOrigem() == null || transaction.getDestino() == null) {
            throw new IllegalArgumentException("Transação deve ter remetente e destinatário");
        }

        if (transaction.getValor() <= 0) {
            throw new IllegalArgumentException("Valor da transação deve ser positivo");
        }

        // REGRA CRÍTICA: Não permitir saldo negativo em nenhuma hipótese
        // Calcula o saldo após aplicar todas as transações pendentes
        double currentBalance = blockchainPojo.getBalance(transaction.getOrigem());

        // Subtrai as transações pendentes do mesmo sender
        for (TransactionPojo pending : blockchainPojo.getPendingTransactions()) {
            if (pending.getOrigem().equals(transaction.getOrigem())) {
                currentBalance -= pending.getValor();
            }
        }

        // Verifica se haverá saldo suficiente
        if (currentBalance - transaction.getValor() < 0) {
            throw new IllegalArgumentException(
                    String.format("Saldo insuficiente! Saldo atual: %.2f, Tentando enviar: %.2f",
                            currentBalance, transaction.getValor()));
        }

        blockchainPojo.addTransaction(transaction);

        log.debug("Transação adicionada: {}", transaction);
    }

    /**
     * Adiciona um bloco já minerado à chain (vindo da rede)
     * Valida antes de aceitar conforme especificação
     */
    public static boolean addBlock(BlockchainPojo blockchainPojo, BlockPojo block) {
        // Verifica se é o próximo bloco sequencial
        if (block.getIndex() != blockchainPojo.getChain().size()) {
            log.warn("Índice de bloco inválido");
            return false;
        }

        // Verifica se aponta para o bloco anterior correto
        if (!block.getPreviousHash().equals(blockchainPojo.getLatestBlock().getHash())) {
            log.warn("Hash do bloco anterior não corresponde");
            return false;
        }

        // Verifica se o bloco é válido (PoW)
        if (!block.getHash().startsWith(ProtocolUtil.DIFFICULTY)) {
            log.warn("Bloco inválido - PoW falhou");
            return false;
        }

        // Remove transações que já estão no bloco do pool de pendentes
        for (TransactionPojo tx : block.getTransactions()) {
            blockchainPojo.getPendingTransactions().removeIf(pending -> pending.getId().equals(tx.getId()));
        }

        blockchainPojo.getChain().add(block);
        log.info("Bloco adicionado: {}", block.getHash());
        return true;
    }

    /**
     * Cria saldo inicial para endereços (útil para testes)
     * Simula "mineração prévia" ou "funding inicial"
     */
    public static void fundAddress(BlockchainPojo blockchain, String address, double amount) {
        List<TransactionPojo> fundingTx = new ArrayList<>();
        fundingTx.add(new TransactionPojo("GENESIS_FUND", address, amount));

        int index = blockchain.getChain().size();
        String previousHash = blockchain.getLatestBlock().getHash();
        BlockPojo fundingBlock = new BlockPojo(index, fundingTx, previousHash);

        BlockHelper.mineBlock(fundingBlock, blockchain.getDifficulty());
        blockchain.addBlock(fundingBlock);

        log.info("Endereço {} recebeu funding de {}", address, amount);
    }

    /**
     * Verifica se toda a blockchain é válida
     * Equivalente ao is_chain_valid do Python
     */
    public static boolean isChainValid(BlockchainPojo blockchainPojo) {
        List<BlockPojo> chain = blockchainPojo.getChain();
        String difficulty = blockchainPojo.getDifficulty();

        for (int i = 1; i < chain.size(); i++) {
            BlockPojo currentBlock = chain.get(i);
            BlockPojo previousBlock = chain.get(i - 1);

            // Verifica: current.hash != current.calculate_hash()
            String calculatedHash = BlockHelper.calculateHash(currentBlock);
            if (!currentBlock.getHash().equals(calculatedHash)) {
                log.warn("Bloco {} tem hash inválido (calculado: {}, armazenado: {})", 
                         i, calculatedHash, currentBlock.getHash());
                return false;
            }

            // Verifica: current.previous_hash != previous.hash
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                log.warn("Bloco {} não aponta para o anterior", i);
                return false;
            }

            // Verifica: not current.hash.startswith(DIFFICULTY)
            if (!currentBlock.getHash().startsWith(difficulty)) {
                log.warn("Bloco {} não começa com dificuldade {}", i, difficulty);
                return false;
            }
        }

        return true;
    }
}
