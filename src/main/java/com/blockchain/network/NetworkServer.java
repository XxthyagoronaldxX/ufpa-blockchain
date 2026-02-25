package com.blockchain.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.blockchain.factories.MessageFactory;
import com.blockchain.pojos.MessagePojo;
import com.blockchain.pojos.NodePojo;
import com.blockchain.utils.ProtocolUtil;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class NetworkServer {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    private volatile boolean running;
    private NodePojo node;

    /**
     * Inicia o servidor do nó
     */
    public void start(NodePojo nodePojo) throws IOException {
        // Bind no endereço específico - 0.0.0.0 aceita de qualquer interface
        serverSocket = new ServerSocket(nodePojo.getPort(), 50, InetAddress.getByName("0.0.0.0"));
        running = true;
        node = nodePojo;

        log.info("═══════════════════════════════════════");
        log.info("  Nó Blockchain iniciado");
        log.info("  Host: {}", nodePojo.getHost());
        log.info("  Port: {}", nodePojo.getPort());
        log.info("  Bind: {}", serverSocket.getInetAddress());
        log.info("═══════════════════════════════════════");

        // Thread para aceitar conexões
        executorService.submit(this::acceptConnections);
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
            log.error("Erro ao fechar servidor: {}", e.getMessage());
        }
        executorService.shutdown();
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
                    log.error("Erro ao aceitar conexão: {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Manipula uma conexão recebida
     * Formato: [4 bytes tamanho big-endian] + [JSON UTF-8]
     */
    private void handleConnection(Socket socket) {
        try (socket) {
            InputStream input = socket.getInputStream();

            // Lê os 4 primeiros bytes (tamanho da mensagem em big-endian)
            byte[] lengthBytes = new byte[4];
            int bytesRead = input.read(lengthBytes);

            if (bytesRead != 4) {
                log.warn("Conexão fechada ou cabeçalho incompleto");
                return;
            }

            // Converte big-endian ('>I') para int
            int messageLength = ((lengthBytes[0] & 0xFF) << 24) |
                    ((lengthBytes[1] & 0xFF) << 16) |
                    ((lengthBytes[2] & 0xFF) << 8) |
                    (lengthBytes[3] & 0xFF);

            if (messageLength <= 0 || messageLength > ProtocolUtil.BUFFER_SIZE) {
                log.error("Tamanho de mensagem inválido: {}", messageLength);
                return;
            }

            // Lê exatamente messageLength bytes
            byte[] messageBytes = new byte[messageLength];
            int totalRead = 0;
            while (totalRead < messageLength) {
                int read = input.read(messageBytes, totalRead, messageLength - totalRead);
                if (read == -1) {
                    log.error("Conexão fechada antes de ler mensagem completa");
                    return;
                }
                totalRead += read;
            }

            MessagePojo message = MessageFactory.fromJson(messageBytes);
            log.debug("Mensagem recebida: {} ({} bytes)", message.getType(), messageLength);
            MessageHandler.handleMessage(node, message);
        } catch (IOException ex) {
            log.error("Erro ao manipular conexão: {}", ex.getMessage());
        }
    }

}
