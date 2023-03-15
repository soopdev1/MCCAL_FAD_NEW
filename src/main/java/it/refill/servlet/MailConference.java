/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import it.refill.engine.Action;
import static it.refill.engine.Action.getRequestValue;
import static it.refill.servlet.Mail.fadmail_conference;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.validator.routines.EmailValidator;

/**
 *
 * @author rcosco
 */
public class MailConference extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String mailaddress = getRequestValue(request, "mail");
        String id_stanza = getRequestValue(request, "st");
        
        
        if (mailaddress.equals("---")) {
            List<String> usr = Action.getMailFromConference(id_stanza);
            usr.forEach(user -> {
                if (EmailValidator.getInstance().isValid(user)) {
                    fadmail_conference(id_stanza, user);
                    out.print("success");
                    out.flush();
                    out.close();
                } else {
                    out.print("ERRORE MAIL NON VALIDA :" + user);
                    out.flush();
                    out.close();
                }
            });
        } else {
            if (EmailValidator.getInstance().isValid(mailaddress)) {
                fadmail_conference(id_stanza, mailaddress);
                out.print("success");
                out.flush();
                out.close();
            } else {
                out.print("ERRORE MAIL NON VALIDA :" + mailaddress);
                out.flush();
                out.close();
            }
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
