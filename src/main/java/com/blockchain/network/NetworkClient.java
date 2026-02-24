package com.blockchain.network;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.ConnectException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.blockchain.factories.MessageFactory;
import com.blockchain.helpers.BlockchainHelper;
import com.blockchain.helpers.MessageHelper;
import com.blockchain.pojos.BlockchainPojo;
import com.blockchain.pojos.MessagePojo;
import com.blockchain.pojos.NodePojo;
import com.blockchain.pojos.PeerPojo;
import com.blockchain.pojos.TransactionPojo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NetworkClient {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private NetworkClient() {
    }

    /**
     * Envia uma mensagem para um peer específico
     */
    public static void sendToPeer(String peerHost, int peerPort, MessagePojo message) {
        executorService.submit(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(peerHost, peerPort), 5000);
                socket.setSoTimeout(5000);

                OutputStream output = socket.getOutputStream();
                output.write(MessageHelper.toJson(message));
                output.flush();

                log.debug("Mensagem enviada para {}:{}", peerHost, peerPort);
            } catch (ConnectException e) {
                log.warn("Peer {}:{} não está disponível. [{}]", peerHost, peerPort, e.getMessage());
            } catch (SocketTimeoutException e) {
                log.warn("Timeout ao conectar com {}:{}", peerHost, peerPort);
            } catch (IOException e) {
                log.error("Erro ao enviar para {}:{} - {}", peerHost, peerPort, e.getMessage());
            }
        });
    }

    /**
     * Faz broadcast de uma mensagem para todos os peers
     */
    public static void broadcast(Set<PeerPojo> peers, MessagePojo message) {
        for (PeerPojo peer : peers) {
            sendToPeer(peer.getHost(), peer.getPort(), message);
        }
    }

    /**
     * Adiciona uma transação e faz broadcast
     * Valida saldo antes de criar (não permitir saldo negativo)
     */
    public static void createTransaction(NodePojo node, String sender, String recipient, double amount) {
        try {
            BlockchainPojo blockchain = node.getBlockchain();

            TransactionPojo transaction = new TransactionPojo(sender, recipient, amount);
            BlockchainHelper.addTransaction(blockchain, transaction);

            // Broadcast da transação
            MessagePojo message = MessageFactory.buildNewTransaction(transaction, node.getHost(), node.getPort());
            broadcast(node.getPeers(), message);

            log.info("✓ Transação criada e transmitida: {}", transaction);
        } catch (IllegalArgumentException ex) {
            log.error("✗ Erro ao criar transação: {}", ex.getMessage());
        }
    }
}
