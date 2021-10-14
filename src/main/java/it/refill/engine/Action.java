/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static it.refill.engine.Action.parseINT;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author rcosco
 */
public class Action {

    public static final boolean test = false;
    
    public static final Logger log = createLog("MC_API");
    
    public static final String pathTEMP = "/mnt/Microcredito/temp/";
    public static final String pathLOG = "/mnt/Microcredito/log/";
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
        } catch (IOException | SecurityException ex) {
            logger.severe(ex.getMessage());
        }
        return logger;
    }

    public static String getDomainFAD() {
//        return "fadmcar.servizi.link";
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
                System.out.println("NORMAL FIELD - " + paramName + " : " + new String(paramValue.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
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
                        System.out.println("MULTIPART FIELD - " + fieldName + " : " + value);
                    } else {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getName();
                        System.out.println("MULTIPART FILE - " + fieldName + " : " + fieldValue);
                    }
                }
            } catch (FileUploadException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void redirect(HttpServletRequest request, HttpServletResponse response, String destination) throws ServletException, IOException {

//        String domain = "https://fad.servizi.link/";
        String domain = StringUtils.replace(Action.get_Path("linkfad"), "Login", "");
//        String domain = "";

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
            out = out.trim().toUpperCase();
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

    private static String sendSMS(String cell, String msg, String type) {
        try {
            String[] recipients = new String[]{"39" + cell};

            String username = "yisucal.supporto@microcredito.gov.it";
            String password = "Calabria2020$$";

            // Invio SMS Classic con mittente personalizzato di tipo alfanumerico
            String result = skebbyGatewaySendSMS(username, password, recipients, msg, type, null, "yisucal");
            // Invio SMS Basic
            // String result = skebbyGatewaySendSMS(username, password, recipients, msg, "send_sms_basic", null, null);
            // Invio SMS Classic con mittente personalizzato di tipo numerico
            // String result = skebbyGatewaySendSMS(username, password, recipients, "Hi Mike, how are you?", "send_sms_classic", "393471234567", null);
            // Invio SMS Classic con notifica(report) con mittente personalizzato di tipo alfanumerico - Invio SMS Classic Plus
            // String result = skebbyGatewaySendSMS(username, password, recipients, "Hi Mike, how are you?", "send_sms_classic_report", null, "John");
            // Invio SMS Classic con notifica(report) con mittente personalizzato di tipo numerico - Invio SMS Classic Plus
            // String result = skebbyGatewaySendSMS(username, password, recipients, "Hi Mike, how are you?", "send_sms_classic_report", "393471234567", null);
            // ------------------------------------------------------------------
            // Controlla la documentazione completa all'indirizzo http://www.skebby.it/business/index/send-docs/ 
            // ------------------------------------------------------------------
            // Per i possibili errori si veda http://www.skebby.it/business/index/send-docs/#errorCodesSection
            // ATTENZIONE: in caso di errore Non si deve riprovare l'invio, trattandosi di errori bloccanti
            // ------------------------------------------------------------------       
            //System.out.println("result: " + result);
            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "failed";
        }
    }

    private static String skebbyGatewaySendSMS(String username, String password, String[] recipients, String text, String smsType, String senderNumber, String senderString) throws IOException {
        return skebbyGatewaySendSMS(username, password, recipients, text, smsType, senderNumber, senderString, "ISO-8859-1");
    }

    private static String skebbyGatewaySendSMS(String username, String password, String[] recipients, String text, String smsType,
            String senderNumber, String senderString, String charset) throws IOException {
        if (!charset.equals("UTF-8") && !charset.equals("ISO-8859-1")) {
            throw new IllegalArgumentException("Charset not supported.");
        }
        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("method", smsType));
        formparams.add(new BasicNameValuePair("username", username));
        formparams.add(new BasicNameValuePair("password", password));
        if (null != senderNumber) {
            formparams.add(new BasicNameValuePair("sender_number", senderNumber));
        }
        if (null != senderString) {
            formparams.add(new BasicNameValuePair("sender_string", senderString));
        }

        for (String recipient : recipients) {
            formparams.add(new BasicNameValuePair("recipients[]", recipient));
        }
        formparams.add(new BasicNameValuePair("text", text));
        formparams.add(new BasicNameValuePair("charset", charset));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, charset);
        String endpoint = "https://gateway.skebby.it/api/send/smseasy/advanced/http.php";
        HttpPost post = new HttpPost(endpoint);
        post.setEntity(entity);
        HttpResponse response = HttpClientBuilder.create().build().execute(post);
        HttpEntity resultEntity = response.getEntity();
        if (null != resultEntity) {
            return EntityUtils.toString(resultEntity);
        }
        return null;

    }

    private static String[] login(String username, String password, String BASEURL) throws IOException {
        URL url = new URL(BASEURL + "/login?username=" + username + "&password=" + password);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        BufferedReader br
                = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = "";
        String output;
        while ((output = br.readLine()) != null) {
            response += output;
        }
        conn.disconnect();

        String[] parts = response.split(";");
        return parts;
    }

    private static boolean sendSMS(String[] authKeys, SendSMSRequest sendSMS, String BASEURL) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        URL url = new URL(BASEURL + "/sms");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Sending an SMS requires authentication
        conn.setRequestProperty("user_key", authKeys[0]);
        conn.setRequestProperty("Session_key", authKeys[1]);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setDoOutput(true);

        String payload = gson.toJson(sendSMS);

        OutputStream os = conn.getOutputStream();
        os.write(payload.getBytes());
        os.flush();

        if (conn.getResponseCode() != 201) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }

        BufferedReader br
                = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        String response = "";
        String output;
        while ((output = br.readLine()) != null) {
            response += output;
        }
        conn.disconnect();

        SendSMSResponse responseObj = gson.fromJson(response, SendSMSResponse.class);

        return responseObj.isValid();
    }

    public static boolean newSkebby(String cell, String msg) {
        try {
            String BASEURL = "https://api.skebby.it/API/v1.0/REST/";
            String MESSAGE_HIGH_QUALITY = "TI";
            String username = "yisucal.supporto@microcredito.gov.it";
            String password = "Calabria2020$$";
            String[] authKeys = login(username, password, BASEURL);
            SendSMSRequest sendSMS = new SendSMSRequest();
            sendSMS.setMessage(msg);
            sendSMS.setMessageType(MESSAGE_HIGH_QUALITY);
            sendSMS.addRecipient("+39" + cell);
            sendSMS.setSender("yisucal");
            return sendSMS(authKeys, sendSMS, BASEURL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static int parseINT(String ing) {
        try {
            return Integer.parseInt(ing);
        } catch (Exception e) {
        }
        return 0;
    }

    public static void main(String[] args) {
        
        Sms.sendSmsFAD("SIMONE", "COSCO", "3286137172");
        
    }

}

class SendSMSRequest {

    /**
     * The message body
     */
    private String message;

    /**
     * The message type
     */
    private String message_type = "GP";

    /**
     * Should the API return the remaining credits?
     */
    private boolean returnCredits = false;

    /**
     * The list of recipients
     */
    private List<String> recipient = new ArrayList<>();

    /**
     * The sender Alias (TPOA)
     */
    private String sender = null;

    /**
     * Postpone the SMS message sending to the specified date
     */
    private Date scheduled_delivery_time = null;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return message_type;
    }

    public void setMessageType(String messageType) {
        this.message_type = messageType;
    }

    public boolean isReturnCredits() {
        return returnCredits;
    }

    public void setReturnCredits(boolean returnCredits) {
        this.returnCredits = returnCredits;
    }

    public List<String> getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Date getScheduledDeliveryTime() {
        return scheduled_delivery_time;
    }

    public void setScheduledDeliveryTime(Date scheduled_delivery_time) {
        this.scheduled_delivery_time = scheduled_delivery_time;
    }

    public void addRecipient(String recipient) {
        this.recipient.add(recipient);
    }
}

/**
 * This class represents the API Response. It is automatically created starting
 * from the JSON object returned by the server, using GSon
 */
class SendSMSResponse {

    private String result;
    private String order_id;
    private int total_sent;
    private int remaining_credits;
    private String internal_order_id;

    public String getResult() {
        return result;
    }

    public String getOrderId() {
        return order_id;
    }

    public int getTotalSent() {
        return total_sent;
    }

    public int getRemainingCredits() {
        return remaining_credits;
    }

    public String getInternalOrderId() {
        return internal_order_id;
    }

    public boolean isValid() {
        return "OK".equals(result);
    }

    private static Calendar conversion(String date) {
        try {
            String data = date.split(" ")[0];
            String anno = data.split("-")[0];
            String mese = data.split("-")[1];
            String giorno = data.split("-")[2];
            String ora = date.split(" ")[1];
            String ore = ora.split(":")[0];
            String min = ora.split(":")[1];
            Calendar d1 = new GregorianCalendar();
            d1.set(Calendar.MONTH, parseINT(mese) - 1);
            d1.set(Calendar.DAY_OF_MONTH, parseINT(giorno));
            d1.set(Calendar.YEAR, parseINT(anno));
            d1.set(Calendar.HOUR_OF_DAY, parseINT(ore));
            d1.set(Calendar.MINUTE, parseINT(min));
            d1.set(Calendar.SECOND, 0);
            return d1;
        } catch (Exception e) {
        }
        return null;
    }

    public static File createEVENT(String datainizioMYSQL, String datafineMYSQL, String eventName) {
        try {
            File ics = new File("Event_MC.ics");
            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            TimeZone timezone = registry.getTimeZone("Europe/Rome");
            VTimeZone tz = timezone.getVTimeZone();
            Calendar startDate = conversion(datainizioMYSQL);
            startDate.setTimeZone(timezone);
            java.util.Calendar endDate = conversion(datafineMYSQL);
            endDate.setTimeZone(timezone);
            net.fortuna.ical4j.model.DateTime start = new net.fortuna.ical4j.model.DateTime(startDate.getTime());
            net.fortuna.ical4j.model.DateTime end = new net.fortuna.ical4j.model.DateTime(endDate.getTime());
            VEvent meeting = new VEvent(start, end, eventName);
            meeting.getProperties().add(tz.getTimeZoneId());
            UidGenerator ug = new RandomUidGenerator();
            Uid uid = ug.generateUid();
            meeting.getProperties().add(uid);
            net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar();
            icsCalendar.getProperties().add(new ProdId("-//FL_EventsCalendar//iCal4j 2.0//IT"));
            icsCalendar.getProperties().add(Version.VERSION_2_0);
            icsCalendar.getProperties().add(CalScale.GREGORIAN);
            icsCalendar.getComponents().add(meeting);
            FileOutputStream fout = new FileOutputStream(ics);
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(icsCalendar, fout);
            fout.close();
            return ics;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
