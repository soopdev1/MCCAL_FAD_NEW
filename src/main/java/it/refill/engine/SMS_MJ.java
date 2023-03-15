package it.refill.engine;

import com.mailjet.client.ClientOptions;
import static com.mailjet.client.ClientOptions.builder;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.sms.SmsSend;
import static it.refill.engine.Action.conf;
import static it.refill.engine.Action.log;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author Administrator
 */
public class SMS_MJ {

    private static String getName(String nome, String cognome) {
        try {
            return String.valueOf(nome.charAt(0)).toUpperCase() + " " + cognome.trim().toUpperCase();
        } catch (Exception exception) {
            return nome + " " + cognome;
        }
    }

    public static boolean sendSmsFAD(String nome, String cognome, String numerono39) {
        try {
            if (numerono39 == null) {
                return false;
            }
            if (numerono39.trim().equals("")) {
                return false;
            }
            String nm = getName(nome, cognome);
            Database d1 = new Database(Action.log);
            String msg = d1.get_Path("smsFAD").replace("@nome", nm);
            d1.closeDB();
            sendSMS2022(StringUtils.deleteWhitespace(numerono39), msg);
            return true;
        } catch (Exception ex) {
            System.err.println(ExceptionUtils.getStackTrace(ex));
            return false;
        }
    }

    private static boolean sendSMS2022(String cell, String msg) {
        try {
            ClientOptions options = builder().bearerAccessToken(conf.getString("mj.sms.token")).build();
            MailjetClient client = new MailjetClient(options);
            MailjetRequest request = new MailjetRequest(SmsSend.resource)
                    .property(SmsSend.FROM, conf.getString("mj.sms.name"))
                    .property(SmsSend.TO, "+39" + cell)
                    .property(SmsSend.TEXT, msg);
            MailjetResponse response = client.post(request);
            if (response.getStatus() == 200) {
                return true;
            }
            log.log(Level.INFO, "sendSMS2022: {0}", response.getStatus());
            log.log(Level.INFO, "sendSMS2022: {0}", response.toString());
        } catch (Exception e) {
            Action.insertTR("E", "service", Action.estraiEccezione(e));
        }
        return false;
    }

//    public static void main(String[] args) {
//        sendSMS2022("3286137172", "testing message");
//    }
}
