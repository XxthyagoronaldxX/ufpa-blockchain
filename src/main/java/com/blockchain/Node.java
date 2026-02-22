package com.blockchain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nó da rede blockchain P2P
 * Gerencia conexões, broadcast de mensagens e sincronização
 */
public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final String host;
    private final int port;
    private final Blockchain blockchain;
    private final ExecutorService executorService;
    private final Set<Peer> peers;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public Node(String host, int port) {
        this.host = host;
        this.port = port;
        this.blockchain = new Blockchain();
        this.peers = ConcurrentHashMap.newKeySet();
        this.executorService = Executors.newCachedThreadPool();
        this.running = false;
    }

    /**
     * Inicia o servidor do nó
     */
    public void start() throws IOException {
        // Bind no endereço específico - 0.0.0.0 aceita de qualquer interface
        serverSocket = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
        running = true;

        logger.info("═══════════════════════════════════════");
        logger.info("  Nó Blockchain iniciado");
        logger.info("  Host: {}", host);
        logger.info("  Port: {}", port);
        logger.info("  Bind: {}", serverSocket.getInetAddress());
        logger.info("═══════════════════════════════════════");

        // Thread para aceitar conexões
        executorService.submit(this::acceptConnections);
    }

    /**
     * Aceita conexões de outros nós
     */
    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleConnection(clientSocket));
            } catch (IOException ex) {
                if (running) {
                    logger.error("Erro ao aceitar conexão: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Manipula uma conexão recebida
     */
    private void handleConnection(Socket socket) {
        try (socket) {
            InputStream input = socket.getInputStream();
            byte[] buffer = new byte[Protocol.BUFFER_SIZE];
            int bytesRead = input.read(buffer);

            if (bytesRead > 0) {
                byte[] messageBytes = Arrays.copyOf(buffer, bytesRead);
                Message message = Message.fromJson(messageBytes);

                logger.debug("Mensagem recebida: {}", message);
                handleMessage(message);
            }
        } catch (IOException ex) {
            logger.error("Erro ao manipular conexão: {}", ex.getMessage());
        }
    }

    /**
     * Processa mensagens recebidas baseado no tipo
     */
    private void handleMessage(Message message) {
        try {
            switch (message.getType()) {
                case Protocol.NEW_TRANSACTION -> handleNewTransaction(message);
                case Protocol.NEW_BLOCK -> handleNewBlock(message);
                case Protocol.REQUEST_CHAIN -> handleRequestChain(message);
                case Protocol.RESPONSE_CHAIN -> handleResponseChain(message);
                default -> logger.warn("Tipo de mensagem desconhecido: {}", message.getType());
            }

            // Adiciona o remetente aos peers conhecidos
            if (message.getSenderHost() != null && message.getSenderPort() != null) {
                addPeer(message.getSenderHost(), message.getSenderPort());
            }

        } catch (Exception e) {
            logger.error("Erro ao processar mensagem: " + e.getMessage(), e);
        }
    }

    /**
     * Processa nova transação recebida
     */
    private void handleNewTransaction(Message message) {
        Transaction transaction = message.getTransaction();
        blockchain.addTransaction(transaction);
        logger.info("✓ Transação recebida e adicionada: {}", transaction);
    }

    /**
     * Processa novo bloco recebido
     */
    private void handleNewBlock(Message message) {
        Block block = message.getBlock();
        boolean added = blockchain.addBlock(block);

        if (added) {
            logger.info("✓ Bloco recebido e adicionado: {}", block.getHash());
        } else {
            logger.warn("✗ Bloco recebido rejeitado");
        }
    }

    /**
     * Processa requisição de chain
     */
    private void handleRequestChain(Message message) {
        logger.debug("Requisição de chain recebida");

        // Envia a chain de volta
        if (message.getSenderHost() != null && message.getSenderPort() != null) {
            Message response = Message.buildResponseChain(blockchain, host, port);
            sendToPeer(message.getSenderHost(), message.getSenderPort(), response);
        }
    }

    /**
     * Processa resposta com chain completa
     */
    private void handleResponseChain(Message message) {
        Blockchain receivedChain = message.getBlockchain();
        logger.info("Chain recebida com {} blocos", receivedChain.getChainLength());

        boolean replaced = blockchain.replaceChain(receivedChain.getChain());
        if (replaced) {
            logger.info("✓ Chain local substituída");
        } else {
            logger.info("✗ Chain local mantida");
        }
    }

    /**
     * Adiciona um peer à lista de conhecidos
     */
    public void addPeer(String peerHost, int peerPort) {
        Peer peer = new Peer(peerHost, peerPort);
        if (peers.add(peer)) {
            logger.info("Peer adicionado: {}", peer);
        }
    }

    /**
     * Conecta a um peer e solicita a chain
     */
    public void connectToPeer(String peerHost, int peerPort) {
        addPeer(peerHost, peerPort);

        // Solicita a blockchain completa
        Message request = Message.buildRequestChain(host, port);
        sendToPeer(peerHost, peerPort, request);
    }

    /**
     * Envia uma mensagem para um peer específico
     */
    public void sendToPeer(String peerHost, int peerPort, Message message) {
        executorService.submit(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new java.net.InetSocketAddress(peerHost, peerPort), 5000);
                socket.setSoTimeout(5000);

                OutputStream output = socket.getOutputStream();
                output.write(message.toJson());
                output.flush();

                logger.debug("Mensagem enviada para {}:{}", peerHost, peerPort);
            } catch (ConnectException e) {
                logger.warn("Peer {}:{} não está disponível. [{}]", peerHost, peerPort, e.getMessage());
            } catch (SocketTimeoutException e) {
                logger.warn("Timeout ao conectar com {}:{}", peerHost, peerPort);
            } catch (IOException e) {
                logger.error("Erro ao enviar para {}:{} - {}", peerHost, peerPort, e.getMessage());
            }
        });
    }

    /**
     * Faz broadcast de uma mensagem para todos os peers
     */
    public void broadcast(Message message) {
        for (Peer peer : peers) {
            sendToPeer(peer.getHost(), peer.getPort(), message);
        }
    }

    /**
     * Adiciona uma transação e faz broadcast
     * Valida saldo antes de criar (não permitir saldo negativo)
     */
    public void createTransaction(String sender, String recipient, double amount) {
        try {
            Transaction transaction = new Transaction(sender, recipient, amount);
            blockchain.addTransaction(transaction);

            // Broadcast da transação
            Message message = Message.buildNewTransaction(transaction, host, port);
            broadcast(message);

            logger.info("✓ Transação criada e transmitida: {}", transaction);
        } catch (IllegalArgumentException ex) {
            logger.error("✗ Erro ao criar transação: {}", ex.getMessage());
        }
    }

    /**
     * Minera um bloco e faz broadcast
     * Implementa Proof of Work conforme especificação do trabalho
     */
    public void mineBlock() {
        Block newBlock = blockchain.minePendingTransactions();

        // Broadcast do bloco minerado
        Message message = Message.buildNewBlock(newBlock, host, port);
        broadcast(message);

        logger.info("✓ Bloco minerado e transmitido!");
    }

    /**
     * Cria funding inicial para um endereço (útil para testes)
     */
    public void fundAddress(String address, double amount) {
        blockchain.fundAddress(address, amount);
    }

    /**
     * Para o servidor do nó
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Erro ao fechar servidor: {}", e.getMessage());
        }
        executorService.shutdown();
    }

    // Getters
    public Blockchain getBlockchain() {
        return blockchain;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Set<Peer> getPeers() {
        return new HashSet<>(peers);
    }

    /**
     * Classe interna para representar um peer
     */
    public static class Peer {
        private final String host;
        private final int port;

        public Peer(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Peer peer = (Peer) o;
            return port == peer.port && Objects.equals(host, peer.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port);
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }
    }
}
