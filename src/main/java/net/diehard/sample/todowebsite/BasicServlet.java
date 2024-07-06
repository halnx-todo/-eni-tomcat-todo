/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.diehard.sample.todowebsite;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;

/**
 * This Servlet inspired from the default tomcat examples display session parameters and hostname
 *
 * @author KanedaFromParis
 */
public class BasicServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 2452098580777027008L;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            response.setContentType("text/html");

            HttpSession session = request.getSession(true);

            Date created = new Date(session.getCreationTime());
            Date accessed = new Date(session.getLastAccessedTime());
            String hostName = InetAddress.getLocalHost().getHostName();

            StringBuilder sb = new StringBuilder();
            sb.append("""
                            <html><head>
                            <meta charset="UTF-8">
                            <title>Sessions Example</title>
                            </head><body bgcolor="white">
                            <h3>Sessions Example</h3>
                            Session ID:\s""")
                    .append(session.getId()).append("<br />\n")
                    .append("HostName: <b>").append(hostName).append("</b><br />\n")
                    .append("Created: ").append(created).append(" <br />\n")
                    .append("Last Accessed: ").append(accessed).append(" <br />\n")
                    .append("""
                                <p>The following data is in your session:<br /></p>
                                <p></p><form action="BasicServlet" method="POST">
                                Name of Session Attribute:<input type="text" size="20" name="dataname"><br />
                                Value of Session Attribute:<input type="text" size="20" name="datavalue">
                                <br /><input type="submit"></form><p>GET based form:<br /> 
                                </p><form action="BasicServlet" method="GET">Name of Session Attribute:<input type="text" size="20" name="dataname"><br />
                                Value of Session Attribute:<input type="text" size="20" name="datavalue"><br />
                                <input type="submit"></form>
                                <p><a href="BasicServlet?dataname=foo&amp;datavalue=bar">URL encoded </a></p></body>
                                """);


            // set session info if needed
            String dataName = request.getParameter("dataname");
            if (dataName != null && !dataName.isEmpty()) {
                String dataValue = request.getParameter("datavalue");
                session.setAttribute(dataName, dataValue);
            }
            sb.append("<hr />");
            sb.append("<p>");
            Enumeration<String> e = session.getAttributeNames();
            while (e.hasMoreElements()) {
                String name = (String) e.nextElement();
                String value = session.getAttribute(name).toString();

                sb.append(name).append(" = ").append(value).append("<br />");
            }
            sb.append("</p>");
            sb.append("<hr />");
            out.println(sb.toString());
            out.println("</body></html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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
