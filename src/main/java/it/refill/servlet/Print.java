/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.refill.servlet;

import static it.refill.engine.Action.getNanoSecond;
import static it.refill.engine.Action.getRequestValue;
import static it.refill.engine.Action.log_ajax;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author rcosco
 */
public class Print extends HttpServlet {
    
    //SCREEN IN PDF
    public static String screen_to_pdf(
            String requestbase64,
            String nomeutente,
            String nomeoperatore
    ) {
//        try {

//            Db_Bando dbb = new Db_Bando();
//            String pathtemp = dbb.getPath("pathtemp");
//            dbb.closeDB();
//
//            String fileimg = pathtemp + Utility.generaId() + ".jpeg";
//            String b64 = StringUtils.substringAfter(requestbase64, ",");
//            FileUtils.writeByteArrayToFile(new File(fileimg), Base64.decodeBase64(b64));
//            File pdfOut = new File(replace(fileimg, ".jpeg", "_SCR.pdf"));
//            PdfWriter wr = new PdfWriter(pdfOut);
//            PdfFont roman = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
//            PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLDITALIC);
//            PdfDocument pdfDocument = new PdfDocument(wr);
//            Document document = new Document(pdfDocument);
//            ImageData imageData = ImageDataFactory.create(fileimg);
//            Image image1 = new Image(imageData);
//            document.add(image1);
//            document.add(new Paragraph(""));
//            float[] columnWidths = {1};
//            Table table = new Table(columnWidths);
//            table.setWidth(UnitValue.createPercentValue(100));
//            Cell cell = new Cell()
//                    .add(new Paragraph("  "));
//            cell.setBorder(Border.NO_BORDER);
//            cell.setBorderTop(new SolidBorder(ColorConstants.BLACK, 1));
//            table.addCell(cell);
//            document.add(table);
//            document.add(new Paragraph(""));
//            Paragraph p1 = new Paragraph().add(new Text("SCREENSHOT EFFETTUATO IN DATA: ").setFont(bold))
//                    .add(new Text(new DateTime().toString("dd/MM/yyyy HH:mm:ss")).setFont(roman));
//            p1.setTextAlignment(TextAlignment.RIGHT);
//            document.add(p1);
//            if (nomeutente != null) {
//                Paragraph p2 = new Paragraph().add(new Text("UTENTE: ").setFont(bold)).add(new Text(nomeutente).setFont(roman));
//                p2.setTextAlignment(TextAlignment.RIGHT);
//                document.add(p2);
//            }
//            if (nomeoperatore != null) {
//                Paragraph p3 = new Paragraph().add(new Text("OPEDRATORE: ").setFont(bold)).add(new Text(nomeoperatore).setFont(roman));
//                p3.setTextAlignment(TextAlignment.RIGHT);
//                document.add(p3);
//            }
//            document.close();
//            wr.close();
//            if (checkPDF(pdfOut)) {
//                String out = Base64.encodeBase64String(FileUtils.readFileToByteArray(pdfOut));
//                if (out != null) {
//                    pdfOut.delete();
//                    return out;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return null;
    }
    
    protected void screenshot(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String out = screen_to_pdf(getRequestValue(request, "base64"), getRequestValue(request, "nomeuser"), getRequestValue(request, "nomeop"));
        PrintWriter pw = response.getWriter();
        pw.print(out);
        pw.close();

    }

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

        String date = getNanoSecond();
        String type = getRequestValue(request, "type");
        String room = getRequestValue(request, "room");
        String action = getRequestValue(request, "action");

        if (type.equals("screenshot")) {
            screenshot(request, response);
        }else if (!type.equals("")) {
            log_ajax(type, room, action, date);
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
