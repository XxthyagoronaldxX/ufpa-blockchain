package com.blockchain;

import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        printHeader();

        if (args.length > 0 && args[0].equals("--demo")) {
            runDemo();
        } else {
            runInteractiveNode(args);
        }
    }

    private static void printHeader() {
        logger.info("╔═══════════════════════════════════════════════════╗");
        logger.info("║     BLOCKCHAIN JAVA - Implementação P2P           ║");
        logger.info("╚═══════════════════════════════════════════════════╝");
    }

    /**
     * Modo demonstração - executa operações básicas
     */
    private static void runDemo() {
        logger.info("=== MODO DEMONSTRAÇÃO ===");
        logger.info("");
        logger.info("Conforme especificações do trabalho acadêmico:");
        logger.info("- Transações somente com valores positivos");
        logger.info("- Não permitir saldo negativo em nenhuma hipótese");
        logger.info("- Proof of Work com dificuldade '000'");
        logger.info("");

        // Cria blockchain
        Blockchain blockchain = new Blockchain();

        // Funding inicial (simula que esses endereços já têm moedas)
        logger.info("1. Criando funding inicial para endereços...");
        blockchain.fundAddress("Alice", 100.0);
        blockchain.fundAddress("Bob", 100.0);
        blockchain.fundAddress("Charlie", 50.0);

        // Adiciona transações
        logger.info("");
        logger.info("2. Adicionando transações válidas...");
        blockchain.addTransaction(new Transaction("Alice", "Bob", 50.0));
        blockchain.addTransaction(new Transaction("Bob", "Charlie", 25.0));

        // Minera bloco
        logger.info("");
        logger.info("3. Minerando bloco 1...");
        blockchain.minePendingTransactions();

        // Mais transações
        logger.info("");
        logger.info("4. Adicionando mais transações...");
        blockchain.addTransaction(new Transaction("Charlie", "Alice", 10.0));

        // Minera outro bloco
        logger.info("");
        logger.info("5. Minerando bloco 2...");
        blockchain.minePendingTransactions();

        // Verifica validade
        logger.info("");
        logger.info("6. Verificando validade da blockchain...");
        logger.info("Chain válida? {}", blockchain.isChainValid());

        // Mostra saldos
        logger.info("");
        logger.info("7. Saldos finais:");
        logger.info("   Alice: {} (100 - 50 + 10 = 60)", blockchain.getBalance("Alice"));
        logger.info("   Bob: {} (100 + 50 - 25 = 125)", blockchain.getBalance("Bob"));
        logger.info("   Charlie: {} (50 + 25 - 10 = 65)", blockchain.getBalance("Charlie"));

        // Testa regra de saldo negativo
        logger.info("");
        logger.info("8. Testando regra de saldo negativo...");
        try {
            blockchain.addTransaction(new Transaction("Charlie", "Alice", 1000.0));
            logger.error("✗ ERRO: Deveria ter rejeitado!");
        } catch (IllegalArgumentException e) {
            logger.info("✓ Transação corretamente rejeitada: {}", e.getMessage());
        }

        // Testa valor negativo
        logger.info("");
        logger.info("9. Testando regra de valor positivo...");
        try {
            blockchain.addTransaction(new Transaction("Alice", "Bob", -10.0));
            logger.error("✗ ERRO: Deveria ter rejeitado!");
        } catch (IllegalArgumentException e) {
            logger.info("✓ Transação corretamente rejeitada: {}", e.getMessage());
        }

        // Mostra blockchain
        logger.info("");
        logger.info("10. Blockchain completa:");
        logger.info("{}", blockchain);

        logger.info("");
        logger.info("=== DEMONSTRAÇÃO CONCLUÍDA ===");
    }

    /**
     * Modo interativo - inicia um nó da rede
     */
    private static void runInteractiveNode(String[] args) {
        String host = "localhost";
        int port = 5000;

        // Parse argumentos
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Porta inválida, usando 5000");
            }
        }

        if (args.length >= 2) {
            host = args[1];
        }

        logger.info("Iniciando nó blockchain...");
        logger.info("Host: {}", host);
        logger.info("Porta: {}", port);

        try {
            Node node = new Node(host, port);
            node.start();

            runInteractiveMenu(node);

            node.stop();
        } catch (IOException e) {
            logger.error("Erro ao iniciar nó: {}", e.getMessage(), e);
        }
    }

    /**
     * Menu interativo do nó
     */
    private static void runInteractiveMenu(Node node) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createTransaction(scanner, node);
                    case "2" -> mineBlock(node);
                    case "3" -> showBlockchain(node);
                    case "4" -> showBalance(scanner, node);
                    case "5" -> connectToPeer(scanner, node);
                    case "6" -> showPeers(node);
                    case "7" -> validateChain(node);
                    case "8" -> showInfo(node);
                    case "9" -> fundAddress(scanner, node);
                    case "0" -> {
                        logger.info("Encerrando...");
                        return;
                    }
                    default -> logger.warn("Opção inválida!");
                }
            } catch (Exception e) {
                logger.error("Erro: {}", e.getMessage(), e);
            }

            logger.info("");
        }
    }

    private static void printMenu() {
        logger.info("");
        logger.info("╔════════════════════════════════════╗");
        logger.info("║        MENU BLOCKCHAIN             ║");
        logger.info("╠════════════════════════════════════╣");
        logger.info("║ 1. Criar transação                 ║");
        logger.info("║ 2. Minerar bloco                   ║");
        logger.info("║ 3. Mostrar blockchain              ║");
        logger.info("║ 4. Consultar saldo                 ║");
        logger.info("║ 5. Conectar a peer                 ║");
        logger.info("║ 6. Listar peers                    ║");
        logger.info("║ 7. Validar chain                   ║");
        logger.info("║ 8. Informações do nó               ║");
        logger.info("║ 9. Criar funding (teste)           ║");
        logger.info("║ 0. Sair                            ║");
        logger.info("╚════════════════════════════════════╝");
        logger.info("Escolha uma opção: ");
    }

    private static void createTransaction(Scanner scanner, Node node) {
        logger.info("Remetente: ");
        String sender = scanner.nextLine();
        logger.info("Destinatário: ");
        String recipient = scanner.nextLine();
        logger.info("Valor: ");
        double amount = Double.parseDouble(scanner.nextLine());

        try {
            node.createTransaction(sender, recipient, amount);
        } catch (IllegalArgumentException e) {
            // Erro já foi exibido pelo Node
        }
    }

    private static void mineBlock(Node node) {
        int pendingCount = node.getBlockchain().getPendingTransactions().size();
        if (pendingCount == 0) {
            logger.info("Nenhuma transação pendente!");
            return;
        }

        logger.info("Minerando {} transações...", pendingCount);
        node.mineBlock();
    }

    private static void fundAddress(Scanner scanner, Node node) {
        logger.info("Endereço para funding: ");
        String address = scanner.nextLine();
        logger.info("Valor: ");
        double amount = Double.parseDouble(scanner.nextLine());

        node.fundAddress(address, amount);
        logger.info("✓ Funding criado!");
    }

    private static void showBlockchain(Node node) {
        logger.info("");
        logger.info("{}", node.getBlockchain());
    }

    private static void showBalance(Scanner scanner, Node node) {
        logger.info("Endereço: ");
        String address = scanner.nextLine();
        double balance = node.getBlockchain().getBalance(address);
        logger.info("Saldo de {}: {}", address, balance);
    }

    private static void connectToPeer(Scanner scanner, Node node) {
        logger.info("Host do peer: ");
        String peerHost = scanner.nextLine();
        logger.info("Porta do peer: ");
        int peerPort = Integer.parseInt(scanner.nextLine());

        node.connectToPeer(peerHost, peerPort);
        logger.info("✓ Solicitação de conexão enviada!");
    }

    private static void showPeers(Node node) {
        logger.info("");
        logger.info("Peers conectados:");
        if (node.getPeers().isEmpty()) {
            logger.info("  (nenhum peer conectado)");
        } else {
            for (Node.Peer peer : node.getPeers()) {
                logger.info("  - {}", peer);
            }
        }
    }

    private static void validateChain(Node node) {
        boolean valid = node.getBlockchain().isChainValid();
        logger.info("Blockchain válida? {}", valid ? "✓ SIM" : "✗ NÃO");
    }

    private static void showInfo(Node node) {
        Blockchain bc = node.getBlockchain();
        logger.info("");
        logger.info("╔════════════════════════════════════╗");
        logger.info("║     INFORMAÇÕES DO NÓ              ║");
        logger.info("╠════════════════════════════════════╣");
        logger.info("  Host: {}", node.getHost());
        logger.info("  Porta: {}", node.getPort());
        logger.info("  Blocos na chain: {}", bc.getChainLength());
        logger.info("  Transações pendentes: {}", bc.getPendingTransactions().size());
        logger.info("  Peers conectados: {}", node.getPeers().size());
        logger.info("  Dificuldade: {}", bc.getDifficulty());
        logger.info("╚════════════════════════════════════╝");
    }
}
