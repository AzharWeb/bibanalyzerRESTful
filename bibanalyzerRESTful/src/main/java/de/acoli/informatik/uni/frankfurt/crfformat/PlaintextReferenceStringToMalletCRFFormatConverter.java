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

package de.acoli.informatik.uni.frankfurt.crfformat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description: Reads in automatically annotated XML-format references, such as:
 *
 *
 * T. <editor-lastname>Hey</editor-lastname>, S.
 * <editor-lastname>Tansley</editor-lastname>
 * , and K. M. <editor-lastname>Tolle</editor-lastname>, Eds.,
 * <title>The Fourth Paradigm: Data-Intensive Scientific Discovery</title>.
 * <publisher>Microsoft Research</publisher>, <year>2009</year>. [Online].
 * Available: <url>db/books/collections/4paradigm2009.html</url>
 *
 *
 * and converts it into line separated Mallet format for CRF training.
 *
 *
 *
 * @author niko
 *
 */
public class PlaintextReferenceStringToMalletCRFFormatConverter {

    // Replacement symbols.
    public static final HashMap<String, String> rs = new HashMap<String, String>();

    static {
        // cf. http://myhandbook.info/codes_htmlchr.html

        // Example splitting:
        // Input:  <title>A (really) nice, cool title.</title>
        // Output: <title>A/(/really/)/nice/,/cool/title/.</title>
        rs.put(" ", "<&nbsp;>");
        rs.put(".", "<&period;>");
        rs.put(",", "<&comma;>");
        rs.put("(", "<&brackl>");
        rs.put(")", "<&brackr>");
        rs.put(":", "<&colon;>");
        rs.put("-", "<&hypen;>");
        rs.put("–", "<&hypen;>"); // Springer.
        rs.put("[", "<&sbrackl>");
        rs.put("]", "<&sbrackr>");
        rs.put("/", "<&slash>");

        // Werden Satz-intern noch nicht gesplittet.
        rs.put(";", "<&semicolon;>");
        rs.put("\"", "<&quot;>");  // self-defined.
        rs.put("'", "<&accent>");  // self-defined.
        rs.put("&", "<&amp;>");
        rs.put("‘", "<&lsquo;>");
        rs.put("’", "<&lsquo;>");
        rs.put("“", "<&ldquo;>");
        rs.put("„", "<&ldquo;>");
        rs.put("”", "<&rdquo>");
        rs.put("′", "<&prime;>");
        rs.put("″", "<&Prime;>");  // careful: different from above "&quot".

    }

    /**
     * Convert a plaintext reference string to Mallet format tokenized new-line
     * separated format.
     *
     * Non-annotated spans in the text are labeled "dummy" if they don't match a
     * tokenization symbol.
     *
     * @param aReference
     * @param w
     * @throws FileNotFoundException
     */
    public static void convertReferenceString(String aReference, PrintWriter w) throws FileNotFoundException {

        // Handle beginning of reference (in case it does not start with a tag right away.)
        if (aReference.charAt(0) != '<') {
            // Add dummy tag to beginning of string.
            aReference = "<dummy>" + aReference;
        }

        // Map which will be sorted by index later to get original
        // sequence of strings + labels from input reference.
        // The ArrayList contains TWO items: the string and the label 
        // (e.g. Microsoft Research|<publisher>)
        HashMap<Integer, ArrayList<String>> startIdxToStringAndLabel = new HashMap<Integer, ArrayList<String>>();

        ///*******************
        ///********** GET ALL BEGIN TAGS.
        ///*******************
        //       System.out.println("Analyzing string: \"" + aReference + "\"");
        ArrayList<String> startTags = new ArrayList<String>();
        for (int i = 0; i < aReference.length(); i++) {
            char aChar = aReference.charAt(i);
            if (i + 1 == aReference.length()) {
                break;
            }
            char nextChar = aReference.charAt(i + 1);

            if (aChar == '<' && nextChar != '/') {
                // Collect tag name.
                StringBuilder tag = new StringBuilder();
                while (aChar != '>') {
                    if (i == aReference.length()) {
                        break;
                    }
                    aChar = aReference.charAt(i);
                    tag.append(aChar);
                    i++;
                }
                //System.out.println(tag);
                startTags.add(tag.toString());
            }

        }

        //     System.out.println();
        String previousTag = "";
        int currentOccurrenceIdx = 0;
        ArrayList<String> alreadySeenTags = new ArrayList<>();
        for (int i = 0; i < startTags.size(); i++) {
            String currentTag = startTags.get(i);
            int idx = 0;
            if (previousTag.equals(currentTag) || alreadySeenTags.contains(currentTag)) { // nothing has changed - get next index.
                idx = aReference.indexOf(currentTag, currentOccurrenceIdx + 1);

            } else {
                idx = aReference.indexOf(currentTag);
            }

            String partRef = aReference.substring(idx + currentTag.length());
            int j = 0;
            char aChar = partRef.charAt(j);
            StringBuilder sb = new StringBuilder();
            while (aChar != '<') {
                sb.append(aChar);
                j++;

                if (j >= partRef.length()) {
                    System.out.println("ERROR.");
                    break;
                } // IST DAS KORREKT ?!?!?!?!?
                aChar = partRef.charAt(j);

            }
                 //    System.out.print("string: \"" + sb + "\" | ");
            //    System.out.println("label: " + currentTag + " idx: " + idx);
            alreadySeenTags.add(currentTag);
            ArrayList<String> stringPlusLabel = new ArrayList<String>();
            stringPlusLabel.add(sb.toString());
            stringPlusLabel.add(currentTag);
            startIdxToStringAndLabel.put(idx, stringPlusLabel);

            previousTag = currentTag;
            currentOccurrenceIdx = idx;
        }

        ///*******************
        ///********** GET ALL END TAGS.
        ///*******************
        ArrayList<String> endTags = new ArrayList<String>();
        for (int i = 0; i < aReference.length(); i++) {
            char aChar = aReference.charAt(i);
            if (i + 1 == aReference.length()) {
                break;
            }
            char nextChar = aReference.charAt(i + 1);

            if (aChar == '<' && nextChar == '/') {
                // Collect tag name.
                StringBuilder tag = new StringBuilder();
                while (aChar != '>') {
                    if (i == aReference.length()) {
                        break;
                    }
                    aChar = aReference.charAt(i);
                    tag.append(aChar);
                    i++;
                }
                //System.out.println(tag);
                endTags.add(tag.toString());
            }
        }

        String previousTag2 = "";
        int currentOccurrenceIdx2 = 0;
        ArrayList<String> alreadySeenTags2 = new ArrayList<>();
        for (int i = 0; i < endTags.size(); i++) {
            String currentTag2 = endTags.get(i);
            int idx = 0;
            if (previousTag2.equals(currentTag2) || alreadySeenTags2.contains(currentTag2)) { // nothing has changed - get next index.
                idx = aReference.indexOf(currentTag2, currentOccurrenceIdx2 + 1);

            } else {
                idx = aReference.indexOf(currentTag2);
            }

            String partRef = aReference.substring(idx + currentTag2.length());
            int j = 0;
            if (j >= partRef.length()) {
                break;
            }
            char aChar = partRef.charAt(j);
            StringBuilder sb = new StringBuilder();
            while (aChar != '<') {
                sb.append(aChar);
                j++;
                if (j >= partRef.length()) {
                    break;
                }
                aChar = partRef.charAt(j);
            }
            //         System.out.print("string: \"" + sb + "\" | ");
            //         System.out.println("label: " + "dummy"+ " idx: " + idx);
            alreadySeenTags2.add(currentTag2);
            ArrayList<String> stringPlusLabel = new ArrayList<String>();
            stringPlusLabel.add(sb.toString());
            stringPlusLabel.add("<dummy>");
            startIdxToStringAndLabel.put(idx, stringPlusLabel);

            previousTag2 = currentTag2;
            currentOccurrenceIdx2 = idx;
        }

        w.write("BOR <BOR>\n");
        w.write("&nbsp; <&nbsp;>\n"); // Add this space so that an unknown reference
        // can be correctly tokenized. BOR_A. Schmidt, B. Schulz...

        w.flush();
        // Sort map by keys.
        //System.out.println();
        Map<Integer, ArrayList<String>> sortedmap = new TreeMap<Integer, ArrayList<String>>(startIdxToStringAndLabel);
        for (int aStartIndex : sortedmap.keySet()) {
            ArrayList<String> stringPlusLabel = sortedmap.get(aStartIndex);
            String string = stringPlusLabel.get(0);
            String label = stringPlusLabel.get(1);

            //System.out.println(string + " " + label);
            // Handelt spans der Form <author>Firstname Lastname</author>, wobei
            // der ganze Span einen zu annotierenden Delimiter enthält (hier: whitespace).
            // Auch Punkt (dot) <title>A good title.</title>
            // Oder Komma <title>Nice, little title</title>
            // Alles was INNERHALB der spans an delimitern zu finden ist muss hier deklariert werden
            // in der Regex.
            String[] field = string.split(
                    "(?<=\\s)|(?=\\s)|" + // space
                    "(?<=\\.)|(?=\\.)|" + // period
                    "(?<=,)|(?=,)|" + // comma
                    "(?<=\\))|(?=\\))|" + // closing bracket 
                    "(?<=\\()|(?=\\()|" + // opening bracket
                    "(?<=:)|(?=:)|" + // colon
                    "(?<=-)|(?=-)|" + // hyphen
                    "(?<=–)|(?=–)|" + // hyphen
                    "(?<=“)|(?=“)|" + //
                    "(?<=”)|(?=”)|"
                    + "(?<=')|(?=')|"
                    + "(?<=/)|(?=/)|"
                    + "(?<=„)|(?=„)|"
                    + "(?<=\\[)|(?=\\[)|" + // opening square bracket
                    "(?<=\\])|(?=\\])" // closing square bracket
            );

            String curLab = label;

            boolean writeFontInfo = true;

            for (String piece : field) {
                if (piece.length() > 0) {  // Debug das! weiß nicht was das bedeutet.

                    if (rs.get(piece) != null) {
                        curLab = rs.get(piece);
                    }

                    if (piece.equals(" ")) {
                        piece = "&nbsp;";
                    }

                    if (label.equals("<dummy>")) {
                        w.write(piece + " " + curLab + "\n");
                    } else {

                        if (label.contains("~")) {
                            String raw_label = label.substring(0, label.indexOf("~")) + ">";
                            String fontInfo = label.substring(label.indexOf("~") + 1, label.length() - 1);
                            if (writeFontInfo) {
                                w.write(piece + " <" + fontInfo + "> " + raw_label + "\n");
                            } else {
                                w.write(piece + " " + raw_label + "\n");
                            }

                        } else {
                            // label is only italics from training data.
                            //and has no real label. Should be "dummy" with italics information.
                            if (label.equals("<Italic>") || label.equals("<Bold>")) {
                                if (writeFontInfo) {
                                    w.write(piece + " " + label + " " + curLab.replace(label, "<dummy>") + "\n");
                                } else {
                                    w.write(piece + " " + curLab.replace(label, "<dummy>") + "\n");
                                }
                            } else {
                                // normal. only one feature (the 'token'):
                                w.write(piece + " " + label + "\n");
                            }
                        }
                    }

                    curLab = label; // reset.
                } else {
                    //w.write("BUG!\n");
                }
            }

        }

        w.write("&nbsp; <&nbsp;>\n");
        w.write("EOR <EOR>\n\n");
        w.flush();
        //System.out.println("EOR <eor>");

    }

    /**
     * Test client.
     *
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("Simple test client.\n\n");
        //String aRef = "<author-fullname>Sunisa Rimch(aroen</author-fullname>, <author-fullname>Daricha Sutivong</author-fullname>, and <author-fullname>Prabhas Chongstitvatana</author-fullname>. <title>Optimal stopping time of compact genetic algorithm on deceptive problem using real options analysis.</title> In <booktitle>IEEE Congress on Evolutionary Computation</booktitle>, pages <pages>4668-4675</pages>, <year>2007</year>. URL <url>db/conf/cec/cec2007.html#RimcharoenSC07</url>";
        //String aRef = "M.H. <author-lastname>Kazemi</author-lastname>, M. <author-lastname>Karrari</author-lastname>, M.B. <author-lastname>Menhaj</author-lastname>, <journal>Eur. J. Control,</journal> <volume>9</volume>(<number>5</number>), 474 (<year>2003</year>). URL <url>db/journals/ejcon/ejcon9.html#KazemiKM03</url>";
        //String aRef = "<author-lastname>Sun</author-lastname>, <Initials>L</Initials>, <author-lastname>Ge</author-lastname>, <Initials>H</Initials>, <author-lastname>Yoshida</author-lastname>, <Initials>S</Initials>, <author-lastname>Liang</author-lastname>, <Initials>Y</Initials>, <author-lastname>Tan</author-lastname>, <Initials>G</Initials> (<year>2014</year>) <title>Support vector description of clusters for content-based image annotation.</title> <journal>Pattern Recognition</journal> <volume>47</volume>(<number>3</number>):<pages>1361-1374</pages>, URL <url>db/journals/pr/pr47.html#SunGYLT14</url>";
        //String aRef = "<author-fullname>Antonio Dell'Aquila</author-fullname>, <author-fullname>Marco Liserre</author-fullname>, <author-fullname>Vito Giuseppe Monopoli</author-fullname>, and <author-fullname>Paola Rotondo</author-fullname>. <title>An energy-based control for an n-H-bridges multilevel active rectifier.</title> <journal>IEEE Transactions on Industrial Electronics</journal>, <volume>52</volume>(<number>3</number>):<pages>670-678</pages>, <year>2005</year>. URL <url>db/journals/tie/tie52.html#DellAquilaLMR05</url>.";
        // TODO:
        String aRef = "<author-lastname>Chae</author-lastname>, <Initials>H.</Initials>, <author-lastname>Kim</author-lastname>, <Initials>K.</Initials>, <author-lastname>Ran</author-lastname>, <Initials>R.</Initials> & <author-lastname>Kim</author-lastname>, <Initials>D. K.</Initials> (<year>2013</year>), ‘<title>A Single Feedback Based Interference Alignment for Three-User MIMO Interference Channels with Limited Feedback.</title>', <journal>TIIS</journal> <volume>7</volume>(<number>4</number>), <pages>692-710</pages>. URL: <url>db/journals/itiis/itiis7.html#ChaeKRK13</url>";

        // New initials.
        //String aRef = "<Initials>G</Initials>. <Initials>J</Initials>. <Initials>M</Initials>. <author-lastname>Smit</author-lastname>, <Initials>A</Initials>. <Initials>B</Initials>. <Initials>J</Initials>. <author-lastname>Kokkeler</author-lastname>, <Initials>P</Initials>. <Initials>T</Initials>. <author-lastname>Wolkotte</author-lastname>, <Initials>P</Initials>. <Initials>K</Initials>. <Initials>F</Initials>. <author-lastname>Hölzenspies</author-lastname>, <Initials>M</Initials>. <Initials>D</Initials>. <Initials>van</Initials> <author-lastname>Burgwal</author-lastname>, and <Initials>P</Initials>. <Initials>M</Initials>. <author-lastname>Heysters</author-lastname>, “<title>The Chameleon Architecture for Streaming DSP Applications.</title>” <journal>EURASIP J. Emb. Sys.</journal>, vol. <year><volume>2007</volume></year>, 2007. [Online]. Available: <url>db/journals/ejes/ejes2007.html#SmitKWHBH07</url>";
        //String aRef = "Water, Air, and Soil Pollution";

        System.out.println();
        System.out.println(aRef);

        // http://stackoverflow.com/questions/15769028/java-regex-to-strip-out-xml-tags-but-not-tag-contents
        String substituted = aRef.replaceAll("<[^>]+>", "");
        System.out.println(substituted + "\n");

        PrintWriter w = new PrintWriter(new File("/home/niko/Desktop/out.txt"));
        convertReferenceString(aRef, w);
        w.close();
    }
}
