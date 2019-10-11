
package velha.cliente;


public class Jogador {
    
    private String nome;
    private String status;
    private long id;
    

    public String getNome() {
        return nome;
    }

    public String getStatus() {
        return status;
    }
    
    public long getId() {
        return id;
    }
    
    

    public Jogador(long id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Jogador{id="+id + ", nome=" + nome + ", status=" + status + '}';
    }
    
    
    
}
