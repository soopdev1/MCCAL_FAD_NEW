/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import it.refill.engine.Action;
import static it.refill.engine.Action.cf_list_alunni;
import static it.refill.engine.Action.cf_list_docenti;
import static it.refill.engine.Action.getAllievo;
import static it.refill.engine.Action.getDocente;
import static it.refill.engine.Action.getNanoSecond;
import static it.refill.engine.Action.getRequestValue;
import static it.refill.engine.Action.getUserMC;
import static it.refill.engine.Action.getUserSA;
import static it.refill.engine.Action.get_Stanza;
import static it.refill.engine.Action.get_Stanza_conferenza_login;
import static it.refill.engine.Action.insertTR;
import static it.refill.engine.Action.log_ajax;
import static it.refill.engine.Action.parseINT;
import static it.refill.engine.Action.redirect;
import it.refill.engine.CAD;
import it.refill.engine.GenericUser;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.capitalize;
import org.joda.time.DateTime;

/**
 *
 * @author rcosco
 */
public class Login extends HttpServlet {

    protected void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getSession().invalidate();
        redirect(request, response, "login.jsp");
    }

    protected void loginconference(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Action.printRequest(request);
        //VERIFICHE
        String view = getRequestValue(request, "view", "0");
        String id = getRequestValue(request, "id");
        String user = getRequestValue(request, "user");
        String password = getRequestValue(request, "password");

        if (Action.test) {
            HttpSession se = request.getSession();
            se.setAttribute("us_nome", "TESTING USER");
            se.setAttribute("us_stanza", "Testing page");
            se.setAttribute("us_role", "USER");
            se.setAttribute("id_stanza", "1");
            redirect(request, response, "conference_all.jsp");
        } else {
            if (view.equals("0")) {
                String stanza = get_Stanza_conferenza_login(id, password, user);
                if (stanza != null) {
                    HttpSession se = request.getSession();
                    se.setAttribute("us_nome", user.toLowerCase().replaceAll("@", "_"));
                    se.setAttribute("us_stanza", StringUtils.deleteWhitespace(stanza));
                    se.setAttribute("id_stanza", id);
                    se.setAttribute("us_role", "USER");
                    redirect(request, response, "conference_all.jsp");
                } else {
                    redirect(request, response, "logerr.jsp");
                }
            } else {
                String stanza = get_Stanza_conferenza_login(id, password, null);
                HttpSession se = request.getSession();
                se.setAttribute("us_nome", user.toLowerCase().replaceAll("@", "_"));
                se.setAttribute("us_stanza", StringUtils.deleteWhitespace(stanza));
                se.setAttribute("id_stanza", id);
                se.setAttribute("us_role", "ADMIN");
                redirect(request, response, "conference_all.jsp");
            }
        }

    }

    protected void logindoc(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String codfisc = getRequestValue(request, "codfiscDC");
        insertTR("I", codfisc, "LOGIN");
        String stanza = StringUtils.deleteWhitespace(getRequestValue(request, "stanzaDC"));

        if (!codfisc.trim().equals("") && !stanza.trim().equals("")) {
            List<String> corretti = cf_list_docenti();
            if (corretti.contains(codfisc)) {
                GenericUser user = getDocente(codfisc);
                if (user != null) {
                    HttpSession se = request.getSession();
                    se.setAttribute("us_cod", user.getIdallievi());
                    se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                    se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                    se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                    se.setAttribute("us_stanza", stanza.toUpperCase());
                    se.setAttribute("us_role", "DOCENTE");
                    redirect(request, response, "conference.jsp");
                } else {
                    redirect(request, response, "login.jsp?error=1");

                }
            } else {
                redirect(request, response, "login.jsp?error=1");
            }
        } else {
            redirect(request, response, "login.jsp?error=2");
        }

    }

    protected void login_fad_mc_multi(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession se = request.getSession();

        String codfisc = StringUtils.deleteWhitespace(getRequestValue(request, "codfisc"));
        String progetto = StringUtils.deleteWhitespace(getRequestValue(request, "progetto"));
        String view = StringUtils.deleteWhitespace(getRequestValue(request, "view"));
        String roomname = StringUtils.deleteWhitespace(getRequestValue(request, "roomname"));
        String corso = StringUtils.deleteWhitespace(getRequestValue(request, "corso"));

        List<String> docenti = cf_list_docenti();
        List<String> alunni = cf_list_alunni();

        boolean ok = Action.verificaStanza(progetto, roomname);
        if (ok) {
            if (docenti.contains(codfisc)) {//DOCENTI
                redirect(request, response, "logerr.jsp");
            } else if (alunni.contains(codfisc)) { //ALUNNI
                redirect(request, response, "logerr.jsp");
            } else { //MC
                switch (view) {
                    case "1": {
                        GenericUser user = getUserMC(codfisc);
                        if (user != null) {
                            se.setAttribute("us_pro", progetto);
                            se.setAttribute("us_cod", user.getIdallievi());
                            se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                            se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                            se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                            se.setAttribute("us_stanza", roomname.toUpperCase());
                            se.setAttribute("us_corso", "CORSO " + corso);
                            se.setAttribute("us_role", "ADMINMC");
                            response.sendRedirect("conference.jsp");
                        } else {
                            redirect(request, response, "logerr.jsp");
                        }
                        break;
                    }
                    case "2": {
                        //SA
                        GenericUser user = getUserSA(codfisc);
                        if (user != null) {
                            se.setAttribute("us_pro", progetto);
                            se.setAttribute("us_cod", user.getIdallievi());
                            se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                            se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                            se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                            se.setAttribute("us_stanza", roomname.toUpperCase());
                            se.setAttribute("us_corso", "CORSO " + corso);
                            se.setAttribute("us_role", "SOGGETTO ATTUATORE");
                            response.sendRedirect("conference.jsp");
                        } else {
                            redirect(request, response, "logerr.jsp");
                        }
                        break;
                    }
                    default:
                        redirect(request, response, "logerr.jsp");
                        break;
                }
            }
        } else {
            redirect(request, response, "logerr.jsp");
        }

    }

    protected void login_fad_mc(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Action.printRequest(request);
        HttpSession se = request.getSession();
        String codfisc = StringUtils.deleteWhitespace(getRequestValue(request, "codfisc"));
        String progetto = StringUtils.deleteWhitespace(getRequestValue(request, "progetto"));
        String stanza = StringUtils.deleteWhitespace(getRequestValue(request, "roomname"));
        String view = StringUtils.deleteWhitespace(getRequestValue(request, "view"));

        List<String> docenti = cf_list_docenti();
        List<String> alunni = cf_list_alunni();

        if (!stanza.equals("")) {//NEW0
            if (docenti.contains(codfisc)) {
                //LOGIN DOC
                GenericUser user = getDocente(codfisc);
                if (user != null) {
                    se.setAttribute("us_cod", user.getIdallievi());
                    se.setAttribute("us_pro", progetto);
                    se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                    se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                    se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                    se.setAttribute("us_stanza", stanza.toUpperCase());
                    se.setAttribute("us_role", "DOCENTE");
                    redirect(request, response, "conference.jsp");
                } else {
                    redirect(request, response, "logerr.jsp");
                }
            } else if (alunni.contains(codfisc)) {
                //LOGIN ALUNNO
                GenericUser user = getAllievo(codfisc);
                if (user != null) {
                    se.setAttribute("us_cod", user.getIdallievi());
                    se.setAttribute("us_pro", progetto);
                    se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                    se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                    se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                    se.setAttribute("us_stanza", stanza.toUpperCase());
                    se.setAttribute("us_role", "ALLIEVO");
                    log_ajax("L1", stanza.toUpperCase(), "ALLIEVO:" + user.getIdallievi(), getNanoSecond());
                    redirect(request, response, "conference.jsp");
                } else {
                    redirect(request, response, "logerr.jsp");
                }
            } else {
                redirect(request, response, "logerr.jsp");
            }
        } else { //COME PRIMA
            stanza = get_Stanza(progetto);
            if (stanza != null) {
                if (docenti.contains(codfisc)) {
                    //LOGIN DOC
                    GenericUser user = getDocente(codfisc);
                    if (user != null) {
                        se.setAttribute("us_cod", user.getIdallievi());
                        se.setAttribute("us_pro", progetto);
                        se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                        se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                        se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                        se.setAttribute("us_stanza", stanza.toUpperCase());
                        se.setAttribute("us_role", "DOCENTE");
                        redirect(request, response, "conference.jsp");
                    } else {
                        redirect(request, response, "logerr.jsp");
                    }
                } else if (alunni.contains(codfisc)) {
                    //LOGIN ALUNNO
                    GenericUser user = getAllievo(codfisc);
                    if (user != null) {
                        se.setAttribute("us_cod", user.getIdallievi());
                        se.setAttribute("us_pro", progetto);
                        se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                        se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                        se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                        se.setAttribute("us_stanza", stanza.toUpperCase());
                        se.setAttribute("us_role", "ALLIEVO");
                        log_ajax("L1", stanza.toUpperCase(), "ALLIEVO:" + user.getIdallievi(), getNanoSecond());
                        redirect(request, response, "conference.jsp");
                    } else {
                        redirect(request, response, "logerr.jsp");
                    }
                } else {
                    if (view.equals("1")) {
                        GenericUser user = getUserMC(codfisc);
                        if (user != null) {
                            se.setAttribute("us_pro", progetto);
                            se.setAttribute("us_cod", user.getIdallievi());
                            se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                            se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                            se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                            se.setAttribute("us_stanza", stanza.toUpperCase());
                            se.setAttribute("us_role", "ADMINMC");
                            response.sendRedirect("conference.jsp");
                        } else {
                            redirect(request, response, "logerr.jsp");
                        }

                    } else {
                        redirect(request, response, "logerr.jsp");
                    }
                }
            } else {
                redirect(request, response, "logerr.jsp");
            }
        }

    }

    protected void login_cad(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Action.printRequest(request);
        //VERIFICHE
        String view = getRequestValue(request, "view", "0");
        String id = getRequestValue(request, "id");
        String user = getRequestValue(request, "user");
        String password = getRequestValue(request, "password");

        if (Action.test) {
            HttpSession se = request.getSession();
            se.setAttribute("us_nome", "LUANA MARINO");
            se.setAttribute("us_stanza", "CAD_3");
            se.setAttribute("us_role", "USER");
            se.setAttribute("id_stanza", "3");
            redirect(request, response, "conference_cad.jsp");
        } else {
            CAD room = Action.getroom(parseINT(id));
            if (room != null && room.getStato().equals("0")) {
                if (view.equals("0")) {
                    if (room.getEmail().toLowerCase().equals(user) && room.getPassword().equals(password)) {
                        ///////////////////////////////////////////////////////////////////
                        HttpSession se = request.getSession();
                        se.setAttribute("us_nome", room.getNome().toUpperCase());
                        se.setAttribute("us_cognome", room.getCognome().toUpperCase());
                        se.setAttribute("us_stanza", "CAD_" + room.getIdcad() + "_" + new DateTime().toString("yyyyMMdd"));
                        se.setAttribute("id_stanza", room.getIdcad());
                        se.setAttribute("us_role", "USER");
                        redirect(request, response, "conference_cad.jsp");
                    } else {
                        redirect(request, response, "logerr.jsp");
                    }
                } else {
                    GenericUser us = Action.getUser(user);
                    if (us != null && room.getPassword().equals(password)) {
                        HttpSession se = request.getSession();
                        se.setAttribute("us_nome", us.getNome());
                        se.setAttribute("us_cognome", "");
                        se.setAttribute("us_stanza", "CAD_" + room.getIdcad() + "_" + new DateTime().toString("yyyyMMdd"));
                        se.setAttribute("id_stanza", room.getIdcad());
                        se.setAttribute("us_role", "ADMIN");
                        redirect(request, response, "conference_cad.jsp");
                    } else {
                        redirect(request, response, "logerr.jsp");
                    }
                }
            } else {
                redirect(request, response, "logerr.jsp");
            }

        }

    }

    protected void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String codfisc = getRequestValue(request, "codfisc");
        insertTR("I", codfisc, "LOGIN");
        String stanza = StringUtils.deleteWhitespace(getRequestValue(request, "stanza"));
        if (!codfisc.trim().equals("") && !stanza.trim().equals("")) {
            List<String> corretti = cf_list_alunni();
            if (corretti.contains(codfisc)) {
                GenericUser user = getAllievo(codfisc);
                if (user != null) {
                    HttpSession se = request.getSession();
                    se.setAttribute("us_cod", user.getIdallievi());
                    se.setAttribute("us_nome", capitalize(user.getNome().toLowerCase()));
                    se.setAttribute("us_cognome", capitalize(user.getCognome().toLowerCase()));
                    se.setAttribute("us_cf", user.getCodicefiscale().toUpperCase());
                    se.setAttribute("us_stanza", stanza.toUpperCase());
                    se.setAttribute("us_role", "ALLIEVO");
                    log_ajax("L1", stanza.toUpperCase(), "ALLIEVO:" + user.getIdallievi(), getNanoSecond());
                    redirect(request, response, "conference.jsp");
                } else {
                    redirect(request, response, "login.jsp?error=1");
                }
            } else {
                redirect(request, response, "login.jsp?error=1");
            }
        } else {
            redirect(request, response, "login.jsp?error=2");
        }

    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            String type = request.getParameter("type");
            switch (type) {
                case "login":
                    login(request, response);
                    break;
                case "logindoc":
                    logindoc(request, response);
                    break;
                case "login_conference":
                    loginconference(request, response);
                    break;
                case "login_fad_mc":
                    login_fad_mc(request, response);
                    break;
                case "login_cad":
                    login_cad(request, response);
                    break;
                case "login_fad_mc_multi":
                    login_fad_mc_multi(request, response);
                    break;
                default:
                    break;
            }
        } catch (ServletException | IOException ex) {
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
