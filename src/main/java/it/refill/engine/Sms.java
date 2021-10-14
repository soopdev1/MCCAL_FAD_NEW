/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author rcosco
 */
public class Sms {

    public static final String BASEURL = "https://api.skebby.it/API/v1.0/REST";
    
    public static final String MESSAGE_HIGH_QUALITY = "GP";
    public static final String MESSAGE_MEDIUM_QUALITY = "TI";
    public static final String MESSAGE_LOW_QUALITY = "SI";

    public static boolean sendSmsCAD(CAD cad) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdf_h = new SimpleDateFormat("HH:mm");
            Database d1 = new Database(Action.log);
            String[] authKeys = login(d1.get_Path("skebbyUser"), d1.get_Path("skebbyPwd"));
            SendSMSRequest sendSMS = new SendSMSRequest();
            sendSMS.setMessage(d1.get_Path("smsCAD")
                    .replace("@nome", cad.getNome())
                    .replace("@giorno", sdf.format(cad.getGiorno()))
                    .replace("@start", sdf_h.format(cad.getOrariostart()))
                    .replace("@end", sdf_h.format(cad.getOrarioend())));
            sendSMS.setMessageType(MESSAGE_LOW_QUALITY);
            sendSMS.addRecipient("+39" + cad.getNumero());
            sendSMS(authKeys, sendSMS);
            d1.closeDB();
            return true;
        } catch (Exception ex) {
            System.err.println(ExceptionUtils.getStackTrace(ex));
        }
        return false;
    }

    private static String getName(String nome, String cognome) {
        try {
            return String.valueOf(nome.charAt(0)).toUpperCase() + " " + cognome.trim().toUpperCase();
        } catch (Exception e) {
        }
        return nome + " " + cognome;
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
            sendSMS2021_R(StringUtils.deleteWhitespace(numerono39), msg);
//            
//            
//            Database d1 = new Database(Action.log);
//            String[] authKeys = login(d1.get_Path("skebbyUser"), d1.get_Path("skebbyPwd"));
//            SendSMSRequest sendSMS = new SendSMSRequest();
//            sendSMS.setMessage();
//            sendSMS.setMessageType(MESSAGE_LOW_QUALITY);
//            sendSMS.addRecipient("+39" + StringUtils.deleteWhitespace(numerono39));
//            sendSMS(authKeys, sendSMS);
//            d1.closeDB();
            return true;
        } catch (Exception ex) {
            System.err.println(ExceptionUtils.getStackTrace(ex));
        }
        return false;
    }

    /**
     * This object is used to create an SMS message sending request. The JSon
     * object is then automatically created starting from an instance of this
     * class, using GSon.
     */
    public static class SendSMSRequest {

        /**
         * The message body
         */
        private String message;

        /**
         * The message type
         */
        private String message_type = MESSAGE_HIGH_QUALITY;

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
     * This class represents the API Response. It is automatically created
     * starting from the JSON object returned by the server, using GSon
     */
    public static class SendSMSResponse {

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
    }

    /**
     * Authenticates the user given it's username and password. Returns the pair
     * user_key, Session_key
     *
     * @param username The user username
     * @param password The user password
     * @return A list with 2 strings. Index 0 is the user_key, index 1 is the
     * Session_key
     * @throws IOException If an error occurs
     */
    private static String[] login(String username, String password) throws IOException {
        URL url = new URL(BASEURL + "/login?username=" + username + "&password=" + password);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = "";
        String output;
        while ((output = br.readLine()) != null) {
            response += output;
        }
        conn.disconnect();

        String[] parts = response.split(";");
        return parts;
    }

    /**
     * Sends an SMS message
     *
     * @param authKeys The pair of user_key and Session_key
     * @param sendSMS The SendSMS object
     * @throws IOException If an error occurs
     */
    private static boolean sendSMS(String[] authKeys, SendSMSRequest sendSMS) throws IOException {
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
        
        System.out.println("it.refill.engine.Sms.sendSMS() "+payload);
        System.out.println("it.refill.engine.Sms.sendSMS() "+conn.getResponseCode());
        System.out.println("it.refill.engine.Sms.sendSMS() "+conn.getResponseMessage());
        
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


    
    
    public static boolean sendSMS2021_R(String cell, String msg) {
        try {
            
            
            
            
            
            Database d1 = new Database(Action.log);
            String skebbyuser = d1.get_Path("skebbyUser");
            String skebbyPwd = d1.get_Path("skebbyPwd");
            String skebbyURL = "https://api.skebby.it/API/v1.0/REST";
            d1.closeDB();
            
            System.out.println("it.refill.engine.Sms.sendSMS2021_R() "+skebbyuser);
            System.out.println("it.refill.engine.Sms.sendSMS2021_R() "+skebbyPwd);
            
            String[] authKeys = login_R(skebbyuser, skebbyPwd, skebbyURL);
            SendSMSRequest sendSMS = new SendSMSRequest();
            System.out.println(msg);
            sendSMS.setMessage(msg);
            sendSMS.setMessageType(MESSAGE_LOW_QUALITY);
            sendSMS.addRecipient("+39" + cell);
//            sendSMS.setSender(skebbySender);
            boolean es = sendSMS_R(authKeys, sendSMS, skebbyURL);
            return es;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String[] login_R(String username, String password, String BASEURL) throws IOException {
        URL url = new URL(BASEURL + "/login?username=" + username + "&password=" + password);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = "";
        String output;
        while ((output = br.readLine()) != null) {
            response += output;
        }
        conn.disconnect();
        String[] parts = response.split(";");
        return parts;
    }

    private static boolean sendSMS_R(String[] authKeys, SendSMSRequest sendSMS, String BASEURL) throws IOException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        URL url = new URL(BASEURL + "/sms");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
            System.out.println(BASEURL + "/sms" + " " + conn.getResponseMessage());
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = "";
        String output;
        while ((output = br.readLine()) != null) {
            response += output;
        }
        conn.disconnect();
        SendSMSResponse responseObj = gson.fromJson(response, SendSMSResponse.class);
        return responseObj.isValid();
    }
    
    


}
