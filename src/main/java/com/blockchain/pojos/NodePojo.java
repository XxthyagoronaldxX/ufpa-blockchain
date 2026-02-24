package com.blockchain.pojos;

import java.net.ServerSocket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@ToString
@Slf4j
public class NodePojo {
    private final String host;

    private final int port;

    private final BlockchainPojo blockchain;

    private final ExecutorService executorService;

    private final Set<PeerPojo> peers;

    private ServerSocket serverSocket;

    private volatile boolean running;

    public NodePojo(String host, int port) {
        this.host = host;
        this.port = port;
        this.blockchain = new BlockchainPojo();
        this.peers = ConcurrentHashMap.newKeySet();
        this.executorService = Executors.newCachedThreadPool();
        this.running = false;
    }

    public void addPeer(String peerHost, int peerPort) {
        PeerPojo peer = new PeerPojo(peerHost, peerPort);

        if (peers.add(peer)) {
            log.info("Peer adicionado: {}", peer);
        }
    }

    public void addTransaction(TransactionPojo transaction) {
        blockchain.addTransaction(transaction);
    }
}
