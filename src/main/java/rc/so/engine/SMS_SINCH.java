/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rc.so.engine;

import com.sinch.xms.ApiConnection;
import com.sinch.xms.SinchSMSApi;
import com.sinch.xms.api.MtBatchTextSmsResult;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import static rc.so.engine.Action.conf;
import static rc.so.engine.Action.estraiEccezione;
import static rc.so.engine.Action.log;

/**
 *
 * @author Administrator
 */
public class SMS_SINCH {

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
            sendSMS2024(StringUtils.deleteWhitespace(numerono39), msg);
            return true;
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
            return false;
        }
    }

    private static String getName(String nome, String cognome) {
        try {
            return String.valueOf(nome.charAt(0)).toUpperCase() + " " + cognome.trim().toUpperCase();
        } catch (Exception exception) {
            return nome + " " + cognome;
        }
    }

    private static boolean sendSMS2024(String cell, String msg) {
        try (ApiConnection conn = ApiConnection.builder()
                .servicePlanId(conf.getString("si.sms.spid"))
                .token(conf.getString("si.sms.token"))
                .start()) {
            String[] recipients = {"39" + StringUtils.replace(cell, "+39", "")};
            MtBatchTextSmsResult batch
                    = conn.createBatch(
                            SinchSMSApi.batchTextSms()
                                    .sender(conf.getString("si.sms.sender"))
                                    .addRecipient(recipients)
                                    .body(msg)
                                    .build());
            log.log(Level.INFO, "SMS OK -> {0} -- ID:{1}", new Object[]{cell, batch.id()});
            return true;
        } catch (Exception ex) {
            log.severe(estraiEccezione(ex));
        }
        return false;
    }
}
