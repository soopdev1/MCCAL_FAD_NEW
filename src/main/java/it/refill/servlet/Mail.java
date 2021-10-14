/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import com.google.common.io.Files;
import it.refill.engine.Action;
import static it.refill.engine.Action.formatStringtoStringDate;
//import static it.refill.engine.Action.azioneform;
import static it.refill.engine.Action.getRequestValue;
import static it.refill.engine.Action.log;
import static it.refill.engine.Action.pat_1;
import static it.refill.engine.Action.pat_10;
import static it.refill.engine.Action.pat_5;
import static it.refill.engine.Action.pathTEMP;
import it.refill.engine.Database;
import it.refill.engine.Fadroom;
import it.refill.engine.GenericUser;
import it.refill.engine.SendMailJet;
import static it.refill.engine.SendMailJet.createEVENT;
import static it.refill.engine.SendMailJet.sendMailEvento;
import it.refill.engine.Sms;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.joda.time.DateTime;

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

        if (usr.size() > 0) {
            if (cf.equals("---")) {
                usr.forEach(user -> {
                    if (EmailValidator.getInstance().isValid(user.getEmail())) {
                        fadmail(nomeprogettoform, datainvito, user.getCognome() + " " + user.getNome(), azioneform, user.getCodicefiscale(), pr, user.getEmail(), st);
                        Sms.sendSmsFAD(user.getNome(), user.getCognome(), user.getNumero());
                        System.out.println("MAIL A: " + user.getEmail() + " -- " + user.getCognome() + " " + user.getNome());
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
                            Sms.sendSmsFAD(user.getNome(), user.getCognome(), user.getNumero());
                            System.out.println("MAIL SINGOLA A: " + user.getEmail() + " -- " + user.getCognome() + " " + user.getNome());
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
                SendMailJet.sendMail("Microcredito", new String[]{maildest}, content, "FAD - Promemoria");
                temp.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
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
                SendMailJet.sendMail("Microcredito", new String[]{maildest}, content, "FAD - Promemoria");

                System.out.println("it.refill.servlet.Mail.fadmail() " + temp.getPath());

//                temp.delete();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

//    public static void main(String[] args) {
//        try {
//            String mail = Action.get_Path("fadmail");
//            new File(pathTEMP).mkdirs();
//            File temp = new File(pathTEMP + Action.generaId(75) + "_temp.html");
//
//            FileUtils.writeByteArrayToFile(temp, Base64.decodeBase64(mail));
//            System.out.println(temp.getPath());
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }

    public static void fadmail_conference(String idfad, String maildest) {
        String[] mail = Action.get_Mail("conferenza");
        if (!mail[0].equals("")) {
            try {
                Fadroom fa = Action.getroom(idfad);
                if (fa != null) {
                    Database db = new Database(log);
//                    
//
//                    String content = mail[1];
//                    content = StringUtils.replace(content, "@redirect", db.get_Path("domino") + "redirect_out.jsp");
//                    content = StringUtils.replace(content, "@link", db.get_Path("linkfad"));
//                    content = StringUtils.replace(content, "@id", idfad);
//                    content = StringUtils.replace(content, "@user", maildest);
//                    content = StringUtils.replace(content, "@pwd", fa.getPassword());
//                    content = StringUtils.replace(content, "@email_tec", db.get_Path("emailtecnico"));
//                    content = StringUtils.replace(content, "@email_am", db.get_Path("emailamministrativo"));

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

                    sendMailEvento("Microcredito", new String[]{maildest},
                            content,
                            mail[0],
                            createEVENT(fa.getInizio(), fa.getFine(), mail[0]));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

//    public static void main(String[] args) {
//        String mail = Action.get_Path("fadmail");
//        if (mail != null) {
//            try {
//                String pathtemp = "/mnt/temp/";
//                new File(pathtemp).mkdirs();
//                File temp = new File(pathtemp + Action.generaId(75) + "_temp.html");
//                FileUtils.writeByteArrayToFile(temp, Base64.decodeBase64(mail));
//                String content = Files.asCharSource(temp, StandardCharsets.UTF_8).read();
//                content = StringUtils.replace(content, "@nomeprogettoform", "");
//                content = StringUtils.replace(content, "@datainvito", "");
//                content = StringUtils.replace(content, "@nomecognome", "");
//                content = StringUtils.replace(content, "@azioneform", azioneform);
//                content = StringUtils.replace(content, "@codfiscuser", "");
//                content = StringUtils.replace(content, "@proguser", "3" + "&roomname=" + "3");
//                SendMailJet.sendMail("Microcredito", new String[]{"rcosco@setacom.it"}, content, "FAD - Promemoria");
//                temp.delete();
////            } catch (IOException ex) {
//            } catch (MailjetException | MailjetSocketTimeoutException | IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//    
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
