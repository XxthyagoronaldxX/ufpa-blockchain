package com.blockchain.pojos;

import java.util.ArrayList;
import java.util.List;

import com.blockchain.factories.BlockFactory;
import com.blockchain.helpers.BlockHelper;
import com.blockchain.helpers.BlockchainHelper;
import com.blockchain.utils.ProtocolUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@Slf4j
public class BlockchainPojo {
    @JsonProperty("pending_transactions")
    private List<TransactionPojo> pendingTransactions;

    private List<BlockPojo> chain;

    private String difficulty;

    public BlockchainPojo() {
        this.chain = new ArrayList<>();
        this.pendingTransactions = new ArrayList<>();
        this.difficulty = ProtocolUtil.DIFFICULTY;

        BlockPojo genesis = BlockFactory.createGenesisBlock();

        this.chain.add(genesis);

        log.info("Bloco Gênesis criado: {}", genesis.getHash());
    }

    /**
     * Substitui a chain atual por uma nova (sincronização)
     * Consenso: cadeia mais longa prevalece
     */
    public boolean replaceChain(List<BlockPojo> newChain) {
        // Verifica se a nova chain é mais longa
        if (newChain.size() <= chain.size()) {
            log.debug("Chain recebida não é mais longa");
            return false;
        }

        // Valida a nova chain
        BlockchainPojo tempBlockchain = new BlockchainPojo();
        tempBlockchain.setChain(new ArrayList<>(newChain));

        if (!BlockchainHelper.isChainValid(tempBlockchain)) {
            log.warn("Chain recebida é inválida");
            return false;
        }

        // Substitui a chain
        chain = new ArrayList<>(newChain);

        log.info("Chain substituída com sucesso");

        return true;
    }

    /**
     * Minera um novo bloco com as transações pendentes
     * SEM recompensa de mineração (não especificada no trabalho)
     */
    public BlockPojo minePendingTransactions() {
        if (pendingTransactions.isEmpty()) {
            throw new IllegalStateException("Não há transações pendentes para minerar");
        }

        // Cria novo bloco com transações pendentes
        BlockPojo newBlock = new BlockPojo(
                chain.size(),
                new ArrayList<>(pendingTransactions),
                getLatestBlock().getHash());

        // Minera o bloco (Proof of Work)
        log.info("Minerando bloco {}...", newBlock.getIndex());
        BlockHelper.mineBlock(newBlock, difficulty);

        // Adiciona à chain
        chain.add(newBlock);

        // Limpa transações pendentes
        pendingTransactions.clear();

        return newBlock;
    }

    @JsonIgnore
    public BlockPojo getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addTransaction(TransactionPojo transaction) {
        pendingTransactions.add(transaction);
    }

    public void addBlock(BlockPojo block) {
        chain.add(block);
    }

    @JsonIgnore
    public int getChainLength() {
        return chain.size();
    }

    public double getBalance(String address) {
        double balance = 0.0;

        for (BlockPojo block : chain) {
            for (TransactionPojo tx : block.getTransactions()) {
                if (tx.getDestino().equals(address)) {
                    balance += tx.getValor();
                }
                if (tx.getOrigem().equals(address)) {
                    balance -= tx.getValor();
                }
            }
        }

        return balance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Blockchain (").append(chain.size()).append(" blocos):\n");
        for (BlockPojo block : chain) {
            sb.append("  ").append(block).append("\n");
        }
        return sb.toString();
    }
}
