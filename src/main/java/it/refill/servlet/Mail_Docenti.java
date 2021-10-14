/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import it.refill.engine.Action;
import static it.refill.engine.Action.getRequestValue;
import static it.refill.engine.Action.pat_1;
import it.refill.engine.GenericUser;
import static it.refill.servlet.Mail.fadmail_docente;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.validator.routines.EmailValidator;
import org.joda.time.DateTime;

/**
 *
 * @author rcosco
 */
public class Mail_Docenti extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        String id_docente = getRequestValue(request, "cf");
        String id_progetto = getRequestValue(request, "pr");
        String st = getRequestValue(request, "st");

        List<GenericUser> lista_docenti = Action.get_DocProg(id_progetto);
        String nomeprogettoform = Action.get_nomeProg(id_progetto);
        String azioneform = Action.get_Path("linkfad");
        String datainvito = new DateTime().toString(pat_1);

        if (lista_docenti.stream().anyMatch(us -> us.getIdallievi().equals(id_docente))) {
            GenericUser docente = lista_docenti.stream().filter(us -> us.getIdallievi().equals(id_docente)).findAny().get();
            if (docente != null) {
                String maildest = docente.getEmail();
                //maildest = "rcosco@setacom.it";
                if (!EmailValidator.getInstance().isValid(maildest)) {
                    out.print("MAIL NON VALIDA :" + docente.getEmail());
                    out.flush();
                    out.close();
                } else {
                    fadmail_docente(nomeprogettoform, datainvito, docente.getCognome() + " " + docente.getNome(), azioneform, docente.getCodicefiscale(), id_progetto, maildest, st);
                    System.out.println("MAIL DOCENTE A: " + maildest + " -- " + docente.getCognome() + " " + docente.getNome());
                    out.print("success");
                    out.flush();
                    out.close();
                }
            } else {
                out.print("MAIL NON TROVATA");
                out.flush();
                out.close();
            }
        } else {
            out.print("MAIL NON TROVATA");
            out.flush();
            out.close();
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
