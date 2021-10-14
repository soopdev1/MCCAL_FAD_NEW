/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.engine;

/**
 *
 * @author rcosco
 */
public class CAD {

    int idcad;
    String nome, cognome, email, numero, giorno, orariostart, orarioend, password, stato;
    int iduser;

    public CAD(int idcad, String nome, String cognome, String email, String numero, String giorno, String orariostart, String orarioend, String password, String stato, int iduser) {
        this.idcad = idcad;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.numero = numero;
        this.giorno = giorno;
        this.orariostart = orariostart;
        this.orarioend = orarioend;
        this.password = password;
        this.stato = stato;
        this.iduser = iduser;
    }

    public int getIdcad() {
        return idcad;
    }

    public void setIdcad(int idcad) {
        this.idcad = idcad;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getGiorno() {
        return giorno;
    }

    public void setGiorno(String giorno) {
        this.giorno = giorno;
    }

    public String getOrariostart() {
        return orariostart;
    }

    public void setOrariostart(String orariostart) {
        this.orariostart = orariostart;
    }

    public String getOrarioend() {
        return orarioend;
    }

    public void setOrarioend(String orarioend) {
        this.orarioend = orarioend;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public int getIduser() {
        return iduser;
    }

    public void setIduser(int iduser) {
        this.iduser = iduser;
    }
    
    
}
