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
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Description: Exports PDFtoXML (xml) output to plaintext output. Checks how
 * the pieces of a reference are structured.
 *
 * @author niko
 */
public class PDFtoTextPreprocessor {

    public static String PATH = "/home/niko/Desktop/pdftoxml/test/";
    //public static String INPUTPDFTOXML = "original_formatted.xml";
    //public static String INPUTPDFTOXML = "spr1formatted.xml";
    //public static String INPUTPDFTOXML = "247_2012_2470_ReferencePDF_2_formatted.xml";
    //public static String INPUTPDFTOXML = "Su4ad1tpADxKR3_nFrgLWA_formatted.xml";

    /**
     *
     * @param args
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        preprocessReferences(PATH);
    }

    /**
     *
     * @param aPath
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     */
    public static void preprocessReferences(String aPath) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {

        ArrayList<File> pdfToXmlFiles = new ArrayList<>();
        collectFiles(pdfToXmlFiles, aPath);
        System.out.println("Collected: " + pdfToXmlFiles.size() + " files.");

        for (File f : pdfToXmlFiles) {

            ArrayList<String> tokenList = new ArrayList<String>();

        // Check if we have a token whose text is 
            // "REFERENCES", "References", "Bibliography" or "BIBLIOGRAPHY".
            // Possibly lower case.
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(f.getAbsolutePath());
            doc.getDocumentElement().normalize();

            System.out.println("Analyzing ... " + f.getName());
            //System.out.println("Root element :" + doc.getDocumentElement().getNodeName() + "\n");

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.evaluate("//*/TEXT/TOKEN"
                    + "[text() = 'REFERENCES' or text() = 'References'"
                    + " or text() = 'Bibliography'"
                    + " or text() = 'BIBLIOGRAPHY'"
                    + " or text() = 'Bibliographie'"
                    + " or text() = 'BIBLIOGRAPHIE'"
                    + " or text() = 'Literatur'"
                    + " or text() = 'LITERATUR']",
                    doc.getDocumentElement(), XPathConstants.NODESET);

            // There should not be more than one reference section.
            if (nodes.getLength() > 0) {
                // Get the 'Reference' text piece.
                Element e = null;
                if (nodes.getLength() == 1) {
                    e = (Element) nodes.item(0);
                }
                if (nodes.getLength() > 1) {
                // We found more than one "Reference" Section.
                    // Get the last one!
                    //    System.out.println("More than one reference section found !!!");
                    e = (Element) nodes.item(nodes.getLength() - 1);
                }

            //System.out.println(e.getNodeName());
                //System.out.println(e.getTextContent());
                // Get the parent node in order to get all TEXT siblings.
                Node parent = e.getParentNode();
                Node page = parent.getParentNode();
                Element pageEl = (Element) page;
                int pageNumber = Integer.parseInt(pageEl.getAttribute("number"));
        //    System.out.println("Extracting references from page number: " + pageNumber);

            //System.out.println(parent.getNodeName());
                // Now get all following siblings.
                Element nextSibling = null;
                Node currentNode = parent;

                getTokenValues(nextSibling, currentNode, tokenList);

            // Check if there is a next page!
                // with the page number!
                while (true) {

                    pageNumber++;
            //    System.out.println("Analyzing next page! " + pageNumber);
                    // Get that page.
                    XPath xPathNextPage = XPathFactory.newInstance().newXPath();
                    NodeList nodesOfNextPage = (NodeList) xPathNextPage.evaluate(
                            "//*/PAGE[@number='" + pageNumber + "']/TEXT",
                            doc.getDocumentElement(), XPathConstants.NODESET);

                    // Get all text nodes of next page.
                    Node n = nodesOfNextPage.item(0);
                    if (n == null) {
                        break;
                    }

                    ///******************************** Refactor. ****///
                    Element nsdf = (Element) n;
                    NodeList tokens = nsdf.getElementsByTagName("TOKEN");

                    double currentEnding = 0.0;

                    for (int j = 0; j < tokens.getLength(); j++) {

                        Element tok = (Element) tokens.item(j);

                // Format:
                        // X-Val -- Base-Val -- Width-Val -- End-Val -- Bold -- Italic -- Font -- Token
                        double xVal = Double.parseDouble(tok.getAttribute("x"));
                        double baseVal = Double.parseDouble(tok.getAttribute("base"));
                        double widthVal = Double.parseDouble(tok.getAttribute("width") + "\t");
                        double endVal = roundTwoDecimals(xVal + widthVal);

                        String token = tok.getTextContent();
                        //System.out.println(token);

                        if (xVal < currentEnding) {
                    // You have to combine two items.
                            // "broken umlaut".
                            //System.out.println("Umlaut broken! Combining with previous token. ");
                            // Combine with previous token.
                            // Get previous entry.
                            String previousLine = tokenList.get(tokenList.size() - 1);
                    //System.out.println("previous. " + previousLine);
                            // Get previous token.
                            String restBeforePreviousToken = previousLine.substring(0, previousLine.lastIndexOf("\t"));
                            String previousToken = previousLine.substring(previousLine.lastIndexOf("\t") + "\t".length());
                            //System.out.println(previousToken);
                            String combinedTokens = previousToken.concat(token);
                            // Normalize !
                            String normalizedTokens = normalizeString(combinedTokens);

                            if (normalizedTokens.equals(combinedTokens)) {
                                System.out.println("No normalization found for: " + combinedTokens);
                            }

                            //System.out.println(combinedTokens);
                            String combinedTokensWithPreviousLineReplaced = restBeforePreviousToken.concat("\t").concat(normalizedTokens);
                            // Remove old one.
                            tokenList.remove(tokenList.size() - 1);
                    // Add new one.
                            // TODO: Adjust end index.
                            tokenList.add(combinedTokensWithPreviousLineReplaced);

                        } else {

                            StringBuilder sb = new StringBuilder();

                            sb.append(xVal + "\t");
                            sb.append(baseVal + "\t");
                            sb.append(widthVal + "\t");
                            sb.append(endVal + "\t");

                            sb.append(tok.getAttribute("bold") + "\t");
                            sb.append(tok.getAttribute("italic") + "\t");
                            sb.append(tok.getAttribute("font-name") + "\t");

                            // Detect hyphenated words at the end of a line.
                            if (j == tokens.getLength() - 1 && token.endsWith("-")) {
                                token = token.concat("HYPHENATED");
                            }
                            sb.append(normalizeString(token));

                    // Check if we have a number at the beginning.
                            // Or a reference of the style [Bla09]
                            // Indicator of a new reference.
                            if ((isNumeric(token) && j == 0)
                                    || (token.startsWith("[") && token.endsWith("]") && j == 0)) {
                                tokenList.add("EndOfReference");
                            }

                            //System.out.println(sb.toString());
                            tokenList.add(sb.toString());

                        }

                        currentEnding = endVal;

                    }

                ///******************************** Refactor. ****///
                    getTokenValues(nextSibling, n, tokenList);

                }

            } else {
                System.out.println("NO reference section found !!!");
            }

            // Writer for tab separated data with all information.
            PrintWriter w = new PrintWriter(new File(f.getAbsolutePath() + "_tabinfo.txt"));
            for (int i = 0; i < tokenList.size(); i++) {
                String t = tokenList.get(i);
                if (t.endsWith("-HYPHENATED")) {
                    t = t.substring(0, t.indexOf("-HYPHENATED"));
                    w.write(t);
                    // Get next line and add rest of token to it.
                    i++;
                    String lastPieceOnNextLine = tokenList.get(i).substring(tokenList.get(i).lastIndexOf("\t") + "\t".length());
                    w.write(lastPieceOnNextLine + "\n");
                } else {
                    w.write(t + "\n");
                }
            }
            w.flush();
            w.close();

            // Plaintext writer for oneliners.
            PrintWriter w2 = new PrintWriter(new File(f.getAbsolutePath() + "_nontok.txt"));
            for (String t : tokenList) {
                if (t.equals("EndOfReference")) {
                    w2.write("\n");
                } else if (t.endsWith("-HYPHENATED")) {
                    t = t.substring(t.lastIndexOf("\t") + "\t".length(), t.indexOf("-HYPHENATED"));
                    w2.write(t);
                } else {
                    w2.write(t.substring(t.lastIndexOf("\t") + "\t".length()) + " ");
                }
            }
            w2.flush();
            w2.close();

            System.out.println();

        }
    }

    public static Element getNextSiblingElement(Node node) {
        Node nextSibling = node.getNextSibling();
        while (nextSibling != null) {
            if (nextSibling.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) nextSibling;
            }
            nextSibling = nextSibling.getNextSibling();
        }

        return null;
    }

    private static void getTokenValues(Element nextSibling, Node n, ArrayList<String> tokenList) {
        if (n == null) {
            return;
        }
        while (true) {
            nextSibling = getNextSiblingElement(n);
            if (nextSibling == null) {
                break;
            }
            // Should be a TEXT node.
            // System.out.println("-> " + nextSibling.getNodeName());

            NodeList tokens = nextSibling.getElementsByTagName("TOKEN");

            double currentEnding = 0.0;

            for (int j = 0; j < tokens.getLength(); j++) {

                Element tok = (Element) tokens.item(j);

                // Format:
                // X-Val -- Base-Val -- Width-Val -- End-Val -- Bold -- Italic -- Font -- Token
                double xVal = Double.parseDouble(tok.getAttribute("x"));
                double baseVal = Double.parseDouble(tok.getAttribute("base"));
                double widthVal = Double.parseDouble(tok.getAttribute("width") + "\t");
                double endVal = roundTwoDecimals(xVal + widthVal);

                String token = tok.getTextContent();
                //System.out.println(token);

                if (xVal < currentEnding) {
                    // You have to combine two items.
                    // "broken umlaut".
                    //System.out.println("Umlaut broken! Combining with previous token. ");
                    // Combine with previous token.
                    // Get previous entry.
                    String previousLine = tokenList.get(tokenList.size() - 1);
                    //System.out.println("previous. " + previousLine);
                    // Get previous token.
                    String restBeforePreviousToken = previousLine.substring(0, previousLine.lastIndexOf("\t"));
                    String previousToken = previousLine.substring(previousLine.lastIndexOf("\t") + "\t".length());
                    //System.out.println(previousToken);
                    String combinedTokens = previousToken.concat(token);
                    // Normalize !
                    String normalizedTokens = normalizeString(combinedTokens);

                    if (normalizedTokens.equals(combinedTokens)) {
                        System.out.println("No normalization found for: " + combinedTokens);
                    }

                    //System.out.println(combinedTokens);
                    String combinedTokensWithPreviousLineReplaced = restBeforePreviousToken.concat("\t").concat(normalizedTokens);
                    // Remove old one.
                    tokenList.remove(tokenList.size() - 1);
                    // Add new one.
                    // TODO: Adjust end index.
                    tokenList.add(combinedTokensWithPreviousLineReplaced);

                } else {

                    StringBuilder sb = new StringBuilder();

                    sb.append(xVal + "\t");
                    sb.append(baseVal + "\t");
                    sb.append(widthVal + "\t");
                    sb.append(endVal + "\t");

                    sb.append(tok.getAttribute("bold") + "\t");
                    sb.append(tok.getAttribute("italic") + "\t");
                    sb.append(tok.getAttribute("font-name") + "\t");

                    // Detect hyphenated words at the end of a line.
                    if (j == tokens.getLength() - 1 && token.endsWith("-")) {
                        token = token.concat("HYPHENATED");
                    }
                    sb.append(normalizeString(token));

                    // Check if we have a number at the beginning.
                    // Or a reference of the style [Bla09]
                    // Indicator of a new reference.
                    if ((isNumeric(token) && j == 0)
                            || (token.startsWith("[") && token.endsWith("]") && j == 0)) {
                        tokenList.add("EndOfReference");
                    }

                    //System.out.println(sb.toString());
                    tokenList.add(sb.toString());

                }

                currentEnding = endVal;

            }

            // Check if this is really the end of the reference.
            n = nextSibling;

        }

    }

    private static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
    }

    /*
     * TODO: Add more from 
     * http://www.theukwebdesigncompany.com/articles/entity-escape-characters.php
     * 
     */
    private static String normalizeString(String anInput) {

        return anInput
                .replace("iﬁ", "ifi")
                .replace("ﬂ", "fl")
                .replace("ﬁ", "fi")
                .replace("ﬀ", "ff")
                .replace("ﬀ", "ff")
                .replace("ﬃ", "ffi")
                //.replace("’", "'") // ?

                .replace("˘a", "")
                .replace("C ¸ ", "Ç")
                .replace("C ¸", "Ç")
                .replace("˚ A", "Å")
                .replace("´ ı", "í")
                .replace("˚ a", "å")
                .replace("¨ ı", "ï")
                .replace("´ ı", "í")
                .replace("˜ a", "ã")
                .replace("ˆa", "â")
                .replace("˘a", "ă")
                .replace("˜ n", "ñ")
                .replace("˜ o", "õ")
                .replace("´ y", "ý")
                .replace("´a", "á")
                .replace("´A", "Á")
                .replace("´e", "é")
                .replace("´E", "É")
                .replace("´ı", "í") // PDF.
                .replace("´i", "í")
                .replace("´I", "Í")
                .replace("´o", "ó")
                .replace("´O", "Ó")
                .replace("´u", "ú")
                .replace("´U", "Ú")
                .replace("´y", "ý")
                .replace("´Y", "Ý")
                // DBLP XML specific
                .replace("¨a", "ä")
                .replace("¨e", "ë")
                .replace("¸t", "ţ")
                .replace("¸s", "ş")
                .replace("¨o", "ö")
                .replace("¨i", "&iuml;")
                .replace("¨u", "ü")
                .replace("¨A", "Ä")
                .replace("¨E", "Ë")
                .replace("¨I", "&Iuml;")
                .replace("¨O", "Ö")
                .replace("¨U", "Ü")
                .replace("`e", "è")
                .replace("`a", "à")
                .replace("`i", "ì")
                .replace("`o", "ò")
                .replace("`u", "ù")
                .replace("`A", "À")
                .replace("`E", "È")
                .replace("`I", "Ì")
                .replace("`O", "Ò")
                .replace("`U", "Ù");

        //                        replace("&ntilde;", "{\\~n}").
        //                        
        //                        
        //                        // The following are
        //                        // Not properly replaced !!!
        //                        
        //                      replace("aring", "&aring;").
    }

    public static boolean isNumeric(String str) {
        double d;
        try {
            d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        // Nice trick:
        // If a line starts with a number that could possibly be a reference enumeration.
        // However large numbers are unlikely to be such an enumeration.
        // Heristic: Only numbers below a threshold are considered enumerations.
        return d < 160.0;
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
                if (f.getName().endsWith("formatted.xml")) {
                    fileList.add(f);
                }
            }
        }
    }
}
