/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 *
 * @author shannah
 */
public class NovaserverHTTPProxy extends HttpServlet {

    private String novaserverHost = "10.0.1.117";
    private int novaserverPort = 80;

    private static Map<String, String> parseHTTPHeaders(InputStream inputStream)
            throws IOException {
        int charRead;
        StringBuffer sb = new StringBuffer();
        while (true) {
            sb.append((char) (charRead = inputStream.read()));
            if ((char) charRead == '\r') {            // if we've got a '\r'
                sb.append((char) inputStream.read()); // then write '\n'
                charRead = inputStream.read();        // read the next char;
                if (charRead == '\r') {                  // if it's another '\r'
                    sb.append((char) inputStream.read());// write the '\n'
                    break;
                } else {
                    sb.append((char) charRead);
                }
            }
        }

        String[] headersArray = sb.toString().split("\r\n");
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 1; i < headersArray.length - 1; i++) {
            headers.put(headersArray[i].split(": ")[0],
                    headersArray[i].split(": ")[1]);
        }

        return headers;
    }
    
    private static int find(byte[] needle, byte[] haystack, int start, int len) {
        int needleLen = needle.length;
        if (needleLen > len) {
            return -1;
        }
        outer: for (int i=start; i<len-needleLen; i++) {
            inner: for (int j=0; j<needleLen; j++) {
                if (haystack[i+j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
    
    private static String ucFirst(String str) {
        if (str == null) {
            return null;
        }
        if (str.length() == 0) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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
        String requestContentType = request.getParameter("Content-Type");
        if (requestContentType != null && requestContentType.isEmpty()) {
            requestContentType = null;
        }
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isEmpty()) {
            authHeader = request.getParameter("Authorization");
        }
        if (authHeader == null || authHeader.isEmpty()) {
            HttpSession session = request.getSession();
            if (session != null) {
                authHeader = (String)session.getAttribute("Authorization");
            }
        }
        if (authHeader != null && !authHeader.isEmpty()) {
            HttpSession session = request.getSession(true);
            String currAuth = (String)session.getAttribute("Authorization");
            if (!Objects.equals(currAuth, authHeader)) {
                session.setAttribute("Authorization", authHeader);
            }
        }
        String uri = request.getParameter("uri");
        String escapedUri = uri.replace(" ", "%20");
        System.out.println("Received HTTP request for "+uri);
        System.out.println("AuthHeader is "+authHeader);
        Socket sock = null;
        try {
            System.out.println("Opening socket");
            sock = new Socket(novaserverHost, novaserverPort);
            sock.setKeepAlive(false);
            sock.setTcpNoDelay(true);
            
            /*
            fputs($fp, "GET $url HTTP/V1.0\r\n");
		  fputs($fp, $this->createDigestAuthHeader($url)."\r\n");
		  fputs($fp, "Connection maintain\r\n");
		  fputs($fp, "User-Agent: ResNova_NovaTerm_Mac/4.0\r\n");

		  fputs($fp, "\r\n");
            */
            boolean passthru = true;
            if (uri.startsWith("/file/") || uri.startsWith("http://*/file/")) {
                passthru = true;
            }
            OutputStream os = sock.getOutputStream();
            
            ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
            Writer output;
            
            if (request.getMethod().equalsIgnoreCase("post")) {
                output = new OutputStreamWriter(bufStream, "x-MacRoman");
                // this is a post request
                System.out.print("POST "+uri+" HTTP/V1.0\r\n");
                System.out.print("Authorization: "+(authHeader+"\r\n"));
                output.write("POST "+uri+" HTTP/V1.0\r\n");
                output.write("Authorization: "+authHeader+"\r\n");
                System.out.print("Connection: maintain\r\n");
                output.write("Connection: maintain\r\n");
                System.out.print("User-Agent: ResNova_NovaTerm_Mac/4.0\r\n");
                output.write("User-Agent: ResNova_NovaTerm_Mac/4.0\r\n");
                System.out.print("MIME-Version: 1.0\r\n");
                output.write("MIME-Version: 1.0\r\n");
                //multipart/mixed; boundary="qphzlgnmbemlrcbkvdgqnkauervssvkg"
                String requestBoundary = "qphzlgnmbemlrcbkvdgqnkauervssvkg";
                System.out.print("Content-Type: multipart/mixed; boundary=\""+requestBoundary+"\"\r\n");
                output.write("Content-Type: multipart/mixed; boundary=\""+requestBoundary+"\"\r\n");
                Enumeration<String> headerNames = request.getHeaderNames();
                long fileSize=-1;
                long lastModified=-1;
                String mimetype = null;
                while (headerNames.hasMoreElements()) {
                    
                    String headerName = headerNames.nextElement();
                    System.out.println("Found header: "+headerName);
                    if (headerName.startsWith("x-nt-")) {
                        System.out.print(ucFirst(headerName.substring(5))+": "+request.getHeader(headerName)+"\r\n");
                        output.write(ucFirst(headerName.substring(5))+": "+request.getHeader(headerName)+"\r\n");
                    }
                    if (fileSize == -1 && "x-ns-filesize".equalsIgnoreCase(headerName)) {
                        fileSize = Long.parseLong(request.getHeader(headerName));
                    }
                    if (lastModified == -1 && "x-ns-lastmodified".equalsIgnoreCase(headerName)) {
                        lastModified = Long.parseLong(request.getHeader(headerName));
                    }
                    if (mimetype == null && "x-ns-mimetype".equalsIgnoreCase(headerName)) {
                        mimetype = request.getHeader(headerName);
                    }
                    
                }
                System.out.print("\r\n");
                output.write("\r\n");
                System.out.println("Parts: "+request.getParts());
                for (Part part : request.getParts()) {
                    
                     if ("File".equalsIgnoreCase(part.getName())) {
                        
                        output.write("\r\n--"+requestBoundary+"\r\n");
                        output.write("Content-Type: application/macbinary; name=\""+part.getSubmittedFileName()+"\"\r\n");
                        //output.write("Content-Type: "+part.getContentType()+"; name=\""+part.getSubmittedFileName()+"\"\r\n");
                        output.write("Content-Transfer-Encoding: binary\r\n");
                        output.write("\r\n");
                        output.flush();
                        os.write(bufStream.toByteArray());
                        bufStream.reset();
                        MacbinaryEncoder enc = new MacbinaryEncoder(os, part.getSubmittedFileName(), mimetype, null, new Date(lastModified), new Date(lastModified), fileSize);
                        
                        
                        try (InputStream partInput = part.getInputStream()) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = partInput.read(buffer)) >= 0) {
                                if (len > 0) {
                                    enc.write(buffer, 0, len);
                                }
                            }
                        }
                        enc.closeWithoutClosingUnderlyingStream();
                        System.out.println("Written "+enc.getWritten()+" bytes in data fork");
                        System.out.println("File size reported: "+fileSize+" bytes.");
                        
                        if (fileSize > enc.getWritten() || fileSize < enc.getWritten() - 128) {
                            throw new RuntimeException("Written should be the file size padded to 128 byte segments.");
                        }
                        if (enc.getWritten() % 128 != 0) {
                            throw new RuntimeException("Written should be multiple of 128.  Off by "+(enc.getWritten() % 128));
                        }
                        
                        /*
                        if (enc.getWritten() != fileSize) {
                            throw new RuntimeException("File size vs bytes written is incorrect.");
                        }*/
                        
                        //output.write("\r\n");
                        //output.flush();
                        //os.write(bufStream.toByteArray());
                        //bufStream.reset();
                    } else if ("Description".equalsIgnoreCase(part.getName())) {
                        output.write("\r\n--"+requestBoundary+"\r\n");
                        output.write("Content-Type: text/html\r\n");
                        output.write("Content-Transfer-Encoding: binary\r\n");
                        output.write("\r\n");
                        output.flush();
                        os.write(bufStream.toByteArray());
                        bufStream.reset();
                        try (InputStream partInput = part.getInputStream()) {
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = partInput.read(buffer)) >= 0) {
                                if (len > 0) {
                                    os.write(buffer, 0, len);
                                }
                            }
                        }
                        //output.write("\r\n");
                        //output.flush();
                        //os.write(bufStream.toByteArray());
                        //bufStream.reset();
                        
                    }
                     
                     
                }
               
                output.write("\r\n--"+requestBoundary+"--\r\n");
                output.flush();
                os.write(bufStream.toByteArray());
                bufStream.reset();
                os.flush();
                
            } else {
                output = new OutputStreamWriter(os);
                System.out.print("GET "+uri+" HTTP/V1.0\r\n");
                System.out.print("Authorization: "+(authHeader+"\r\n"));
                output.write("GET "+uri+" HTTP/V1.0\r\n");
                output.write("Authorization: "+authHeader+"\r\n");
                output.write("Connection: maintain\r\n");
                output.write("User-Agent: ResNova_NovaTerm_Mac/4.0\r\n");
                output.write("\r\n");
                output.flush();
            }
            
           
            
            System.out.println("Getting inputstream");
            InputStream input = sock.getInputStream();
            
            
            String contentType = null;
            //Map<String,String> headers = parseHTTPHeaders(input);
            
            byte[] buffer = new byte[4096];
            int len = 0;
            
            byte[] newline = new byte[]{'\r', '\n'};
            long sent = 0;
            long contentLength = -1;
            String boundary = null;
            byte[] endBoundaryBytes = null;
            byte[] startBoundaryBytes = null;
            byte[] lastBuf = new byte[buffer.length];
            byte[] combinedBuf = new byte[buffer.length*2];
            int lastLen = -1;
            try (ServletOutputStream servletOut = response.getOutputStream()) {
                System.out.println("Opened output stream.");
                boolean inHeaders = true;
                ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
                ByteArrayOutputStream contentBuffer = null;
                byte[] endPattern = new byte[]{'\r', '\n', '\r', '\n'};
                System.out.println("About to write to client");
                boolean sendingOnlyPart = false;
                while ((contentLength == -1 || contentLength > sent) && (len = input.read(buffer)) >= 0) {
                    System.out.println("Writing...");
                   // System.out.println("Content: "+new String(buffer, 0, len));
                    if (inHeaders) {
                        int endPos = find(endPattern, buffer, 0, len);
                        if (endPos == -1) {
                            //System.out.println("Test 1");
                            headerBuffer.write(buffer, 0, len);
                            String bfStr = new String(buffer, 0, len);
                            System.out.println("HeaderBuffer: "+new String(buffer, 0, len));
                            if (bfStr.toLowerCase().contains("content-length: 0\r\n")) {
                                inHeaders = false;
                                if (contentBuffer == null) {
                                    contentBuffer = new ByteArrayOutputStream();
                                }
                            }
                        } else {
                            //System.out.println("Test 2");
                            System.out.println("HeaderBuffer+="+new String(buffer, 0, endPos+2));
                            headerBuffer.write(buffer, 0, endPos+2);
                            if (contentBuffer == null) {
                                contentBuffer = new ByteArrayOutputStream();
                            }
                            contentBuffer.write(buffer, endPos+4, len-endPos-4);
                            inHeaders = false;
                        }
                        
                        
                    }
                    //System.out.println("Text 3");
                    if (!inHeaders) {
                        if (headerBuffer != null) {
                            // Let's process the headers now
                            byte[] headerBytes = headerBuffer.toByteArray();
                            String headerStr = new String(headerBytes, "UTF-8");
                            headerStr = headerStr.replace("maintain", "close");
                            ;
                            System.out.println("Writing header: "+headerStr);
                            //servletOut.write(headerStr.getBytes("UTF-8"));
                            //servletOut.write(newline);
                            Scanner scn = new Scanner(headerStr);
                            while (scn.hasNextLine()) {
                                String ln = scn.nextLine();
                                if (ln.contains(":")) {
                                    String key = ln.substring(0, ln.indexOf(":")).trim();
                                    
                                    String val = ln.substring(ln.indexOf(":")+1).trim();
                                    if (key.equalsIgnoreCase("content-length")) {
                                        System.out.println("Content length is "+val);
                                        contentLength = Long.parseLong(val);
                                    } else if (key.equalsIgnoreCase("content-type") && val.contains("multipart/mixed") && val.contains("boundary=\"")) {
                                        boundary = val.substring(val.indexOf("boundary="));
                                        boundary = boundary.substring(boundary.indexOf("\"")+1);
                                        boundary = boundary.substring(0, boundary.indexOf("\""));
                                        endBoundaryBytes = ("--"+boundary+"--").getBytes("UTF-8");
                                        startBoundaryBytes = ("--"+boundary).getBytes("UTF-8");
                                    }
                                    boolean skipHeader = false;
                                    if (requestContentType != null && key.equalsIgnoreCase("MIME-Version")) {
                                        skipHeader = true;
                                    }
                                    response.setHeader(key, val);
                                }
                            }
                            headerBuffer = null;
                            if (contentLength == 0) {
                                System.out.println("Breaking on content length = 0");
                                break;
                            }
                            
                        }
                        if (contentBuffer != null) {

                            byte[] contentBytes = contentBuffer.toByteArray();
                            if (contentBytes.length > 0) {
                                //System.out.println("Writing content: "+new String(contentBytes, "UTF-8"));
                                boolean foundBoundary = false;
                                
                                if (requestContentType != null && endBoundaryBytes != null && contentLength > 0) {
                                    // This is a binary download so we need to strip the multipart stuff.
                                    int startBoundaryPos = find(startBoundaryBytes, contentBytes, 0, contentBytes.length);
                                    if (startBoundaryPos >= 0) {
                                        System.out.println("StartBoundaryHeaders at: "+startBoundaryPos);
                                        int startOfBoundary = startBoundaryPos;
                                        startBoundaryPos = find(endPattern, contentBytes, startBoundaryPos, contentBytes.length - startBoundaryPos);
                                        if (startBoundaryPos >= 0) {
                                            System.out.println("StartBoundaryContent At:"+startBoundaryPos);
                                            startBoundaryPos += endPattern.length;
                                            foundBoundary = true;
                                            sendingOnlyPart = true;
                                            if (contentLength > -1) {
                                                contentLength = contentLength - startBoundaryPos - endBoundaryBytes.length;
                                                response.setIntHeader("Content-Length", (int)contentLength);
                                                System.out.println("Chacking ContentLength to "+contentLength);
                                            }
                                            
                                            Scanner partHeadersScanner = new Scanner(new ByteArrayInputStream(contentBytes, startOfBoundary, startBoundaryPos - startOfBoundary), "UTF-8");
                                            while (partHeadersScanner.hasNextLine()) {
                                                String line = partHeadersScanner.nextLine();
                                                if (line.trim().isEmpty()) {
                                                    break;
                                                }
                                                if (line.indexOf(":") != -1) {
                                                    String key = line.substring(0, line.indexOf(":")).trim();
                                                    String val = line.substring(line.indexOf(":")+1).trim();
                                                    response.setHeader(key, val);
                                                    if (key.equalsIgnoreCase("content-type") && val.contains("name=\"")) {
                                                        String fname = val.substring(val.toLowerCase().indexOf("name=\""));
                                                        fname = fname.substring(fname.indexOf("\"")+1);
                                                        fname = fname.substring(0, fname.lastIndexOf("\""));
                                                        response.setHeader("Content-Disposition", "attachment; filename=\""+fname+"\"");
                                                    }
                                                }
                                            }
                                            
                                            servletOut.write(contentBytes, startBoundaryPos, contentBytes.length - startBoundaryPos);
                                            sent += contentBytes.length - startBoundaryPos;
                                            System.out.println("Sent "+sent);
                                        }
                                        
                                        
                                    }
                                }
                                if (!foundBoundary) {
                                    
                                    servletOut.write(contentBytes, 0, contentBytes.length);
                                    sent += contentBytes.length;
                                    System.out.println("Sent "+sent);
                                }
                            }
                            
                            contentBuffer = null;
                        } else {
                            if (sendingOnlyPart) {
                                long remaining = contentLength - sent;
                                if (remaining > 0) {
                                    int toSend = (int)Math.min(remaining, len);
                                    servletOut.write(buffer, 0, toSend);
                                    sent += toSend;
                                    System.out.println("Sent "+sent);
                                }
                            } else {
                                //System.out.println("Writing content: "+new String(buffer, 0, len, "UTF-8"));
                                servletOut.write(buffer, 0, len);

                                sent += len;
                                System.out.println("Sent "+sent);
                            }
                            
                        }
                    }
                    if (endBoundaryBytes != null && !sendingOnlyPart) {
                        
                        int combinedPos = 0;
                        if (lastLen > 0) {
                            System.arraycopy(lastBuf, 0, combinedBuf, 0, lastLen);
                            combinedPos = lastLen;
                        }
                        if (len > 0) {
                            System.arraycopy(buffer, 0, combinedBuf, combinedPos, len);
                            combinedPos += len;
                        }
                        if (find(endBoundaryBytes, combinedBuf, 0, combinedPos) > -1) {
                            // We've reached the end of output
                            break;
                        }

                        byte[] tmp = buffer;
                        buffer = lastBuf;
                        lastBuf = tmp;
                        lastLen = len;
                    }
                    
                }
                System.out.println("Flushing");
                servletOut.flush();
                
            }
           
            
        } finally {
            if (sock != null) {
                try {
                    sock.shutdownInput();
                } catch (Throwable t){}
                
                try {
                    sock.shutdownOutput();
                } catch (Throwable t){}
                try {
                    sock.close();
                } catch (Throwable t){}
            }
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
