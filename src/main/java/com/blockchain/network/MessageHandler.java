package com.blockchain.network;

import com.blockchain.factories.MessageFactory;
import com.blockchain.helpers.BlockchainHelper;
import com.blockchain.helpers.MessageHelper;
import com.blockchain.pojos.BlockPojo;
import com.blockchain.pojos.BlockchainPojo;
import com.blockchain.pojos.MessagePojo;
import com.blockchain.pojos.NodePojo;
import com.blockchain.pojos.TransactionPojo;
import com.blockchain.utils.ProtocolUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageHandler {
    private MessageHandler() {
    }

    /**
     * Processa mensagens recebidas baseado no tipo
     */
    public static void handleMessage(NodePojo node, MessagePojo message) {
        try {
            switch (message.getType()) {
                case ProtocolUtil.NEW_TRANSACTION -> handleNewTransaction(node, message);
                case ProtocolUtil.NEW_BLOCK -> handleNewBlock(node, message);
                case ProtocolUtil.REQUEST_CHAIN -> handleRequestChain(node, message);
                case ProtocolUtil.RESPONSE_CHAIN -> handleResponseChain(node, message);
                default -> log.warn("Tipo de mensagem desconhecido: {}", message.getType());
            }

            if (message.getSender() != null) {
                String[] parts = message.getSender().split(":");
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);

                node.addPeer(host, port);
            }
        } catch (NumberFormatException ex) {
            log.error("Porta inválida no remetente: " + message.getSender(), ex);
        } catch (Exception ex) {
            log.error("Erro ao processar mensagem: " + ex.getMessage(), ex);
        }
    }

    /**
     * Processa nova transação recebida
     */
    private static void handleNewTransaction(NodePojo node, MessagePojo message) {
        TransactionPojo transaction = MessageHelper.getTransaction(message);
        node.addTransaction(transaction);
        log.info("✓ Transação recebida e adicionada: {}", transaction);
    }

    /**
     * Processa novo bloco recebido
     */
    private static void handleNewBlock(NodePojo node, MessagePojo message) {
        BlockPojo block = MessageHelper.getBlock(message);
        boolean added = BlockchainHelper.addBlock(node.getBlockchain(), block);

        if (added) {
            log.info("✓ Bloco recebido e adicionado: {}", block.getHash());
        } else {
            log.warn("✗ Bloco recebido rejeitado");
        }
    }

    /**
     * Processa requisição de chain
     */
    private static void handleRequestChain(NodePojo node, MessagePojo message) {
        log.debug("Requisição de chain recebida");

        // Envia a chain de volta
        if (message.getSender() != null) {
            String[] parts = message.getSender().split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            MessagePojo response = MessageFactory.buildResponseChain(node.getBlockchain(), node.getHost(),
                    node.getPort());

            NetworkClient.sendToPeer(host, port, response);
        } else {
            log.warn("Requisição de chain sem remetente válido");
        }
    }

    /**
     * Processa resposta com chain completa
     */
    private static void handleResponseChain(NodePojo node, MessagePojo message) {
        BlockchainPojo receivedChain = MessageHelper.getBlockchain(message);
        log.info("Chain recebida com {} blocos", receivedChain.getChainLength());
        boolean replaced = node.getBlockchain().replaceChain(receivedChain.getChain());

        if (replaced) {
            log.info("✓ Chain local substituída");
        } else {
            log.info("✗ Chain local mantida");
        }
    }
}
