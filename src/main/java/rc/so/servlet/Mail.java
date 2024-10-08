/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.so.servlet;

import com.google.common.io.Files;
import rc.so.engine.Action;
import static rc.so.engine.Action.estraiEccezione;
import static rc.so.engine.Action.formatStringtoStringDate;
import static rc.so.engine.Action.getRequestValue;
import static rc.so.engine.Action.log;
import static rc.so.engine.Action.pat_1;
import static rc.so.engine.Action.pat_10;
import static rc.so.engine.Action.pat_5;
import static rc.so.engine.Action.pathTEMP;
import static rc.so.engine.Action.titlepro;
import rc.so.engine.Database;
import rc.so.engine.Fadroom;
import rc.so.engine.GenericUser;
import rc.so.engine.SendMailJet;
import static rc.so.engine.SendMailJet.createEVENT;
import static rc.so.engine.SendMailJet.sendMailEvento;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.joda.time.DateTime;
import static rc.so.engine.SMS_SINCH.sendSmsFAD;

/**
 *
 * @author rcosco
 */
public class Mail extends HttpServlet {

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
        String cf = getRequestValue(request, "cf");
        String pr = getRequestValue(request, "pr");
        String st = getRequestValue(request, "st");
        List<GenericUser> usr = Action.get_UserProg(pr);
        String nomeprogettoform = Action.get_nomeProg(pr);
        String azioneform = Action.get_Path("linkfad");
        String datainvito = new DateTime().toString(pat_1);

        if (!usr.isEmpty()) {
            if (cf.equals("---")) {
                usr.forEach(user -> {
                    if (EmailValidator.getInstance().isValid(user.getEmail())) {
                        fadmail(nomeprogettoform, datainvito, user.getCognome() + " " + user.getNome(), azioneform, user.getCodicefiscale(), pr, user.getEmail(), st);
                        sendSmsFAD(user.getNome(), user.getCognome(), user.getNumero());
                        log.log(Level.INFO, "MAIL A: {0} -- {1} {2}", new Object[]{user.getEmail(), user.getCognome(), user.getNome()});
                        out.print("success");
                        out.flush();
                        out.close();
                    } else {
                        out.print("ERRORE MAIL NON VALIDA :" + user.getEmail());
                        out.flush();
                        out.close();
                    }
                });
            } else {
                if (usr.stream().anyMatch(us -> us.getIdallievi().equals(cf))) {
                    GenericUser user = usr.stream().filter(us -> us.getIdallievi().equals(cf)).findAny().get();
                    if (user != null) {
                        if (EmailValidator.getInstance().isValid(user.getEmail())) {
                            fadmail(nomeprogettoform, datainvito, user.getCognome() + " " + user.getNome(), azioneform, user.getCodicefiscale(), pr, user.getEmail(), st);
//                            sendSmsFAD(user.getNome(), user.getCognome(), user.getNumero());
                            log.log(Level.INFO, "MAIL SINGOLA A: {0} -- {1} {2}", new Object[]{user.getEmail(), user.getCognome(), user.getNome()});
                            out.print("success");
                            out.flush();
                            out.close();
                        } else {
                            out.print("ERRORE MAIL NON VALIDA :" + user.getEmail());
                            out.flush();
                            out.close();
                        }
                    }
                }
            }
        }

    }

    public static void fadmail_docente(String nomeprogettoform, String datainvito, String nomecognome, String azioneform, String codfiscuser, String proguser, String maildest, String stanza) {
        String mail = Action.get_Path("fadmail_docente");
        if (mail != null) {
            try {
                new File(pathTEMP).mkdirs();
                File temp = new File(pathTEMP + Action.generaId(75) + "_temp.html");
                FileUtils.writeByteArrayToFile(temp, Base64.decodeBase64(mail));
                String content = Files.asCharSource(temp, StandardCharsets.UTF_8).read();
                content = StringUtils.replace(content, "@nomeprogettoform", nomeprogettoform);
                content = StringUtils.replace(content, "@datainvito", datainvito);
                content = StringUtils.replace(content, "@nomecognome", nomecognome);
                content = StringUtils.replace(content, "@azioneform", azioneform);
                content = StringUtils.replace(content, "@codfiscuser", codfiscuser);
                content = StringUtils.replace(content, "@proguser", proguser + "&roomname=" + stanza);
                SendMailJet.sendMail(titlepro, new String[]{maildest}, content, "FAD - Promemoria");
                temp.delete();
            } catch (Exception ex) {
                log.severe(estraiEccezione(ex));
            }
        }
    }

    public static boolean fadmail(String nomeprogettoform, String datainvito, String nomecognome, String azioneform,
            String codfiscuser, String proguser, String maildest, String stanza) {
        String mail = Action.get_Path("fadmail");
        if (mail != null) {
            try {
                new File(pathTEMP).mkdirs();
                File temp = new File(pathTEMP + Action.generaId(75) + "_temp.html");

                FileUtils.writeByteArrayToFile(temp, Base64.decodeBase64(mail));
                String content = Files.asCharSource(temp, StandardCharsets.UTF_8).read();
                content = StringUtils.replace(content, "@nomeprogettoform", nomeprogettoform);
                content = StringUtils.replace(content, "@datainvito", datainvito);
                content = StringUtils.replace(content, "@nomecognome", nomecognome);
                content = StringUtils.replace(content, "@azioneform", azioneform);
                content = StringUtils.replace(content, "@codfiscuser", codfiscuser);
                content = StringUtils.replace(content, "@proguser", proguser + "&roomname=" + stanza);
                SendMailJet.sendMail(titlepro, new String[]{maildest}, content, "FAD - Promemoria");
                temp.delete();
                return true;
            } catch (Exception ex) {
                log.severe(estraiEccezione(ex));
            }
        }
        return false;
    }

    public static void fadmail_conference(String idfad, String maildest) {
        String[] mail = Action.get_Mail("conferenza");
        if (!mail[0].equals("")) {
            try {
                Fadroom fa = Action.getroom(idfad);
                if (fa != null) {
                    Database db = new Database(log);
                    String content = mail[1].replace("@redirect", db.get_Path("domino") + "redirect_out.jsp")
                            .replace("@link", db.get_Path("linkfad"))
                            .replace("@id", idfad)
                            .replace("@user", maildest)
                            .replace("@pwd", fa.getPassword())
                            .replace("@start", formatStringtoStringDate(fa.getInizio(), pat_5, pat_10))
                            .replace("@end", formatStringtoStringDate(fa.getInizio(), pat_5, pat_10))
                            .replace("@note", fa.getNote())
                            .replace("@email_tec", db.get_Path("emailtecnico"))
                            .replace("@email_am", db.get_Path("emailamministrativo"));

                    db.closeDB();

                    sendMailEvento(titlepro, new String[]{maildest},
                            content,
                            mail[0],
                            createEVENT(fa.getInizio(), fa.getFine(), mail[0]));
                }
            } catch (Exception ex) {
                log.severe(estraiEccezione(ex));
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
