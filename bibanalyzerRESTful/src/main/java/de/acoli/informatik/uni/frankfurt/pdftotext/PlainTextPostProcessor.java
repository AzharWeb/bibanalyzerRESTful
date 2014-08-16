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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Description: Reads in the plain text reference files and tries to identify
 * "real" reference strings, i.e. we want to
 *
 * - remove page numbers. - remove "too short" references. - removes stuff AFTER
 * a bibliography.
 *
 * - heuristic guidelines: check numerical alphabetical ordering of references.
 *
 * @author niko
 */
public class PlainTextPostProcessor {

    public static final String INPUT_DIR = "pdf2text/xml";
    public static final String OUTPUT_DIR = "pdf2text/txt";

    public static void main(String[] args) throws FileNotFoundException {
        postProcessTextFiles(INPUT_DIR, OUTPUT_DIR);
    }

    public static void postProcessTextFiles(String anInputDir, String anOutputDir) throws FileNotFoundException {

        ArrayList<File> reanalyzedFiles = new ArrayList<File>();
        collectReanalyzedFiles(reanalyzedFiles, anInputDir);

        System.out.println("Collected: " + reanalyzedFiles.size() + " files.");

        for (File r : reanalyzedFiles) {

            boolean referencesAreNumberSeparated = false;
            boolean referencesAreAlphabeticallySeparated = false;

            int numberSeparatedCnt = 0;
            int alphaSeparatedCnt = 0;

            Scanner s = new Scanner(r);
            System.out.println("Analyzing " + r.getName());
            int checkedHowMany = 0;
            while (s.hasNextLine()) {
                String aLine = s.nextLine();
                if (aLine.length() > 0) {
                    // Get first item.
                    if (aLine.contains(" ")) {
                        String firstBeforeSpace = aLine.substring(0, aLine.indexOf(" "));
                        //System.out.println(firstBeforeSpace);

                        boolean isNumeric = isNumeric(firstBeforeSpace);
                        if (isNumeric) {
                            numberSeparatedCnt++;
                        } else {
                            if (firstBeforeSpace.startsWith("[")) {
                                //System.out.println("Careful... [");
                            }
                            alphaSeparatedCnt++;
                        }
                        //System.out.println(isNumeric);

                        checkedHowMany++;
                        if (checkedHowMany > 5) {
                            break;
                        }
                    }
                }
            }
            s.close();

            if (numberSeparatedCnt >= alphaSeparatedCnt) {
                referencesAreNumberSeparated = true;
            } else {
                referencesAreAlphabeticallySeparated = true;
            }

            Scanner sc = new Scanner(r);
            ArrayList<String> collectionOfLines = new ArrayList<>();
            int lineCnt = 0;
            boolean check = true;
            while (sc.hasNextLine()) {

                String aLine = sc.nextLine().trim();

                if (lineCnt > 150 || aLine.length() > 800 || aLine.startsWith("Figure")) {
                    System.out.println("breaking..");
                    break;
                }

                if (aLine.length() > 0) {
                    boolean containsSpace = aLine.contains(" ");
                    boolean startsWithNumericalWOdot = false;

                    if (containsSpace) {
                        String numeric = aLine.substring(0, aLine.indexOf(" "));
                        if (check && isNumeric(numeric) && !numeric.endsWith(".")) {
                            if (lineCnt == 0) {
                                // Set check to false.
                                // These references are separated by number without dots.
                                // 1 Author1 Title1
                                // 2 Author2 ...
                                check = false;
                            } else {
                                startsWithNumericalWOdot = true;
                            }
                        }
                    }
                    if (aLine.length() < 20 || startsWithNumericalWOdot) {
                        if (aLine.length() < 4) {
                            // reject. Probably page number.
                            // Maybe TODO: check if its only a page number.
                        } else {
                            if (aLine.length() > 60) {
                                // It's probably an own reference.
                                // remove the first token (page number) and add the rest as a new reference.
                                collectionOfLines.add(aLine.substring(aLine.indexOf(" ")).trim());

                            } else {
                                System.out.println(aLine);
                                String lastLineAdded = collectionOfLines.get(collectionOfLines.size() - 1);
                                lastLineAdded = lastLineAdded + " " + aLine;
                                collectionOfLines.set(collectionOfLines.size() - 1, lastLineAdded);
                                lineCnt++;
                            }
                        }
                    } else {
                        collectionOfLines.add(aLine);
                        lineCnt++;
                    }
                }
            }
            sc.close();

            PrintWriter w = new PrintWriter(new File(anOutputDir + "/" + r.getName()
                    .substring(0,
                            r.getName().indexOf("xmlformatted")) + "txt"));
            for (String l : collectionOfLines) {
                w.write(l + "\n");
            }

            w.flush();
            w.close();

        }
    }

    /**
     * Collect all (non-directory) files for a specific folder.
     *
     * @param fileList
     * @param path
     */
    public static void collectReanalyzedFiles(ArrayList<File> fileList, String path) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                collectReanalyzedFiles(fileList, f.getAbsolutePath());
            } else {
                if (f.getName().contains("reanalyzed")) {
                    fileList.add(f);
                }
            }
        }
    }

    public static boolean isNumeric(String str) {
        double d;
        try {
            d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
