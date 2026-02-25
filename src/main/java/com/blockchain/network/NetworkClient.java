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
     * Formato: [4 bytes tamanho big-endian] + [JSON UTF-8]
     */
    public static void sendToPeer(String peerHost, int peerPort, MessagePojo message) {
        executorService.submit(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(peerHost, peerPort), 5000);
                socket.setSoTimeout(5000);

                OutputStream output = socket.getOutputStream();
                
                // Serializa mensagem para JSON
                byte[] messageBytes = MessageHelper.toJson(message);
                int messageLength = messageBytes.length;
                
                // Prepara 4 bytes de tamanho (big-endian, '>I')
                byte[] lengthBytes = new byte[4];
                lengthBytes[0] = (byte) ((messageLength >> 24) & 0xFF);
                lengthBytes[1] = (byte) ((messageLength >> 16) & 0xFF);
                lengthBytes[2] = (byte) ((messageLength >> 8) & 0xFF);
                lengthBytes[3] = (byte) (messageLength & 0xFF);
                
                // Envia: tamanho (4 bytes) + mensagem
                output.write(lengthBytes);
                output.write(messageBytes);
                output.flush();

                log.debug("Mensagem enviada para {}:{} ({} bytes)", peerHost, peerPort, messageLength);
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
