package com.blockchain;

import java.io.IOException;
import java.util.Scanner;

import com.blockchain.factories.MessageFactory;
import com.blockchain.helpers.BlockchainHelper;
import com.blockchain.network.NetworkClient;
import com.blockchain.network.NetworkServer;
import com.blockchain.pojos.BlockPojo;
import com.blockchain.pojos.BlockchainPojo;
import com.blockchain.pojos.MessagePojo;
import com.blockchain.pojos.NodePojo;
import com.blockchain.pojos.PeerPojo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    private static final NetworkServer networkServer = new NetworkServer();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        printHeader();

        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.warn("Porta inválida, usando 5000");
            }
        }

        if (args.length >= 2) {
            host = args[1];
        }

        log.info("Iniciando nó blockchain...");
        log.info("Host: {}", host);
        log.info("Porta: {}", port);

        try {
            NodePojo node = new NodePojo(host, port);
            networkServer.start(node);

            runInteractiveMenu(node);

            networkServer.stop();
        } catch (IOException e) {
            log.error("Erro ao iniciar nó: {}", e.getMessage(), e);
        }
    }

    private static void printHeader() {
        log.info("╔═══════════════════════════════════════════════════╗");
        log.info("║     BLOCKCHAIN JAVA - Implementação P2P           ║");
        log.info("╚═══════════════════════════════════════════════════╝");
    }

    /**
     * Menu interativo do nó
     */
    private static void runInteractiveMenu(NodePojo node) {
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createTransaction(node);
                    case "2" -> mineBlock(node);
                    case "3" -> showBlockchain(node);
                    case "4" -> showBalance(node);
                    case "5" -> connectToPeer(node);
                    case "6" -> showPeers(node);
                    case "7" -> validateChain(node);
                    case "8" -> showInfo(node);
                    case "9" -> fundAddress(node);
                    case "0" -> {
                        log.info("Encerrando...");
                        return;
                    }
                    default -> log.warn("Opção inválida!");
                }
            } catch (Exception e) {
                log.error("Erro: {}", e.getMessage(), e);
            }

            log.info("");
        }
    }

    private static void printMenu() {
        log.info("");
        log.info("╔════════════════════════════════════╗");
        log.info("║        MENU BLOCKCHAIN             ║");
        log.info("╠════════════════════════════════════╣");
        log.info("║ 1. Criar transação                 ║");
        log.info("║ 2. Minerar bloco                   ║");
        log.info("║ 3. Mostrar blockchain              ║");
        log.info("║ 4. Consultar saldo                 ║");
        log.info("║ 5. Conectar a peer                 ║");
        log.info("║ 6. Listar peers                    ║");
        log.info("║ 7. Validar chain                   ║");
        log.info("║ 8. Informações do nó               ║");
        log.info("║ 9. Criar funding (teste)           ║");
        log.info("║ 0. Sair                            ║");
        log.info("╚════════════════════════════════════╝");
        log.info("Escolha uma opção: ");
    }

    private static void createTransaction(NodePojo node) {
        log.info("Remetente: ");
        String sender = scanner.nextLine();
        log.info("Destinatário: ");
        String recipient = scanner.nextLine();
        log.info("Valor: ");
        double amount = Double.parseDouble(scanner.nextLine());

        try {
            NetworkClient.createTransaction(node, sender, recipient, amount);
        } catch (IllegalArgumentException e) {
            // Erro já foi exibido pelo Node
        }
    }

    private static void mineBlock(NodePojo node) {
        BlockchainPojo blockchain = node.getBlockchain();
        int pendingCount = blockchain.getPendingTransactions().size();

        if (pendingCount == 0) {
            log.info("Nenhuma transação pendente!");
            return;
        }

        BlockPojo newBlock = blockchain.minePendingTransactions();

        // Broadcast do bloco minerado
        MessagePojo message = MessageFactory.buildNewBlock(newBlock, node.getHost(), node.getPort());
        NetworkClient.broadcast(node.getPeers(), message);

        log.info("✓ Bloco minerado e transmitido!");
    }

    private static void fundAddress(NodePojo node) {
        log.info("Endereço para funding: ");
        String address = scanner.nextLine();
        log.info("Valor: ");
        double amount = Double.parseDouble(scanner.nextLine());

        BlockchainHelper.fundAddress(node.getBlockchain(), address, amount);
        log.info("✓ Funding criado!");
    }

    private static void showBlockchain(NodePojo node) {
        log.info("");
        log.info("{}", node.getBlockchain());
    }

    private static void showBalance(NodePojo node) {
        log.info("Endereço: ");
        String address = scanner.nextLine();
        double balance = node.getBlockchain().getBalance(address);
        log.info("Saldo de {}: {}", address, balance);
    }

    private static void connectToPeer(NodePojo node) {
        log.info("Host do peer: ");
        String peerHost = scanner.nextLine();
        log.info("Porta do peer: ");
        int peerPort = Integer.parseInt(scanner.nextLine());

        node.addPeer(peerHost, peerPort);

        // Solicita a blockchain completa
        MessagePojo request = MessageFactory.buildRequestChain(node.getHost(), node.getPort());
        NetworkClient.sendToPeer(peerHost, peerPort, request);
        log.info("✓ Solicitação de conexão enviada!");
    }

    private static void showPeers(NodePojo node) {
        log.info("");
        log.info("Peers conectados:");

        if (node.getPeers().isEmpty()) {
            log.info("  (nenhum peer conectado)");
        } else {
            for (PeerPojo peer : node.getPeers()) {
                log.info("  - {}", peer);
            }
        }
    }

    private static void validateChain(NodePojo node) {
        boolean valid = BlockchainHelper.isChainValid(node.getBlockchain());

        log.info("Blockchain válida? {}", valid ? "✓ SIM" : "✗ NÃO");
    }

    private static void showInfo(NodePojo node) {
        BlockchainPojo bc = node.getBlockchain();
        
        log.info("");
        log.info("╔════════════════════════════════════╗");
        log.info("║     INFORMAÇÕES DO NÓ              ║");
        log.info("╠════════════════════════════════════╣");
        log.info("  Host: {}", node.getHost());
        log.info("  Porta: {}", node.getPort());
        log.info("  Blocos na chain: {}", bc.getChainLength());
        log.info("  Transações pendentes: {}", bc.getPendingTransactions().size());
        log.info("  Peers conectados: {}", node.getPeers().size());
        log.info("  Dificuldade: {}", bc.getDifficulty());
        log.info("╚════════════════════════════════════╝");
    }
}
