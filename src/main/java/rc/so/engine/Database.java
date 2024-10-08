/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rc.so.engine;

import com.google.gson.Gson;
import static rc.so.engine.Action.conf;
import static rc.so.engine.Action.estraiEccezione;
import static rc.so.engine.Action.pat_5;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author rcosco
 */
public class Database {

    public Connection c;
    public Logger log;

    public Database(Logger l) {

        String user = conf.getString("db.user");
        String password = conf.getString("db.pass");
        String host = conf.getString("db.host") + ":3306/" + conf.getString("db.name");

        this.log = l;
        boolean mysql = true;
        if (mysql) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                Properties p = new Properties();
                p.put("user", user);
                p.put("password", password);
                p.put("characterEncoding", "UTF-8");
                p.put("passwordCharacterEncoding", "UTF-8");
                p.put("useSSL", "false");
                p.put("connectTimeout", "1000");
                p.put("useUnicode", "true");
                p.put("serverTimezone", "UTC");
                this.c = DriverManager.getConnection("jdbc:mysql://" + host, p);
            } catch (Exception ex) {
                this.log.severe(estraiEccezione(ex));
                if (this.c != null) {
                    try {
                        this.c.close();
                    } catch (SQLException ex1) {
                    }
                }
                this.c = null;
            }
        }
    }

    public Connection getC() {
        return c;
    }

    public void setC(Connection c) {
        this.c = c;
    }

    public void closeDB() {
        try {
            if (this.c != null) {
                this.c.close();
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
    }

    public void insertTR(String type, String user, String descr) {
        try {
            PreparedStatement ps = this.c.prepareStatement("INSERT INTO tracking (azione,iduser,timestamp) VALUES (?,?,?)");
            ps.setString(1, "FAD: " + descr);
            ps.setString(2, user);
            ps.setString(3, getNow());
            ps.execute();
        } catch (Exception ex) {
            this.log.severe(estraiEccezione(ex));
        }
    }

    public String getNow() {
        try {
            String sql = "SELECT now()";
            PreparedStatement ps = this.c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (Exception ex) {
        }
        return new DateTime().toString(pat_5);
    }

    public List<String> cf_list(String table) {
        List<String> out = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT(codicefiscale) FROM " + table;
            try ( PreparedStatement ps = this.c.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getString(1));
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public GenericUser getDocente(String cf) {
        GenericUser out = null;
        try {
            String sql = "SELECT iddocenti, nome, cognome, codicefiscale FROM docenti WHERE codicefiscale = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cf);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = new GenericUser(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), "NONE", null);
                    }
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public GenericUser getUserSA(String username) {
        GenericUser out = null;
        try {
            String sql = "SELECT username,idsoggetti_attuatori FROM user WHERE username = ? AND tipo = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, "1");
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int idsa = rs.getInt(2);
                        String sql1 = "SELECT ragionesociale FROM soggetti_attuatori WHERE idsoggetti_attuatori = ?";
                        PreparedStatement ps1 = this.c.prepareStatement(sql1);
                        ps1.setInt(1, idsa);
                        ResultSet rs1 = ps1.executeQuery();
                        if (rs1.next()) {
                            out = new GenericUser(rs.getString(1), rs1.getString(1), "", rs.getString(1), "", null);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public GenericUser getUserMC(String username) {
        GenericUser out = null;
        try {
            String sql = "SELECT username FROM user WHERE username = ? AND tipo = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, "2");
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = new GenericUser(rs.getString(1), "ADMIN", "MC", rs.getString(1), "", null);
                    }
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public GenericUser getUser(int id) {
        GenericUser out = null;
        try {
            String sql = "SELECT username FROM user WHERE iduser = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setInt(1, id);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = new GenericUser(rs.getString(1), "ADMIN", "US", rs.getString(1), "", null);
                    }
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public GenericUser getUser(String mail) {
        GenericUser out = null;
        try {
            String sql = "SELECT username FROM user WHERE email = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, mail);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = new GenericUser(rs.getString(1), "ADMIN", "US", rs.getString(1), "", null);
                    }
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public GenericUser getAllievo(String cf) {
        GenericUser out = null;
        try {
            String sql = "SELECT idallievi, nome, cognome, codicefiscale, email , telefono FROM allievi WHERE codicefiscale = ?";
            try ( PreparedStatement ps = this.c.prepareStatement(sql)) {
                ps.setString(1, cf);
                try ( ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        out = new GenericUser(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
                    }
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public String getNanoSecond() {
        try {
            String sql = "select current_timestamp(6)";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(new Date());
    }

    public List<GenericUser> get_UserProg(String idpr, boolean mail) {
        List<GenericUser> list = new ArrayList<>();
        try {

            String sql = "SELECT * FROM allievi WHERE idprogetti_formativi = '" + idpr + "' AND id_statopartecipazione='01' ";
            if (mail) {
                sql += " AND email REGEXP '^[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9._-]@[a-zA-Z0-9][a-zA-Z0-9._-]*[a-zA-Z0-9]\\\\.[a-zA-Z]{2,63}$'";
            }

            sql += " ORDER BY cognome,nome";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new GenericUser(rs.getString(1), rs.getString(4), rs.getString(5),
                            rs.getString(6), rs.getString(8), rs.getString("telefono")));
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return list;
    }

    public List<GenericUser> get_DocProg(String idpr, boolean mail) {
        List<GenericUser> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM docenti WHERE iddocenti in (SELECT iddocenti FROM progetti_docenti WHERE idprogetti_formativi='" + idpr + "')";
            if (mail) {
                sql += " AND email REGEXP '^[^@]+@[^@]+\\.[^@]{2,}$'";
            }
            sql += " ORDER BY cognome,nome";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new GenericUser(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString("email"), null));
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return list;
    }

    public String get_nomeProg(String idpr) {
        String out = "";
        try {
            String sql = "SELECT descrizione FROM progetti_formativi a WHERE idprogetti_formativi = '" + idpr + "'";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public String get_Stanza_conferenza_login(String id, String psw, String email) {
        String out = null;
        try {
            String sql = "SELECT nomestanza FROM fad_micro WHERE stato='0' AND idfad = '" + id
                    + "' AND password = '" + psw + "' AND partecipanti LIKE '%" + email + "%'";

            if (email == null) {
                sql = "SELECT nomestanza FROM fad_micro WHERE stato='0' AND idfad = '" + id
                        + "' AND password = '" + psw + "'";
            }

            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public String get_Stanza_CAD_login(String id, String psw, String email) {
        String out = null;
        try {
            String sql = "SELECT idcad FROM cad WHERE stato='0' AND idcad = '" + id + "' "
                    + "AND password = '" + psw + "' AND email = '" + email + "'";

            if (email == null) {
                sql = "SELECT idcad FROM cad WHERE stato='0' AND idcad = '" + id
                        + "' AND password = '" + psw + "'";
            }

            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = "CAD_" + rs.getString(1);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public boolean verificaStanza(String pr, String nomestanza) {
        boolean ok = false;
        try {
            String sql = "SELECT nomestanza FROM fad_multi a WHERE stato='0' AND idprogetti_formativi = '" + pr + "' AND nomestanza = '" + nomestanza + "'";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                ok = rs.next();
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }

        return ok;
    }

    public String get_Stanza(String idpr) {
        String out = null;
        try {
            String sql = "SELECT nomestanza FROM fad a WHERE stato='0' AND idprogetti_formativi = '" + idpr + "'";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public void log_ajax(String type, String room, String action, String date) {
        try {
            String sql = "INSERT INTO fad_track (type,room,action,date) VALUES (?,?,?,?)";
            try ( PreparedStatement pst = this.c.prepareStatement(sql)) {
                pst.setString(1, type);
                pst.setString(2, room);
                pst.setString(3, action);
                pst.setString(4, date);
                pst.execute();
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
    }

    public String get_Path(String id) {
        String out = null;
        try {
            String sql = "SELECT url FROM path WHERE id='" + id + "'";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public String[] get_Mail(String id) {
        String[] out = {"", ""};
        try {
            String sql = "SELECT oggetto,testo FROM email WHERE chiave='" + id + "'";
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out[0] = rs.getString(1);
                    out[1] = rs.getString(2);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public List<String> getMailFromConference(String nomestanza) {
        List<String> out = new ArrayList<>();
        try {
            String sql = "SELECT partecipanti FROM fad_micro WHERE idfad = " + nomestanza;
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = Arrays.asList(new Gson().fromJson(rs.getString(1), String[].class));
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public String getPswFromConference(String nomestanza) {
        String out = null;
        try {
            String sql = "SELECT password FROM fad_micro WHERE idfad= " + nomestanza;
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = rs.getString(1);
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public CAD getroom(int id) {
        CAD out = null;
        try {
            String sql = "SELECT * FROM cad WHERE idcad=" + id;
            try ( Statement st = this.c.createStatement();  ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = new CAD(rs.getInt(1), rs.getString("nome"), rs.getString("cognome"), rs.getString("email"), rs.getString("numero"), rs.getString("giorno"),
                            rs.getString("orariostart"), rs.getString("orarioend"), rs.getString("password"),
                            rs.getString("stato"), rs.getInt("iduser"));
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

    public Fadroom getroom(String id) {
        Fadroom out = null;
        try {
            String sql = "SELECT * FROM fad_micro WHERE idfad=" + id;
            try (Statement st = this.c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    out = new Fadroom(
                            rs.getInt("idfad"),
                            rs.getString("datacreazione"),
                            rs.getString("nomestanza"),
                            rs.getString("stato"),
                            rs.getInt("iduser"),
                            rs.getString("partecipanti"),
                            rs.getString("password"),
                            rs.getString("fine"),
                            rs.getString("inizio"),
                            rs.getString("note")
                    );
                }
            }
        } catch (Exception ex) {
            insertTR("E", "System", estraiEccezione(ex));
        }
        return out;
    }

}
