/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.engine;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.resource.Emailv31;
import static com.mailjet.client.resource.Emailv31.MESSAGES;
import static com.mailjet.client.resource.Emailv31.Message.BCC;
import static com.mailjet.client.resource.Emailv31.Message.CC;
import static com.mailjet.client.resource.Emailv31.Message.FROM;
import static com.mailjet.client.resource.Emailv31.Message.HTMLPART;
import static com.mailjet.client.resource.Emailv31.Message.SUBJECT;
import static com.mailjet.client.resource.Emailv31.Message.TO;
import static com.mailjet.client.resource.Emailv31.resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.DateTime;
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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author agodino
 */
public class SendMailJet {

    public static String convertToUTF8(String s) {
        try {
            return new String(s.getBytes("UTF-8"), "ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    public static boolean sendMail(String name, String[] to, String txt, String subject) throws MailjetException {
        return sendMail(name, to, null, txt, subject, null);
    }

    

    public static boolean sendMail(String name, String[] to, String[] bcc, String txt, String subject) throws MailjetException {
        return sendMail(name, to, bcc, txt, subject, null);
    }
    
    public static boolean sendMailEvento(String name, String[] to, String txt, String subject, AttachMJ evento) throws MailjetException {
        return sendMail(name, to, null, txt, subject, evento);
    }

    public static boolean sendMail(String name, String[] to, String[] bcc, String txt, String subject, AttachMJ evento) throws MailjetException {

        MailjetClient client;
        MailjetRequest request;
        MailjetResponse response;

        String mailjet_api = Action.get_Path("mailjet_api");
        String mailjet_secret = Action.get_Path("mailjet_secret");
        String mailjet_name = "microcredito.noreply@faultless.it";

        ClientOptions options = ClientOptions.builder()
                .apiKey(mailjet_api)
                .apiSecretKey(mailjet_secret)
                .build();

        client = new MailjetClient(options);
        JSONArray dest = new JSONArray();
        JSONArray ccn = new JSONArray();
        JSONArray ccj = new JSONArray();

        if (to != null) {
            for (String s : to) {
                dest.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        }

        if (bcc != null) {
            for (String s : bcc) {
                ccn.put(new JSONObject().put("Email", s)
                        .put("Name", ""));
            }
        }

        JSONObject mail = new JSONObject().put(FROM, new JSONObject()
                .put("Email", mailjet_name)
                .put("Name", name))
                .put(TO, dest)
                .put(CC, ccj)
                .put(BCC, ccn)
                .put(SUBJECT, subject)
                .put(HTMLPART, txt);

        if (evento != null) {
            try {
                mail.put(Emailv31.Message.ATTACHMENTS, new JSONArray()
                        .put(new JSONObject()
                                .put("ContentType", evento.getContentType())
                                .put("Filename", evento.getFilename())
                                .put("Base64Content", evento.getBase64Content())));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

//        if (file != null) {
//            try {
//                filename = file.getName();
//                content_type = probeContentType(file.toPath());
//                try (InputStream i = new FileInputStream(file)) {
//                    b64 = new String(encodeBase64(toByteArray(i)));
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//            mail.put(ATTACHMENTS, new JSONArray()
//                    .put(new JSONObject()
//                            .put("ContentType", content_type)
//                            .put("Filename", filename)
//                            .put("Base64Content", b64)));
//        }
        request = new MailjetRequest(resource)
                .property(MESSAGES, new JSONArray()
                        .put(mail));

        response = client.post(request);

        boolean ok = response.getStatus() == 200;

        if (!ok) {
            System.err.println("ERRORE: sendMail - " + response.getStatus() + " -- " + response.getRawResponseContent() + " --- " + response.getData().toList());
        }

        return ok;

//        MailjetClient client = new MailjetClient();
//        client.setDebug(MailjetClient.VERBOSE_DEBUG);
//
//        JSONArray dest = new JSONArray();
//        JSONArray ccn = new JSONArray();
//
//        for (String s : to) {
//            dest.put(new JSONObject().put("Email", s)
//                    .put("Name", ""));
//        }
//
//        if (bcc != null) {
//            for (String s : bcc) {
//                ccn.put(new JSONObject().put("Email", s)
//                        .put("Name", ""));
//            }
//        }
//
//        JSONObject mail = new JSONObject().put(Emailv31.Message.FROM, new JSONObject()
//                .put("Email", "microcredito.noreply@faultless.it")
//                .put("Name", name))
//                .put(Emailv31.Message.TO, dest)
//                .put(Emailv31.Message.BCC, ccn)
//                .put(Emailv31.Message.SUBJECT, subject)
//                .put(Emailv31.Message.HTMLPART, txt);
//        if (evento != null) {
//            mail.put(Emailv31.Message.ATTACHMENTS, new JSONArray()
//                    .put(new JSONObject()
//                            .put("ContentType", evento.getContentType())
//                            .put("Filename", evento.getFilename())
//                            .put("Base64Content", evento.getBase64Content())));
//        }
//
////        MailjetRequest request = new MailjetRequest(Emailv31.resource)
////                .property(Emailv31.MESSAGES, new JSONArray()
////                        .put(mail));
////        
////        MailjetResponse response = client.post(request);
////        boolean ok = response.getStatus() == 200;
////        if (!ok) {
////            System.err.println("ERRORE: sendMail - " + response.getStatus() + " -- " + response.getRawResponseContent() + " --- " + response.getData().toList());
////        }
//
//        System.out.println("it.refill.engine.SendMailJet.sendMail() " + response.getData());
    }

//    private static Calendar conversion(String date) {
//        try {
//            String data = date.split(" ")[0];
//            String anno = data.split("-")[0];
//            String mese = data.split("-")[1];
//            String giorno = data.split("-")[2];
//            String ora = date.split(" ")[1];
//            String ore = ora.split(":")[0];
//            String min = ora.split(":")[1];
//            String sec = ora.split(":")[2];
//            Calendar d1 = new GregorianCalendar();
//            d1.set(Calendar.MONTH, parseIntR(mese) - 1);
//            d1.set(Calendar.DAY_OF_MONTH, parseIntR(giorno));
//            d1.set(Calendar.YEAR, parseIntR(anno));
//            d1.set(Calendar.HOUR_OF_DAY, parseIntR(ore));
//            d1.set(Calendar.MINUTE, parseIntR(min));
//            d1.set(Calendar.SECOND, parseIntR(sec));
//            return d1;
//        } catch (Exception e) {
//        }
//        return null;
//    }
    private static String conversion2(String date) {
        try {
            String data = date.split(" ")[0];
            String anno = data.split("-")[0];
            String mese = data.split("-")[1];
            String giorno = data.split("-")[2];
            String ora = date.split(" ")[1];
            String ore = ora.split(":")[0];
            String min = ora.split(":")[1];
            String sec = ora.split(":")[2];
            return anno + mese + giorno + "T" + ore + min + sec;
        } catch (Exception e) {
        }
        return date;
    }
//    private static int parseIntR(String value) {
//        try {
//            if (value != null) {
//                return Integer.parseInt(value);
//            }
//        } catch (Exception e) {
//        }
//        return 0;
//    }

//    public static AttachMJ createEVENT(String datainizioMYSQL, String datafineMYSQL, String eventName) {
//        try {
//            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
//            TimeZone timezone = registry.getTimeZone("Europe/Rome");
//            VTimeZone tz = timezone.getVTimeZone();
//            Calendar startDate = conversion(datainizioMYSQL);
//            startDate.setTimeZone(timezone);
//            Calendar endDate = conversion(datafineMYSQL);
//            endDate.setTimeZone(timezone);
//            DateTime start = new DateTime(startDate.getTime());
//            DateTime end = new DateTime(endDate.getTime());
//            VEvent meeting = new VEvent(start, end, eventName);
//            meeting.getProperties().add(tz.getTimeZoneId());
//            UidGenerator ug = new RandomUidGenerator();
//            Uid uid = ug.generateUid();
//            meeting.getProperties().add(uid);
//            net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar();
//            icsCalendar.getProperties().add(new ProdId("-//FL_EventsCalendar//iCal4j 2.0//IT"));
//            icsCalendar.getProperties().add(Version.VERSION_2_0);
//            icsCalendar.getProperties().add(CalScale.GREGORIAN);
//            icsCalendar.getComponents().add(meeting);
//            String pathtemp = Action.get_Path("pathTemp");
//            new File(pathtemp).mkdirs();
//            File ics = new File(pathtemp + uid.getValue() + ".ics");
//            try (FileOutputStream fout = new FileOutputStream(ics)) {
//                CalendarOutputter outputter = new CalendarOutputter();
//                outputter.output(icsCalendar, fout);
//            }
//            String Filename = "Event_MC.ics";
//            String ContentType = "text/calendar";
//            String Base64Content;
//            Base64Content = new String(Base64.encodeBase64(FileUtils.readFileToByteArray(ics)));
////            ics.delete();
//            return new AttachMJ(ContentType, Filename, Base64Content);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
    
    public static AttachMJ createEVENT(String datainizioMYSQL, String datafineMYSQL, String eventName) {
        try {
            org.joda.time.DateTime now = new org.joda.time.DateTime();
            String r1 = "BEGIN:VCALENDAR";
            String r2 = "PRODID:-//FL_EventsCalendar//iCal4j 2.0//IT";
            String r3 = "VERSION:2.0";
            String r4 = "CALSCALE:GREGORIAN";
            String r5 = "BEGIN:VEVENT";
            String r6 = "DTSTAMP:" + now.toString("yyyyMMdd") + "T" + now.toString("HHmmss") + "Z";
            String r7 = "DTSTART:" + conversion2(datainizioMYSQL);
            String r8 = "DTEND:" + conversion2(datafineMYSQL);
            String uuid = UUID.randomUUID().toString();
            String r9 = "SUMMARY:" + eventName;
            String r10 = "TZID:Europe/Rome";
            String r11 = "UID:" + uuid;
            String r12 = "END:VEVENT";
            String r13 = "END:VCALENDAR";
            String pathtemp = Action.get_Path("pathTemp");
            new File(pathtemp).mkdirs();
            File ics = new File(pathtemp + uuid + ".ics");

            try (FileWriter fw = new FileWriter(ics);
                    BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(r1);
                bw.newLine();
                bw.write(r2);
                bw.newLine();
                bw.write(r3);
                bw.newLine();
                bw.write(r4);
                bw.newLine();
                bw.write(r5);
                bw.newLine();
                bw.write(r6);
                bw.newLine();
                bw.write(r7);
                bw.newLine();
                bw.write(r8);
                bw.newLine();
                bw.write(r9);
                bw.newLine();
                bw.write(r10);
                bw.newLine();
                bw.write(r11);
                bw.newLine();
                bw.write(r12);
                bw.newLine();
                bw.write(r13);
                bw.newLine();
            }

            String Filename = "Event_MC.ics";
            String ContentType = "text/calendar";
            String Base64Content;
            Base64Content = new String(Base64.encodeBase64(FileUtils.readFileToByteArray(ics)));
            ics.delete();
            return new AttachMJ(ContentType, Filename, Base64Content);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
