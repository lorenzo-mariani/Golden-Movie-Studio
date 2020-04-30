package it.unipv.model;

/**
 * Oggetto che rappresenta il singolo utente.
 *       codice -> dato al momento della registrazione e visibile nell'area riservata, serve per reimpostare
 *                     la password se dimenticata.
 */
public class User implements Comparable<User> {
    private String nome;
    private String password;
    private String email;
    private String codice;

    public User() {}

    public User(String nome, String password) {
        this.nome = nome;
        this.password = password;
    }

    public User(String nome, String password, String email, String codice) {
        this.nome = nome;
        this.password = password;
        this.email = email;
        this.codice = codice;
    }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getCodice() { return codice; }

    public void setCodice(String codice) { this.codice = codice; }

    @Override
    public int compareTo(User o) {
        return this.getNome().compareToIgnoreCase(o.getNome());
    }

    @Override
    public String toString() {
        return
                "Username: " + this.nome + "\n"
              + "Password: " + this.password + "\n"
              + "E-mail " + this.email
              + "Codice " + this.codice;
    }
}
