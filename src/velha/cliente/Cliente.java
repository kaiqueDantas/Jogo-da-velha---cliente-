/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package velha.cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author KaiqueDantas
 */
public class Cliente implements Runnable {
    
    private static final Set<String> COMANDOS;
    private static final Set<String> COMANDOS_JOGO;
    
    private boolean jogando;
    private Scanner scanner;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Set<Jogador> jogadores;
    
    static {
        Set<String> lista = new HashSet<String>();
        lista.add("listar");
        lista.add("convidar");
        lista.add("sair");
        
        Set<String> lista2 = new HashSet<String>(lista);
        lista2.add("jogada");
        lista2.add("listar jogadas");
        COMANDOS = Collections.unmodifiableSet(lista);
        COMANDOS_JOGO = Collections.unmodifiableSet(lista2);
    }
    
    @Override
    public void run() {
        
        scanner = new Scanner(System.in);
        
        try (Socket socket = new Socket(InetAddress.getLocalHost(), 10001)) {
            
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Digita o nome:");
            String nome = scanner.nextLine();
            
            out.writeUTF(nome.trim());
            
            comandoW:
            while (true) {
                System.out.println("Digite um comando da lista (" + (jogando ? COMANDOS_JOGO : COMANDOS).stream().collect(Collectors.joining("|")) + ")");
                String comando = scanner.nextLine().trim().toLowerCase();
                out.writeUTF(comando);
                out.flush();
                switch (comando) {
                    
                    case "jogada": {
                        realizarJogada();
                        break;
                    }
                    case "listar": {
                        listarJogadores();
                        break;
                    }
                    case "convidar": {
                        convidarJogador();
                        break;
                    }
                    case "listar jogadas": {
                        listarJogadas();
                        break;
                    }
                    case "sair": {
                        break comandoW;
                    }
                    
                    case "": {
                        break;
                    }
                    
                    default:
                        erro();
                    
                }
                out.flush();
                String mensagem = in.readUTF().trim().toLowerCase();
                out.writeUTF(mensagem);
                switch (mensagem) {
                    case "convite recebido": {
                        conviteRecebido();
                        break;
                        
                    }
                    case "aguardando": {
                        break;
                    }
                    
                }
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private void realizarJogada() throws IOException {
        if (!jogando) {
            return;
            
        }
        if (!in.readBoolean()) {
            System.out.println(in.readUTF());
            return;
        }
        
        System.out.println(in.readUTF());
        if (in.readBoolean()) {
            System.out.println("Jogo terminado, vencedor é: " + in.readUTF());
            jogando = false;
            return;
        }
        
        if (in.readBoolean()) {
            
            System.out.println("Qual a linha");
            int linha = scanner.nextInt();
            System.out.println("Qual a coluna");
            int coluna = scanner.nextInt();
            out.writeInt(linha);
            out.writeInt(coluna);
            out.flush();
            if (in.readBoolean()) {
                out.writeUTF(in.readUTF());
                
            } else {
                out.writeUTF(in.readUTF());
            }
            
        } else {
            
            System.out.println("Aguarde o seu turno!!!");
            
        }
        out.flush();
        String vencedor = in.readUTF().trim();
        
        System.out.println("Vencedor " + vencedor);
        
    }
    
    private void listarJogadores() throws IOException {
        int size = in.readInt();
        jogadores = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            Jogador jogador = new Jogador(in.readLong(), in.readUTF().trim(), in.readUTF().trim());
            jogadores.add(jogador);
            System.out.println(jogador);
            
        }
        
    }
    
    private void convidarJogador() throws IOException {
        
        System.out.println("digita o Id do jogador");
        long idJogador = scanner.nextLong();
        out.writeLong(idJogador);
        out.flush();
        if (in.readBoolean()) {
            jogando = true;
            System.out.println(in.readUTF());
        } else {
            
            System.out.println(in.readUTF());
        }
        
    }
    
    private void erro() {
        
        System.out.println("Comando inválido!!!");
        
    }
    
    private void conviteRecebido() throws IOException {
        
        String jogador = in.readUTF().trim();
        
        System.out.println("Jogador " + jogador + " convidou!!!");
        System.out.println("Digite [S] para aceitar ou qualquer outra coisa, para recusar.");
        String resposta = scanner.nextLine();
        boolean aceito = "S".equals(resposta);
        out.writeBoolean(aceito);
        out.flush();
        if (aceito) {
            
            jogando = true;
            
        }
    }
    
    private void listarJogadas() throws IOException {
        if (!in.readBoolean()) {
            System.out.println(in.readUTF());
            return;
        }
        
        System.out.println(in.readUTF());
        int tamanho = in.readInt();
        Map<String, String> jogadas = new HashMap<>(tamanho);
        for (int i = 0; i < tamanho; i++) {
            jogadas.put(in.readUTF(), in.readUTF());
        }
        tamanho = in.readInt();
        Map<Coordenada, String> campos = new HashMap<>(tamanho);
        for (int i = 0; i < tamanho; i++) {
            campos.put(new Coordenada(in.readInt(), in.readInt()), in.readUTF());
        }
        System.out.println(jogadas);
        for (int linha = 1; linha <= 3; linha++) {
            for (int coluna = 1; coluna <= 3; coluna++) {
                System.out.print(campos.getOrDefault(new Coordenada(linha, coluna), " "));
            }
            System.out.println();
        }
    }
    
}
