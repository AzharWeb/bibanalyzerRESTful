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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * Description:
 * 
 * Demo program which analyzes PDF files and exports
 * the plain text bibliographic references (usually at
 * the end of the file).
 * 
 * Note, that you need to have the following tools installed.
 * - pdf2xml from http://sourceforge.net/projects/pdf2xml/
 * (Make it executable.)
 * 
 * Following the instructions, you also need:
 * http://sourceforge.net/p/pdf2xml/wiki/Home/
 * 
 * xpdf and libxml2, as well as:
 * 
 * - xmllint (Usually availabe under linux systems).
 * 
 * 
 * In detail, the program performs the following steps: 
 * 1. Convert PDFs to XML format using pdf2xml.
 * 2. Format xml files with xmllint 
 * 3. Export plaintext reference pieces and write them to output folder /txt
 *
 * @author niko
 */
public class PDFtoTextDemoLinux {

    // Time to wait (in milli seconds) between execution of commands.
    public static final int TIME_TO_WAIT = 3000;

    public static void main(String[] args) throws IOException, InterruptedException, SAXException, ParserConfigurationException, XPathExpressionException, FileNotFoundException {

        deleteFiles();
        Thread.sleep(TIME_TO_WAIT);

        System.out.println("Converting PDFs to plain text... (start)");
        System.out.println("Converting PDF to XML...");
        // Convert PDF to XML.
        ProcessBuilder pbPdftoXml
                = new ProcessBuilder("./pdftoxml");
        pbPdftoXml.directory(new File("./pdf2text/bin/"));
        pbPdftoXml.start();

        Thread.sleep(5000);
        // Move XML to correct folder.
        ProcessBuilder pbMoveXml
                = new ProcessBuilder("./movefiles");
        pbMoveXml.directory(new File("./pdf2text/bin/"));
        pbMoveXml.start();

        // Wait a second.
        Thread.sleep(5000);
        System.out.println("Formatting XML...");
        
        // Format XML.
        //ProcessBuilder pbFormatXml
        //        = new ProcessBuilder("./formatxml");
        //pbFormatXml.directory(new File("./pdf2text/bin/"));
        //pbFormatXml.start();

        // Do this without xmllint.
        // Collect all xml files.
        ArrayList<File> xmlFiles = new ArrayList<>();
        collectFiles(xmlFiles, "./pdf2text/xml/");
        for(File f : xmlFiles) {
            if(f.getName().endsWith(".xml")) {
                System.out.println("Formatting: " + f.getAbsolutePath());
                String rval = XMLFormatter.format(f.getAbsolutePath());
                // Write formatted stuff to new output file.
                PrintWriter w = new PrintWriter(new File(f.getAbsolutePath() + "formatted.xml"));
                w.write(rval);
                w.flush();
                w.close();
            }
        }
        
        
        Thread.sleep(TIME_TO_WAIT);
        // Extract Plaintext.
        System.out.println("Preprocessing references.");
        PDFtoTextPreprocessor.preprocessReferences("pdf2text/xml/");

        Thread.sleep(TIME_TO_WAIT);

        System.out.println("Postprocessing references.");
        PDFtoTextPostprocessor.postprocessReferences("pdf2text/xml/");

        Thread.sleep(TIME_TO_WAIT);
        System.out.println("Postprocessing text files.");
        PlainTextPostProcessor.postProcessTextFiles("pdf2text/xml", "pdf2text/txt");

        System.out.println("...done.");

    }

    /**
     * 
     */
    private static void deleteFiles() {
        System.out.println("Cleaning up directory structure...");
        ArrayList<String> pathsToBeDeleted = new ArrayList<>();
        pathsToBeDeleted.add("pdf2text/xml");
        pathsToBeDeleted.add("pdf2text/txt");

        for (String pTBD : pathsToBeDeleted) {
            ArrayList<File> toDelete = new ArrayList<>();
            // Collect all files within this directory.
            collectFiles(toDelete, pTBD);
            for (File f : toDelete) {
                f.delete();
            }
            toDelete.clear();
        }
    }

    /**
     * Collect all (non-directory) files for a specific folder.
     *
     * @param fileList
     * @param path
     */
    public static void collectFiles(ArrayList<File> fileList, String path) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                collectFiles(fileList, f.getAbsolutePath());
            } else {
                fileList.add(f);
            }
        }
    }
}
