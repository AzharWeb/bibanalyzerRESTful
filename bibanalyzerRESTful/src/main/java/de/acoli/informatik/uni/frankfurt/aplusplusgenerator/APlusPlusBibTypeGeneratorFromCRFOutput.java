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

package de.acoli.informatik.uni.frankfurt.aplusplusgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import de.acoli.informatik.uni.frankfurt.processing.CRFOutputReader;

/**
 * Description: Generates A++ XML from CRF tagged text file for three different
 * bibtypes:
 *
 * BibArticle BibBook BibChapter
 *
 *
 * @author niko
 */
public class APlusPlusBibTypeGeneratorFromCRFOutput {

    // One token per line predicted fileds for raw-tokenized references.
    public static final String CRF_PREDICTED_OUTPUT = "/home/niko/Desktop/in.txt";
    // A++ output file.
    public static final String WRITE_TO_OUTPUT_FILE = "/home/niko/Desktop/out.xml";

    /**
     * Demo client.
     *
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException {

        PrintWriter w = new PrintWriter(new File(WRITE_TO_OUTPUT_FILE));
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<?xml-stylesheet type=\"text/css\" href=\"../stylesheet/References_Stylesheet.css\"?>\n"
                + "<Bibliography ID=\"Bib\">\n"
                + "<Heading>References</Heading>\n\n");

        ArrayList<ArrayList<String[]>> referencesPlusTokens = CRFOutputReader.getPredictedTokensAndTagsForReferences(CRF_PREDICTED_OUTPUT, false);

        int crCounter = 0;
        // Cafeful: Demo client assumes that every bibtype is a "BibArticle".
        for (ArrayList<String[]> aReferencePlusTokens : referencesPlusTokens) {
            generateAPlusPlus(w, "BibArticle", aReferencePlusTokens, crCounter);
            crCounter++;
        }

        w.write("</Bibliography>\n");

        w.flush();
        w.close();
    }

    /**
     * Generate A++ Springer format for a single citation.
     *
     * @param w, the output writer.
     * @param bibtype, the current bibtype for this reference.
     * @param aReferencePlusTokensAndTags,
     * @param crCounter
     * @throws FileNotFoundException
     */
    public static void generateAPlusPlus(PrintWriter w, String bibtype, ArrayList<String[]> aReferencePlusTokensAndTags, int crCounter) throws FileNotFoundException {

        w.write(" <Citation ID=\"CR" + crCounter + "_" + bibtype + "\">\n"
                + " <!-- BibTeX Type: " + bibtype + " -->\n"
                + "  <" + bibtype + ">\n");

//            System.out.println("\nAnalyzing this reference... Num tokens : " + aReferencePlusTokens.size());
        // Find Authors.
        String initials = findInitialsField(aReferencePlusTokensAndTags, "Initials");
//            System.out.println(initials);

        String authors = findField(aReferencePlusTokensAndTags, "FamilyName");
//            System.out.println(authors);

        // Combine initials and authors.
        String[] initialsSplit = initials.split("\\|");
        String[] authorsSplit = authors.split("\\|");

//            System.out.println(initialsSplit.length + " " + authorsSplit.length);
        int until = 0;
        if (initialsSplit.length < authorsSplit.length) {
            until = initialsSplit.length;
        } else {
            until = authorsSplit.length;
        }

        // ! -1
        for (int i = 0; i < until; i++) {
            String initialsLocal = initialsSplit[i];
            String authorsLocal = authorsSplit[i];
//                System.out.println("-> " + initialsLocal.trim() + "/" + authorsLocal.trim());
            // Generate XML here.
            w.write("   <BibAuthorName>\n"
                    + "     <Initials>" + initialsLocal.trim() + "</Initials>\n"
                    + "     <FamilyName>" + authorsLocal.trim() + "</FamilyName>\n"
                    + "   </BibAuthorName>\n"
            );
        }

        // TODO: Add BibEditor!
        //  w.write("   <BibEditorName>\n"
        //   + "     <Initials>" + initialsLocal.trim() + "</Initials>\n"
        //   + "     <FamilyName>" + authorsLocal.trim() + "</FamilyName>\n"
        //   + "   </BibEditorName>\n"
        //if(editorsFound) {
        //    w.write("   <Eds/>\n");
        //}
        // Find Year.
        String year = findField(aReferencePlusTokensAndTags, "Year");
        year = cleanField(year);
//            System.out.println("Year: " + year);
        w.write("   <Year>" + year + "</Year>\n");

        switch (bibtype) {
            case "BibArticle": {
                // Find title.
                String title = findField(aReferencePlusTokensAndTags, "ArticleTitle");
                title = cleanField(title);
//            System.out.println("Title: " + title);
                // TODO: Improve langauge detection.
                w.write("   <ArticleTitle Language=\"En\">" + title + "</ArticleTitle>\n");
                // Find Journal.
                String journal = findField(aReferencePlusTokensAndTags, "JournalTitle");
                journal = cleanField(journal);
//            System.out.println("Journal: " + journal);
                w.write("   <JournalTitle>" + journal + "</JournalTitle>\n");
                // Find Volume.
                String volume = findField(aReferencePlusTokensAndTags, "VolumeID");
                volume = cleanField(volume);
//            System.out.println("Volume: " + volume);
                w.write("   <VolumeID>" + volume + "</VolumeID>\n");
                // Find Pages.
                String firstPage = findField(aReferencePlusTokensAndTags, "FirstPage");
                firstPage = cleanField(firstPage);
                //System.out.println("FirstPage: " + firstPages);
                w.write("   <FirstPage>" + firstPage + "</FirstPage>\n");
                String lastPage = findField(aReferencePlusTokensAndTags, "LastPage");
                lastPage = cleanField(lastPage);
                //System.out.println("LastPage: " + lastPages);
                w.write("   <LastPage>" + lastPage + "</LastPage>\n");
                // Find Number.
                String number = findField(aReferencePlusTokensAndTags, "Issue");
                number = cleanField(number);
//            System.out.println("Number: " + number);
                //w.write("  <!-- <BibArticleNumber>" + number + "</BibArticleNumber> -->\n");
                w.write("   <IssueID>" + number + "</IssueID>\n");
                // Find Url.
                String url = findField(aReferencePlusTokensAndTags, "Url");
                url = cleanField(url);
//            System.out.println("URL: " + url);
                // Valides A++.
                //w.write("   <Occurrence Type=\"URL\">\n"
                //        + "     <URL>" + url + "</URL>\n"
                //        + "     </Occurrence>\n"
                //        + "  ");
                // Pretty visualization.
                w.write("     <URL>" + url + "</URL>\n");
                w.write("  </BibArticle>\n");
                String bibUnstructured = getBibUnstructred(aReferencePlusTokensAndTags);
                w.write("  <BibUnstructured>" + bibUnstructured + "</BibUnstructured>\n");
                crCounter++;
                w.write(" </Citation>\n\n");
                w.flush();
                break;
            }
            case "BibBook": {
                // Find title.
                String title = findField(aReferencePlusTokensAndTags, "BookTitle");
                title = cleanField(title);
//            System.out.println("Title: " + title);
                // TODO: Improve langauge detection.
                w.write("   <BookTitle>" + title + "</BookTitle>\n");
                // Find Number.
                String number = findField(aReferencePlusTokensAndTags, "Number");
                number = cleanField(number);
//            System.out.println("Number: " + number);
                w.write("   <EditionNumber>" + number + "</EditionNumber>\n");
                // Find Series.
                String series = findField(aReferencePlusTokensAndTags, "SeriesTitle");
                series = cleanField(series);
//            System.out.println("Series: " + series);
                w.write("   <SeriesTitle Language=\"En\">" + series + "</SeriesTitle>\n");
                // Find Volume.
                String volume = findField(aReferencePlusTokensAndTags, "VolumeID");
                volume = cleanField(volume);
//            System.out.println("Volume: " + volume);
                w.write("   <!-- volume ! -->\n");
                w.write("   <NumberInSeries>" + volume + "</NumberInSeries>\n");
                // Find Volume.
                String publisher = findField(aReferencePlusTokensAndTags, "PublisherName");
                publisher = cleanField(publisher);
//            System.out.println("Publisher: " + publisher);
                w.write("   <PublisherName>" + publisher + "</PublisherName>\n");
                String publisherLocation = findField(aReferencePlusTokensAndTags, "PublisherLocation");
                publisherLocation = cleanField(publisherLocation);
//            System.out.println("Publisher: " + publisherLocation);
                w.write("   <PublisherLocation>" + publisherLocation + "</PublisherLocation>\n");
                // Find Pages.
                String firstPage = findField(aReferencePlusTokensAndTags, "FirstPage");
                firstPage = cleanField(firstPage);
                //System.out.println("FirstPage: " + firstPages);
                w.write("   <FirstPage>" + firstPage + "</FirstPage>\n");
                String lastPage = findField(aReferencePlusTokensAndTags, "LastPage");
                lastPage = cleanField(lastPage);
                //System.out.println("LastPage: " + lastPages);
                w.write("   <LastPage>" + lastPage + "</LastPage>\n");
                // Find Url.
                String url = findField(aReferencePlusTokensAndTags, "Url");
                url = cleanField(url);
//            System.out.println("URL: " + url);
                // Valides A++.
                //w.write("   <Occurrence Type=\"URL\">\n"
                //        + "     <URL>" + url + "</URL>\n"
                //        + "     </Occurrence>\n"
                //        + "  ");
                // Pretty visualization.
                w.write("     <URL>" + url + "</URL>\n");
                // Find ISBN.
                String isbn = findField(aReferencePlusTokensAndTags, "Isbn");
                isbn = cleanField(isbn);
//            System.out.println("ISBN: " + isbn);
                w.write("   <Isbn>" + isbn + "</Isbn>\n");
                w.write("  </BibBook>\n");
                String bibUnstructured = getBibUnstructred(aReferencePlusTokensAndTags);
                w.write("  <BibUnstructured>" + bibUnstructured + "</BibUnstructured>\n");
                crCounter++;
                w.write(" </Citation>\n\n");
                w.flush();
                break;
            }
            case "BibChapter": {
                // Find chapter title.
                String chaptertitle = findField(aReferencePlusTokensAndTags, "ChapterTitle");
                //           System.out.println(chaptertitle);
                chaptertitle = cleanField(chaptertitle);
                //           System.out.println("Chaptertitle: " + chaptertitle);
                // TODO: Improve langauge detection.
                w.write("   <ChapterTitle Language=\"En\">" + chaptertitle + "</ChapterTitle>\n");
                // Find booktitle.
                String booktitle = findField(aReferencePlusTokensAndTags, "BookTitle");
                booktitle = cleanField(booktitle);
                //           System.out.println("Booktitle: " + booktitle);
                // TODO: Improve langauge detection.
                w.write("   <BookTitle>" + booktitle + "</BookTitle>\n");
                // Find Number.
                String number = findField(aReferencePlusTokensAndTags, "EditionNumber");
                number = cleanField(number);
                //           System.out.println("Number: " + number);
                w.write("   <EditionNumber>" + number + "</EditionNumber>\n");
                // Find Series.
                String series = findField(aReferencePlusTokensAndTags, "SeriesTitle");
                series = cleanField(series);
                //           System.out.println("Series: " + series);
                w.write("   <SeriesTitle Language=\"En\">" + series + "</SeriesTitle>\n");
                // Find Volume.
                String volume = findField(aReferencePlusTokensAndTags, "NumberInSeries");
                volume = cleanField(volume);
                //           System.out.println("Volume: " + volume);
                w.write("   <!-- volume ! -->\n");
                w.write("   <NumberInSeries>" + volume + "</NumberInSeries>\n");
                // Find Volume.
                String publisher = findField(aReferencePlusTokensAndTags, "PublisherName");
                publisher = cleanField(publisher);
                //           System.out.println("Publisher: " + publisher);
                w.write("   <PublisherName>" + publisher + "</PublisherName>\n");
                String publisherLocation = findField(aReferencePlusTokensAndTags, "PublisherLocation");
                publisherLocation = cleanField(publisherLocation);
                //         System.out.println("PublisherLocation: " + publisherLocation);
                w.write("   <PublisherLocation>" + publisherLocation + "</PublisherLocation>\n");
                // Find Pages.
                String firstPage = findField(aReferencePlusTokensAndTags, "FirstPage");
                firstPage = cleanField(firstPage);
                //System.out.println("FirstPage: " + firstPages);
                w.write("   <FirstPage>" + firstPage + "</FirstPage>\n");
                String lastPage = findField(aReferencePlusTokensAndTags, "LastPage");
                lastPage = cleanField(lastPage);
                //System.out.println("LastPage: " + lastPages);
                w.write("   <LastPage>" + lastPage + "</LastPage>\n");
                // Find Url.
                String url = findField(aReferencePlusTokensAndTags, "Url");
                url = cleanField(url);
                //           System.out.println("URL: " + url);
                // Valides A++.
                //w.write("   <Occurrence Type=\"URL\">\n"
                //        + "     <URL>" + url + "</URL>\n"
                //        + "     </Occurrence>\n"
                //        + "  ");
                // Pretty visualization.
                w.write("     <URL>" + url + "</URL>\n");
                // Find ISBN.
                String isbn = findField(aReferencePlusTokensAndTags, "Isbn");
                isbn = cleanField(isbn);
                //           System.out.println("ISBN: " + isbn);
                w.write("   <Isbn>" + isbn + "</Isbn>\n");
                w.write("  </BibChapter>\n");
                String bibUnstructured = getBibUnstructred(aReferencePlusTokensAndTags);
                w.write("  <BibUnstructured>" + bibUnstructured + "</BibUnstructured>\n");
                crCounter++;
                w.write(" </Citation>\n\n");
                w.flush();
                break;
            }
            default:
                System.out.println("Wrong bibtype: \"" + bibtype + "\"");
                break;

        }
    }

    /**
     * 
     * @param aReferencePlusTokens
     * @return 
     */
    private static String getBibUnstructred(ArrayList<String[]> aReferencePlusTokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aReferencePlusTokens.size(); i++) {
            String[] tokPlusLab = aReferencePlusTokens.get(i);
            String lab = tokPlusLab[0];
            String tok = tokPlusLab[1];
            sb.append(tok);
        }
        return sb.toString().replace("&nbsp;", " ").replace("&", "&amp;").trim();
    }

    /**
     * 
     * @param aField
     * @return 
     */
    private static String cleanField(String aField) {

        if (aField.contains("|")) {

            if (foundSomethingElse(aField)) {

                String[] split = aField.split("\\|");
                String longest = split[0];
                for (int i = 0; i < split.length; i++) {
                    if (split[i].length() > longest.length()) {
                        longest = split[i];
                    }
                }
                aField = longest; // Reset field to the longest match.
            } else {
                return "";
            }
        }

        return aField;
    }

    /**
     * 
     * @param aField
     * @return 
     */
    private static boolean foundSomethingElse(String aField) {
        // Check if aField only consits of pipes. If so, quit.
        boolean foundSomethingElse = false;
        for (int c = aField.length() - 1; c >= 0; c--) {
            char aChar = aField.charAt(c);
            if (aChar != '|') {
                foundSomethingElse = true;
            }
        }
        return foundSomethingElse;
    }

    // "Peter-Hans Müller" wird oft fälschlicherweise als pages erkannt
    // Das CRF hat probleme bzw. lernt mit Bindestriche. 
    private static String cleanPages(String aField) {
        if (aField.contains("|")) {
            if (foundSomethingElse(aField)) {
                String[] split = aField.split("\\|");
                for (int i = 0; i < split.length; i++) {
                    if (split[i].matches(".*\\d.*")) {
                        aField = split[i];
                    }
                }
            }
        } else {
            return "";
        }
        return aField;
    }

    private static String findField(ArrayList<String[]> aReferencePlusTokens, String field) {

        StringBuilder sb = new StringBuilder();
        ArrayList<String> titleToks = new ArrayList<>();
        for (int i = 0; i < aReferencePlusTokens.size(); i++) {
            String[] tokPlusLab = aReferencePlusTokens.get(i);
            String lab = tokPlusLab[0];
            String tok = tokPlusLab[1];
            if (lab.contains(field)) {

                boolean foundNewLabel = false;
                // Collect everything except a new tag.
                while (!foundNewLabel && i < aReferencePlusTokens.size()) {
                    // Get next token.
                    tokPlusLab = aReferencePlusTokens.get(i);
                    lab = tokPlusLab[0];
                    tok = tokPlusLab[1];
                    // Punctuation is part of title.

                    // früher: (nimmt noch die punctuations zwischen den fields mit (blöd).
                    //if (lab.startsWith("<&") || lab.startsWith("<" + field)) {
                    // jetzt: nimmt nur die tags wie sie predicted werden. (gut)
                    if (lab.startsWith("<" + field)) {

                        //später:
                        //if (lab.startsWith("<" + "article-" + field) || lab.startsWith("<" + "book-" + field)) {
                        titleToks.add(tok);
                        sb.append(tok.replace("&nbsp;", " "));
                        i++;
                    } else {
                        foundNewLabel = true;
                        sb.append("|");
                    }

                }

            }
        }
        return sb.toString().replace("&", "&amp;");
    }

    private static String findInitialsField(ArrayList<String[]> aReferencePlusTokens, String field) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < aReferencePlusTokens.size(); i++) {
            String[] tokPlusLab = aReferencePlusTokens.get(i);
            String lab = tokPlusLab[0];
            String tok = tokPlusLab[1];
            if (lab.contains(field)) {

                boolean foundNewLabel = false;
                // Collect everything except a new tag.
                while (!foundNewLabel && i < aReferencePlusTokens.size()) {
                    // Get next token.
                    tokPlusLab = aReferencePlusTokens.get(i);
                    lab = tokPlusLab[0];
                    tok = tokPlusLab[1];

                    if (lab.startsWith("<" + field)) {
                        sb.append(tok.replace("&nbsp;", " "));
                        i++;
                    } else if (lab.startsWith("<&")) {
                        // Also good! but dont append to A++ output.
                        // punctuation for initials are from latex.
                        // they should not be included.
                        i++;
                    } else {
                        foundNewLabel = true;
                        sb.append("|");
                    }

                }

            }
        }
        return sb.toString().replace("&", "");
    }
}
