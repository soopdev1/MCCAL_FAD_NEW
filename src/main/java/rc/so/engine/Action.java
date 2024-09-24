/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.so.engine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author rcosco
 */
public class Action {

    public static final ResourceBundle conf = ResourceBundle.getBundle("conf.conf");

    public static final boolean test = true;

    public static final Logger log = createLog(conf.getString("name.app"));

    public static final String pathTEMP = conf.getString("path.temp");
    public static final String pathLOG = conf.getString("path.log");
    public static final String titlepro = conf.getString("titolo");
    public static final String pat_1 = "dd/MM/yyyy HH:mm:ss";
    public static final String pat_2 = "dd/MM/yyyy";
    public static final String pat_3 = "HHmmss";
    public static final String pat_4 = "ddMMyyyy";
    public static final String pat_5 = "yyyy-MM-dd HH:mm:ss";
    public static final String pat_6 = "yyyy-MM-dd";
    public static final String pat_7 = "yyyyMMddHHmmssSSS";
    public static final String pat_8 = "ddMMyyyyHHmmss";
    public static final String pat_9 = "yyMMddHHmmssSSS";
    public static final String pat_10 = "dd/MM/yyyy HH:mm";

    private static Logger createLog(String appname) {
        Logger logger = Logger.getLogger(appname);
        try {
            String dataOdierna = new DateTime().toString(pat_4);
            String pathS = pathLOG;
            File logdir = new File(pathS);
            if (!logdir.exists()) {
                logdir.mkdirs();
            }
            String ora = new DateTime().toString(pat_3);
            String pathLog = pathS + dataOdierna;
            File dirLog = new File(pathLog);
            if (!dirLog.exists()) {
                dirLog.mkdirs();
            }
            FileHandler fh = new FileHandler(pathLog + File.separator + appname + "_" + ora + ".log", true);
            logger.addHandler(fh);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
        }
        return logger;
    }

    public static String getDomainFAD() {
        return get_Path("linkfad_FL");
    }

    public static String formatStringtoStringDate(String dat, String pattern1, String pattern2) {
        try {
            if (dat.length() == 21) {
                dat = dat.substring(0, 19);
            }
            if (dat.length() == pattern1.length()) {
                DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern1);
                DateTime dt = formatter.parseDateTime(dat);
                return dt.toString(pattern2, Locale.ITALY);
            }
        } catch (IllegalArgumentException ex) {
        }
        return "No correct date";
    }

    public static void printRequest(HttpServletRequest request) throws ServletException, IOException {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            for (String paramValue : paramValues) {
                log.log(Level.INFO, "NORMAL FIELD - {0} : {1}", new Object[]{paramName, new String(paramValue.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)});
            }
        }
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List items = upload.parseRequest(request);
                Iterator iterator = items.iterator();
                while (iterator.hasNext()) {
                    FileItem item = (FileItem) iterator.next();
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String value = new String(item.getString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                        log.log(Level.INFO, "MULTIPART FIELD - {0} : {1}", new Object[]{fieldName, value});
                    } else {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getName();
                        log.log(Level.INFO, "MULTIPART FILE - {0} : {1}", new Object[]{fieldName, fieldValue});
                    }
                }
            } catch (Exception ex) {
                log.severe(estraiEccezione(ex));
            }
        }
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String destination) throws ServletException, IOException {

        String domain = StringUtils.replace(Action.get_Path("linkfad"), "Login", "");

        if (test) {
            domain = "";
        }

        if (response.isCommitted()) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(domain + destination);
            dispatcher.forward(request, response);
        } else {
            response.sendRedirect(domain + destination);
        }

    }

    public static String getRequestValue(HttpServletRequest request, String fieldname) {
        String out = request.getParameter(fieldname);
        if (out == null) {
            out = "";
        } else {
            out = out.trim();
        }
        return out;
    }

    public static String getRequestValue(HttpServletRequest request, String fieldname, String defvalue) {
        String out = request.getParameter(fieldname);
        if (out == null) {
            out = defvalue;
        } else {
            out = out.trim();
        }
        return out;
    }

    public static String getSessionValue(HttpSession session, String fieldname) {
        String out = (String) session.getAttribute(fieldname);
        if (out == null) {
            out = "";
        } else {
            try {
                out = out.trim().toUpperCase();
            } catch (Exception e) {
                out = "";
            }
        }
        return out;
    }

    public static String[] formatEsito(String esito) {
        if (esito.startsWith("OK") || esito.startsWith("true")) {
            String[] out = {"alert-success", "<i class='fas fa-check'></i> Operazione completata con successo."};
            return out;
        } else {
            if (esito.startsWith("passerr")) {
                if (esito.equalsIgnoreCase("passerr1")) {
                    String[] out = {"alert-danger", "<i class='fas fa-exclamation-triangle'></i>"
                        + " La 'password attuale' inserita risulta essere errata. Riprovare."};
                    return out;
                } else if (esito.equalsIgnoreCase("passerr2")) {
                    String[] out = {"alert-danger", "<i class='fas fa-exclamation-triangle'></i>"
                        + " 'Nuova password' e 'Conferma nuova password' non coincidono. Riprovare."};
                    return out;
                } else if (esito.equalsIgnoreCase("passerr3")) {
                    String[] out = {"alert-danger", "<i class='fas fa-exclamation-triangle'></i>"
                        + " La 'Nuova password' deve essere diversa dalla 'password attuale'. Riprovare."};
                    return out;
                } else if (esito.equalsIgnoreCase("passerr4")) {
                    String[] out = {"alert-danger", "<i class='fas fa-exclamation-triangle'></i>"
                        + " La 'Nuova password' non soddisfa le caratteristiche minime indicate. Riprovare."};
                    return out;
                }
            }

            String[] out = {"alert-danger", "<i class='fas fa-exclamation-triangle'></i>"
                + " Si &#232; verificato un errore durante l'operazione. Riprovare."};
            return out;
        }
    }

    public static String formatLoginError(String err) {
        if (err.equals("1")) {
            return "UTENTE NON TROVATO";
        } else if (err.equals("2") || err.equals("3")) {
            return "'USERNAME' E 'PASSWORD' SONO OBBLIGATORI";
        }
        return "ERRORE GENERICO";
    }

    public static void insertTR(String type, HttpSession session, String descr) {
        String user = (String) session.getAttribute("us_cod");
        if (user == null) {
            user = "service";
        }

        Database db = new Database(log);
        db.insertTR(type, user, descr);
        db.closeDB();
    }

    public static void insertTR(String type, String user, String descr) {
        Database db = new Database(log);
        db.insertTR(type, user, descr);
        db.closeDB();
    }

    public static List<GenericUser> get_UserProg(String pr) {
        Database db = new Database(log);
        List<GenericUser> out = db.get_UserProg(pr, false);
        db.closeDB();
        return out;
    }

    public static List<GenericUser> get_UserProgMAILOK(String pr) {
        Database db = new Database(log);
        List<GenericUser> out = db.get_UserProg(pr, true);
        db.closeDB();
        return out;
    }

    public static List<GenericUser> get_DocProg(String pr) {
        Database db = new Database(log);
        List<GenericUser> out = db.get_DocProg(pr, false);
        db.closeDB();
        return out;
    }

    public static List<GenericUser> get_DocProgMAILOK(String pr) {
        Database db = new Database(log);
        List<GenericUser> out = db.get_DocProg(pr, true);
        db.closeDB();
        return out;
    }

    public static List<String> cf_list_alunni() {
        Database db = new Database(log);
        List<String> out = db.cf_list("allievi");
        db.closeDB();
        return out;
    }

    public static List<String> cf_list_docenti() {
        Database db = new Database(log);
        List<String> out = db.cf_list("docenti");
        db.closeDB();
        return out;
    }

    public static List<String> getMailFromConference(String stanza) {
        Database db = new Database(log);
        List<String> out = db.getMailFromConference(stanza);
        db.closeDB();
        return out;
    }

    public static String getPswFromConference(String stanza) {
        Database db = new Database(log);
        String out = db.getPswFromConference(stanza);
        db.closeDB();
        return out;
    }

    public static GenericUser getAllievo(String cf) {
        Database db = new Database(log);
        GenericUser out = db.getAllievo(cf);
        db.closeDB();
        return out;
    }

    public static GenericUser getUserMC(String username) {
        Database db = new Database(log);
        GenericUser out = db.getUserMC(username);
        db.closeDB();
        return out;
    }

    public static GenericUser getUser(int id) {
        Database db = new Database(log);
        GenericUser out = db.getUser(id);
        db.closeDB();
        return out;
    }

    public static GenericUser getUser(String mail) {
        Database db = new Database(log);
        GenericUser out = db.getUser(mail);
        db.closeDB();
        return out;
    }

    public static GenericUser getUserSA(String username) {
        Database db = new Database(log);
        GenericUser out = db.getUserSA(username);
        db.closeDB();
        return out;
    }

    public static GenericUser getDocente(String cf) {
        Database db = new Database(log);
        GenericUser out = db.getDocente(cf);
        db.closeDB();
        return out;
    }

    public static String getNanoSecond() {
        Database db = new Database(log);
        String out = db.getNanoSecond();
        db.closeDB();
        return out;
    }

    public static String get_Stanza(String idpr) {
        Database db = new Database(log);
        String out = db.get_Stanza(idpr);
        db.closeDB();
        return out;
    }

    public static boolean verificaStanza(String idpr, String nomestanza) {
        Database db = new Database(log);
        boolean out = db.verificaStanza(idpr, nomestanza);
        db.closeDB();
        return out;
    }

    public static String get_Stanza_conferenza_login(String id, String pwd, String email) {
        Database db = new Database(log);
        String out = db.get_Stanza_conferenza_login(id, pwd, email);
        db.closeDB();
        return out;
    }

    public static String get_Stanza_CAD_login(String id, String pwd, String email) {
        Database db = new Database(log);
        String out = db.get_Stanza_CAD_login(id, pwd, email);
        db.closeDB();
        return out;
    }

    public static String get_nomeProg(String idpr) {
        Database db = new Database(log);
        String out = db.get_nomeProg(idpr);
        db.closeDB();
        return out;
    }

    public static String get_Path(String id) {
        Database db = new Database(log);
        String out = db.get_Path(id);
        db.closeDB();
        return out;
    }

    public static Fadroom getroom(String id) {
        Database db = new Database(log);
        Fadroom out = db.getroom(id);
        db.closeDB();
        return out;
    }

    public static CAD getroom(int id) {
        Database db = new Database(log);
        CAD out = db.getroom(id);
        db.closeDB();
        return out;
    }

    public static String[] get_Mail(String id) {
        Database db = new Database(log);
        String[] out = db.get_Mail(id);
        db.closeDB();
        return out;
    }

    public static void log_ajax(String type, String room, String action, String date) {
        Database db = new Database(log);
        db.log_ajax(type, room, action, date);
        db.closeDB();
    }

    public static String generaId(int length) {
        String random = RandomStringUtils.randomAlphanumeric(length - 15).trim();
        return new DateTime().toString(pat_9) + random;
    }

    public static String get_Uri(HttpServletRequest request) {
        String uri = request.getScheme() + "://"
                + request.getServerName()
                + ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort())
                + request.getRequestURI();
        return uri;
    }

    public static int parseINT(String ing) {
        try {
            return Integer.parseInt(ing);
        } catch (Exception e) {
        }
        return 0;
    }
    
    public static String estraiEccezione(Exception ec1) {
        try {
            String stack_nam = ec1.getStackTrace()[0].getMethodName();
            String stack_msg = ExceptionUtils.getStackTrace(ec1);
            return stack_nam + " - " + stack_msg;
        } catch (Exception e) {
        }
        return ec1.getMessage();

    }
}
