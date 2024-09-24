/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.so.engine;

/**
 *
 * @author srotella
 */
public class Fadroom {

    int idfad, iduser;
    String datacreazione, nomestanza, stato, partecipanti, password, fine, inizio,note;

    public Fadroom(int idfad, String datacreazione, String nomestanza, String stato, int iduser, String partecipanti, String password, String fine, String inizio,String note) {
        this.idfad = idfad;
        this.iduser = iduser;
        this.datacreazione = datacreazione;
        this.nomestanza = nomestanza;
        this.stato = stato;
        this.partecipanti = partecipanti;
        this.password = password;
        this.fine = fine;
        this.inizio = inizio;
        this.note = note;
    }
    
    
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    public int getIdfad() {
        return idfad;
    }

    public void setIdfad(int idfad) {
        this.idfad = idfad;
    }

    public int getIduser() {
        return iduser;
    }

    public void setIduser(int iduser) {
        this.iduser = iduser;
    }

    public String getDatacreazione() {
        return datacreazione;
    }

    public void setDatacreazione(String datacreazione) {
        this.datacreazione = datacreazione;
    }

    public String getNomestanza() {
        return nomestanza;
    }

    public void setNomestanza(String nomestanza) {
        this.nomestanza = nomestanza;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getPartecipanti() {
        return partecipanti;
    }

    public void setPartecipanti(String partecipanti) {
        this.partecipanti = partecipanti;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFine() {
        return fine;
    }

    public void setFine(String fine) {
        this.fine = fine;
    }

    public String getInizio() {
        return inizio;
    }

    public void setInizio(String inizio) {
        this.inizio = inizio;
    }

}
