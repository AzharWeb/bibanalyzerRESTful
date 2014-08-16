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
package de.acoli.informatik.uni.frankfurt.pdftotext;

/**
 *
 * @author Schenk2
 */
import java.io.File;
import java.io.FileNotFoundException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * 
 * @author niko
 */
public class XMLFormatter {

    public static final String INPUT_FILE = "pdf2text/xml/springer_openaccess_2.xml";

    /**
     * Inspiration taken from:
     * http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
     *
     * @param args
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static void main(String[] args) throws TransformerException, ParserConfigurationException, SAXException, IOException {

        String input = getUnformattedXMLContent(INPUT_FILE);
        String output = prettyFormatAlt(input);
        System.out.println(output);
        
    }
    
    public static String format(String inputFile) {
        String input = null;
        try {
            input = getUnformattedXMLContent(INPUT_FILE);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(XMLFormatter.class.getName()).log(Level.SEVERE, null, ex);
        }
        String output = null;
        try {
            output = prettyFormatAlt(input);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLFormatter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XMLFormatter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLFormatter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XMLFormatter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }

    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    public static String prettyFormat(String input) {
        return prettyFormat(input, 2);
    }
    
    
    public static String prettyFormatAlt(String input) throws ParserConfigurationException, SAXException, TransformerConfigurationException, IOException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(input));
        Document doc = db.parse(is);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
        return xmlString;
    }
    

    public static String getUnformattedXMLContent(String filename) throws FileNotFoundException {
        Scanner s = new Scanner(new File(filename));
        StringBuilder sb = new StringBuilder();
        while(s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            sb.append(aLine);
        }
        s.close();
        
        return sb.toString();
    }


}