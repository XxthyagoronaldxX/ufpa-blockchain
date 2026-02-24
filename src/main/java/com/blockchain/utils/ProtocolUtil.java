package com.blockchain.utils;

/**
 * Constantes e configurações do protocolo de rede blockchain
 * Conforme especificações do trabalho acadêmico
 * 
 * PROTOCOLO DE COMUNICAÇÃO:
 * - NEW_TRANSACTION: Envio de uma nova transação
 * - NEW_BLOCK: Envio de um bloco minerado
 * - REQUEST_CHAIN: Solicitação da blockchain completa (ao entrar na rede)
 * - RESPONSE_CHAIN: Envio da blockchain para sincronização
 * 
 * REGRAS DO SISTEMA:
 * - Proof of Work com dificuldade fixa ("000" - 3 zeros)
 * - Comunicação via sockets com mensagens JSON
 * - Cada nó mantém cópia local da blockchain
 * - Consenso pela cadeia mais longa
 * - Não permitir saldo negativo em nenhuma hipótese
 * - Somente valores positivos nas transações
 */
public class ProtocolUtil {
    
    // TIPOS DE MENSAGENS (Protocolo de Comunicação)
    public static final String NEW_TRANSACTION = "NEW_TRANSACTION";  // Envio de uma nova transação
    public static final String NEW_BLOCK = "NEW_BLOCK";              // Envio de um bloco minerado
    public static final String REQUEST_CHAIN = "REQUEST_CHAIN";      // Solicitação da blockchain completa
    public static final String RESPONSE_CHAIN = "RESPONSE_CHAIN";    // Envio da blockchain para sincronização
    
    // CONFIGURAÇÕES DA REDE
    public static final int BUFFER_SIZE = 8192;        // Tamanho do buffer de recebimento do socket (aumentado)
    public static final String ENCODING = "UTF-8";     // Codificação padrão das mensagens
    public static final String DIFFICULTY = "000";     // Dificuldade fixa do Proof of Work (3 zeros)
    
    // Impedir instanciação
    private ProtocolUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
}
