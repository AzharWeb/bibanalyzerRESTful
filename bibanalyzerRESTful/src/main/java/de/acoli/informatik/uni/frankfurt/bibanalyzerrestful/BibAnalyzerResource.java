/**
 * *****************************************************************************
 * Copyright (c) 2014 
 * Christian Chiarcos, Niko Schenk 
 * Applied Computational Linguistics Lab (ACoLi)
 * Goethe-Universität Frankfurt am Main 
 * http://acoli.cs.uni-frankfurt.de/en.html
 * Robert-Mayer-Straße 10
 * 60325 Frankfurt am Main
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Niko Schenk - initial API and
 * implementation.
 * *****************************************************************************
 */

package de.acoli.informatik.uni.frankfurt.bibanalyzerrestful;

//import com.sun.jersey.core.header.FormDataContentDisposition;
//import com.sun.jersey.multipart.FormDataParam;

//import com.sun.jersey.multipart.FormDataParam;

//import de.acoli.informatik.uni.frankfurt.classifier.BibAnalyzer;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import de.acoli.informatik.uni.frankfurt.classifier.BibAnalyzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 *
 * @author niko
 */
@Path("/analyze/")
public class BibAnalyzerResource {

    @javax.ws.rs.core.Context
    ServletContext context;

    /**
     * TEXT_PLAIN is the request to this method. 
     * Test with: 
     * curl -i -H "Accept: text/plain" localhost:8080/bibanalyzerRESTful/analyze
     *
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayPlainTextHello() {
        return "A plain hello.";
    }

    /**
     * XML is the request to this method. 
     * Test with: 
     * curl -i -H "Accept: text/xml" localhost:8080/bibanalyzerRESTful/analyze
     * 
     * @return 
     */
    @GET
    @Produces(MediaType.TEXT_XML)
    public String sayXMLHello() {
        return "<?xml version=\"1.0\"?>" + "<hello> An XML hello" + "</hello>";
    }

    /**
     * HTML is the request to this method. 
     * Test with:
     * curl -i -H "Accept: text/html" localhost:8080/bibanalyzerRESTful/analyze
     * 
     * or:
     * Navigate your browser to: localhost:8080/bibanalyzerRESTful/analyze
     *
     * 
     * This is how you can control the input type.
     * @Consumes(MediaType.TEXT_HTML) 
     * 
     * @return 
     * 
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "HTML hello!" + "</title>"
                + "<body><h1>" + "I'm sending you HTML" + "</body></h1>" + "</html> ";
    }

    /**
     * Test with.
     * curl -i -X POST -d "input=thisIsATest" localhost:8080/bibanalyzerRESTful/analyze
     *
     * @param input
     * @return 
     * @throws java.lang.InterruptedException
     * @throws java.io.IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("application/x-www-form-urlencoded")
    public String analyze(@FormParam("input") String input) throws InterruptedException, IOException {

        System.out.println("Input: " + input);
        if (input == null) {
            System.out.println("Input is null!");
        }

        String realPath = context.getRealPath("/");
        return "<html><body><h1>Analyzing input: " + input + " <br></h1></body>" + realPath + " <- real path " + input + "</html>";
    }

    
    
    /**
     * Upload a file containing plaintext references specified by 
     * the "name" parameter (the filename)
     * and analyze it in terms of the bibliography analysis.
     *
     * Sample usage on the local machine:
     * 
     * > curl -i -F "name=@/path/to/your/local/input.txt"
     * localhost:8080/bibanalyzerRESTful/analyze/plaintext
     *
     * 
     * Sample usage on the server side:
     * > curl -i -F "name=@/path/to/your/local/input.txt"
     * corpora.acoli.informatik.uni-frankfurt.de/bibanalyzerRESTful/analyze/plaintext
     *
     *
     * where input.txt contains the plaintext references.
     *
     *
     * // Later options including parameters, e.g.: 
     * > curl -i -F "type=SPRINGER" -F "name=@/home/niko/Desktop/input.txt"
     * localhost:8080/bibanalyzerRESTful/analyze/plaintext
     *
     *
     * @param inputStream
     * @return
     * @throws java.io.IOException
     */
    @POST
    @Path("plaintext")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public synchronized Response uploadFile(@FormDataParam("file") InputStream inputStream
    //, @FormDataParam("file") FormDataContentDisposition file
    ) throws IOException {

        String realPath = context.getRealPath("/") + "modules/";
        System.out.println("real path: " + realPath);
        String generatedFileName = getGeneratedFileName();
        String fileDestination = "no-file-destination";
        try {
            fileDestination = uploadFileToServerDirectory(inputStream, realPath, generatedFileName);
        } catch (IOException ex) {
            Logger.getLogger(BibAnalyzerResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Call bibanalyzer on this filename.
        String[] inputFile = new String[1];
        inputFile[0] = fileDestination;
        BibAnalyzer.analyzeBibliography(inputFile, realPath);
        String aPlusPlusResponse = BibAnalyzer.getFinalAPlusPlus(realPath, "SPRINGER");

        System.out.println("Analysis successful.");

        return Response.status(200).entity(aPlusPlusResponse).build();
    }

    private synchronized String getGeneratedFileName() {
        Random randomGenerator = new Random();
        String fileIdx = String.valueOf(randomGenerator.nextInt(1000));
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        //System.out.println(dateFormat.format(cal.getTime()));
        String date = dateFormat.format(cal.getTime()).replace("/", "-").replace(":", "-").replace(" ", "-");
        return "upload_" + date + "_" + fileIdx + ".txt";
    }

    private synchronized String uploadFileToServerDirectory(InputStream inputStream, String realPath, String generatedFileName) throws FileNotFoundException, IOException {
        // Make sure directory is there, otherwise create it.
        File directoryToUploadTo = new File(realPath + "uploads/");
        //System.out.println(directoryToUploadTo.getName());
        if (directoryToUploadTo.exists()) {
            System.out.println("Directory \"uploads\" exits.");
        } else {
            // Create it.
            System.out.println("Directory for upload does NOT exist.");
            System.out.println("Creating one for you...");
            directoryToUploadTo.mkdir();
        }

        final String fileDestination = realPath + "uploads/" + generatedFileName;
        System.out.println(fileDestination);

        System.out.println("FILE_DESTINATION: " + fileDestination);
        File f = new File(fileDestination);
        OutputStream outputStream = new FileOutputStream(f);
        int size = 0;
        byte[] bytes = new byte[1024];
        while ((size = inputStream.read(bytes)) != -1) {
            System.out.println(size);
            outputStream.write(bytes, 0, size);
        }
        outputStream.flush();
        outputStream.close();

            // TODO:
        // Security check.
        // Plus: Check max size of file upload!
        ArrayList<String> fileLines = new ArrayList<String>();
        Scanner s = new Scanner(new File(fileDestination));
        while (s.hasNextLine()) {
            String aLine = s.nextLine();
            System.out.println(aLine);
            fileLines.add(aLine);
        }
        s.close();

        ArrayList<String> referenceLines = new ArrayList<>();
        for (int i = 4; i < fileLines.size() - 2; i++) {
            referenceLines.add(fileLines.get(i));
        }

        // Overwrite !!
        PrintWriter w = new PrintWriter(new File(fileDestination));
        for (String refLine : referenceLines) {
            w.write(refLine + "\n");
        }
        w.flush();
        w.close();

        return fileDestination;
    }
}
