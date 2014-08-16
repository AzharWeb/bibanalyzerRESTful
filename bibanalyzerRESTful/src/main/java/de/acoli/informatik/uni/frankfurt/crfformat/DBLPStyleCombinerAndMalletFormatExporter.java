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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:
 *
 * Takes the DBLP data dump consisting of "gold" annotated DBLP references and
 * exports a subset of the data.
 *
 * Parameters: - the bibtype (only exports data of a certain bibtype). - the
 * bibstyle - the number of references for training. - the number of references
 * for testing.
 *
 *
 *
 * @author niko
 *
 */
public class DBLPStyleCombinerAndMalletFormatExporter {

    // Data directory with the augmented XML files.
    // That's where the augmented XML files are located.
    // The "training data".
    public static final String DIR = "input/dumps/DBLP_dump/raw/";
    public static final String EXPORT_TO = "input/dumps/DBLP_dump/export/bibchapters/onlytokens/";

    /**
     * Possible parameters: 
     * article 
     * book-proceedings 
     * incollection-inproceedings
     *
     */
    public static final String BIBTYPE = "incollection-inproceedings";
    public static final String BIBSTYLE = "all";

    public static final int EXPORT_HOW_MANY_TRAIN = 2000;
    public static final int EXPORT_HOW_MANY_TEST = 200;

    public static final String inputpath = DIR + BIBTYPE + "/";

    public static boolean shuffle = true;
    public static final boolean debug = false; // write raw reference to file.

    // Where the xml data is located for one bibtype including all styles.
    public static final String PATH_TO_STYLES = inputpath + "/";

    public static void main(String[] args) throws FileNotFoundException {
        // Read in all style.
        ArrayList<File> files = new ArrayList<File>();
        collectFiles(files, PATH_TO_STYLES, BIBSTYLE);

        int allRefsCount = 0;
        // Careful. This data strucutre accepts NO duplicates !!!
        Set<String> references = new LinkedHashSet<String>(10000);
        for (File f : files) {
            if (f.getName().endsWith(".txt")) {
                System.out.println("Analyzing file: " + f.getName());
                Scanner s = new Scanner(f);
                while (s.hasNextLine()) {
                    String aLine = s.nextLine();
                    aLine = convertToSpringerTags(aLine);

                    allRefsCount++;
                    if (getCondition(aLine)) {
                        //System.out.println(aLine);

                        aLine = randomlyRemoveUrlsFromRef(aLine);
                        aLine = randomlyRemoveUrlsTag(aLine);

                        references.add(aLine);
                    }
                }
                s.close();
            }
        }

        System.out.println();
        System.out.println("num (unique) referenences loaded: " + references.size() + " all refs: " + allRefsCount);
        List<String> list = new ArrayList<String>(references);
        if (shuffle) {
            Collections.shuffle(list);
        }

        // Export shuffled references including all styles.
        // TRAIN !!!
        PrintWriter w = new PrintWriter(
                new File(EXPORT_TO + EXPORT_HOW_MANY_TRAIN + "_" + BIBTYPE + "_TRAIN" + ".txt"));
        PrintWriter w_raw = new PrintWriter(
                new File(EXPORT_TO + EXPORT_HOW_MANY_TRAIN + "_" + BIBTYPE + "_TRAIN_raw" + ".txt"));
        PrintWriter w_xmlaug = new PrintWriter(
                new File(EXPORT_TO + EXPORT_HOW_MANY_TRAIN + "_" + BIBTYPE + "_TRAIN_xmlaug" + ".txt"));

        int counterTrain = 0;
        int refIdx = 0;
        // Train.
        while (true) {

            String aRef = list.get(refIdx);
            aRef = replaceGeneralThings(aRef);

            if (counterTrain >= EXPORT_HOW_MANY_TRAIN) {
                break;
            }

            if (debug) {
                w.write(aRef.replaceAll("<[^>]+>", "") + "\n" + aRef + "\n");
            }
            boolean conditionMet = getCondition(aRef);

            if (conditionMet) {

                PlaintextReferenceStringToMalletCRFFormatConverter.convertReferenceString(aRef, w);
                counterTrain++;

                w_raw.write(aRef.replaceAll("<[^>]+>", "") + "\n");
                w_xmlaug.write(aRef + "\n");

            }
            refIdx++;
        }
        w.close();
        w_raw.close();
        w_xmlaug.close();

        // Export shuffled references including all styles.
        // TEST !!!
        PrintWriter w2 = new PrintWriter(
                new File(EXPORT_TO + EXPORT_HOW_MANY_TEST + "_" + BIBTYPE + "_TEST" + ".txt"));
        PrintWriter w2_raw = new PrintWriter(
                new File(EXPORT_TO + EXPORT_HOW_MANY_TEST + "_" + BIBTYPE + "_TEST_raw" + ".txt"));
        PrintWriter w2_xmlaug = new PrintWriter(
                new File(EXPORT_TO + EXPORT_HOW_MANY_TEST + "_" + BIBTYPE + "_TEST_xmlaug" + ".txt"));

        int counterTest = 0;
        // Test.
        while (true) {

            String aRef = list.get(refIdx);
            aRef = replaceGeneralThings(aRef);

            if (counterTest >= EXPORT_HOW_MANY_TEST) {
                break;
            }

            if (debug) {
                w2.write(aRef.replaceAll("<[^>]+>", "") + "\n" + aRef + "\n");
            }
            boolean conditionMet = getCondition(aRef);

            if (conditionMet) {
                PlaintextReferenceStringToMalletCRFFormatConverter.convertReferenceString(aRef, w2);
                counterTest++;

                w2_raw.write(aRef.replaceAll("<[^>]+>", "") + "\n");
                w2_xmlaug.write(aRef + "\n");
            }
            refIdx++;
        }
        w2.close();
        w2_raw.close();
        w2_xmlaug.close();

    }

    public static void collectFiles(ArrayList<File> fileList, String path, String bibstyle) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                collectFiles(fileList, f.getAbsolutePath(), bibstyle);
            } else {
                if (bibstyle.equals("all")) {
                    fileList.add(f);
                } else if (f.getName().contains(bibstyle)) {
                    fileList.add(f);
                } else {
                    // Do nothing.
                }
            }
        }
    }

    // RANDOMMASSIG URL UND ISBN WEGLASSEN !!!
    // URL <Url>db/journals/cl/cl22.html#Drew96</Url>
    // [Online]. Available: <Url>db/journals/ecs/ecs29.html#KambojS07</Url>
    // Only export those which have a title and an author and a year
    private static boolean getCondition(String aRef) {

        // Remove dummy DBLP authors.
        if (aRef.startsWith("00")) {
            return false;
        }

        // Reject those which are not properly annotated!!!
        // For example:
        // F. Castaños, R. Ortega, A. van der Schaft, and A. Astolfi. Asymptotic stabilization via control by interconnection of port-<title>Hamiltonian systems.</title> Automatica, 45(7):1611-1618, <volume>2</volume>009.
        // Throw away those which are not properly annotated.
        // E.g., match: ...</IssueID></VolumeID>
        Pattern p = Pattern.compile("(</[^>]+></[^>]+>)");
        Matcher m = p.matcher(aRef);
        while (m.find()) {
            //String match = m.group();
            //System.out.println("aRef: " + aRef);
            //System.out.println("match: " + match);
            return false;
        }

        // Reject those which have too many untagged stuff in between.
        String tagsReplaced = aRef.replaceAll("<[^>]+>", "");
        //System.out.println(tagsReplaced);
        Pattern pattern = Pattern.compile("(<[^>]+>)(.+?)(<)");
        Matcher matcher = pattern.matcher(aRef);
        //System.out.println("aRef: " + aRef + "<");
        int cumLengthMatchedSpans = 0;
        while (matcher.find()) {
            String match = matcher.group(2);
            //System.out.println(match);
            cumLengthMatchedSpans += match.length();
        }
        //System.out.println("Lengths: " + tagsReplaced.length()  + " vs. " + cumLengthMatchedSpans);
        double ratio = (double) cumLengthMatchedSpans / (double) tagsReplaced.length();
        //System.out.println(ratio);
        //System.out.println();
        if (ratio < 0.7) {
            return false;
        }

        return true;

    }

    private static String convertToSpringerTags(String aLine) {

        // General stuff.
        aLine = aLine.replace("<title><booktitle>", "<title>");
        aLine = aLine.replace("</booktitle></title>", "</title>");

        aLine = aLine.replace("<booktitle><title>", "<booktitle>");
        aLine = aLine.replace("</title></booktitle>", "</booktitle>");

        aLine = aLine.replace("<author-firstname>", "<FirstName>");
        aLine = aLine.replace("</author-firstname>", "<FirstName>");

        aLine = aLine.replace("<author-lastname>", "<FamilyName>");
        aLine = aLine.replace("</author-lastname>", "<FamilyName>");

        aLine = aLine.replace("<author-fullname>", "<FamilyName>");
        aLine = aLine.replace("</author-fullname>", "<FamilyName>");

        aLine = aLine.replace("<editor-firstname>", "<FirstName>");
        aLine = aLine.replace("</editor-firstname>", "<FirstName>");

        aLine = aLine.replace("<editor-lastname>", "<FamilyName>");
        aLine = aLine.replace("</editor-lastname>", "<FamilyName>");

        aLine = aLine.replace("<editor-fullname>", "<FamilyName>");
        aLine = aLine.replace("</editor-fullname>", "<FamilyName>");

        // BibArticle.
        if (BIBTYPE.equals("article")) {
            aLine = aLine.replace("<title>", "<ArticleTitle>");
            aLine = aLine.replace("</title>", "</ArticleTitle>");
        }

        // BibBook.
        if (BIBTYPE.equals("book-proceedings")) {
            aLine = aLine.replace("<title>", "<BookTitle>");
            aLine = aLine.replace("</title>", "</BookTitle>");
        }

        // BibChapter.
        if (BIBTYPE.equals("incollection-inproceedings")) {
            aLine = aLine.replace("<title>", "<ChapterTitle>");
            aLine = aLine.replace("</title>", "</ChapterTitle>");

            aLine = aLine.replace("<booktitle>", "<BookTitle>");
            aLine = aLine.replace("</booktitle>", "</BookTitle>");
        }

        aLine = aLine.replace("<publisher>", "<PublisherName>");
        aLine = aLine.replace("</publisher>", "</PublisherName>");

        aLine = aLine.replace("<series>", "<SeriesTitle>");
        aLine = aLine.replace("</series>", "</SeriesTitle>");

        aLine = aLine.replace("<series>", "<SeriesTitle>");
        aLine = aLine.replace("</series>", "</SeriesTitle>");

        aLine = aLine.replace("<journal>", "<JournalTitle>");
        aLine = aLine.replace("</journal>", "</JournalTitle>");

        aLine = aLine.replace("<year>", "<Year>");
        aLine = aLine.replace("</year>", "</Year>");

        aLine = aLine.replace("<volume>", "<VolumeID>");
        aLine = aLine.replace("</volume>", "</VolumeID>");

        aLine = aLine.replace("<number>", "<IssueID>");
        aLine = aLine.replace("</number>", "</IssueID>");

        aLine = aLine.replace("<pages>", "<Pages>");
        aLine = aLine.replace("</pages>", "</Pages>");

        aLine = aLine.replace("<url>", "<Url>");
        aLine = aLine.replace("</url>", "</Url>");

        aLine = aLine.replace("<isbn>", "<Isbn>");
        aLine = aLine.replace("</isbn>", "</Isbn>");

        aLine = aLine.replace("<note>", "<BibComments>");
        aLine = aLine.replace("</note>", "</BibComments>");

        return aLine;

    }

    private static String replaceGeneralThings(String aLine) {

        // Fix annotations for this thing.
        // (9), 523
        Pattern pDigit = Pattern.compile("\\((\\d+)\\), (\\d+)");
        Matcher mDigit = pDigit.matcher(aLine);
        while (mDigit.find()) {
            String match = mDigit.group(0);
            //System.out.println("aRef: " + aLine);
            //System.out.println("group 0: " + match);
            String annotated = "(<IssueID>" + mDigit.group(1) + "</IssueID>), <Pages>" + mDigit.group(2) + "</Pages>";
            aLine = aLine.replace(match, annotated);
            //System.out.println("-> " + aLine);
        }

        // Fix annotations for missing (first) pages.
        // // Remove references with non-annotated numbers.
        // ), 15 (
        Pattern pDigit2 = Pattern.compile("\\), (\\d+) \\(");
        Matcher mDigit2 = pDigit2.matcher(aLine);
        while (mDigit2.find()) {
            String match = mDigit2.group(1);
            //System.out.println("aRef: " + aLine);
            //System.out.println("group 0: " + match);
            String annotated = "<Pages>" + match + "</Pages>";
            aLine = aLine.replace(match, annotated);
            //System.out.println("-> " + aLine);
        }

        boolean replaceWWW = randInt();
        if (replaceWWW) {
            boolean replaceHttp = randInt();
            if (replaceHttp) {
                aLine = aLine.replace("db/journals/", "http://www.");
            } else {
                aLine = aLine.replace("db/journals/", "www.");
            }
        }

        // Move period within "Initials" span and make it part of it.
        aLine = aLine.replace("</Initials>.", ".</Initials>");

        aLine = aLine.replace("´ı", "í");

        return aLine.replace("<FirstName>", "<Initials>").replace("</FirstName>", "</Initials>")
                .replace("<Superscript>", "<Superscript>").replace("</Superscript>", "");
    }

    private static String randomlyRemoveUrlsTag(String aLine) {
        Pattern urlP = Pattern.compile(" URL ");
        Matcher urlMatch = urlP.matcher(aLine);
        while (urlMatch.find()) {
            String match = urlMatch.group();
            //System.out.println(match);
            boolean rejectUrl = randInt();
            if (rejectUrl) {
                aLine = aLine.replace(match, " ").trim();
            }
        }
                //, URL db/journals/iam/iam7.html#EdmundsonJ84

        return aLine;
    }

    private static String randomlyRemoveUrlsFromRef(String aLine) {

        Pattern urlP = Pattern.compile("(.|,) URL .+$");
        Matcher urlMatch = urlP.matcher(aLine);
        while (urlMatch.find()) {
            String match = urlMatch.group();
            //System.out.println(match);
            boolean rejectUrl = randInt();
            if (rejectUrl) {
                aLine = aLine.replace(match, "").trim();
            }
        }
                //, URL db/journals/iam/iam7.html#EdmundsonJ84

        return aLine;
    }

    
    private static boolean randInt() {

        // Usually this should be a field rather than a method variable so
        // that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((1 - 0) + 1) + 0;

        //System.out.println(randomNum);
        if (randomNum > 0.5) {
            return true;
        } else {
            return false;
        }

    }

}
