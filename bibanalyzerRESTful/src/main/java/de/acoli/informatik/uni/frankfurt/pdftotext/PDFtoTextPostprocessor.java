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
 * Description:
 *
 * Post-processes "corrupt" references which have not been properly separated by
 * the preprocessing step.
 *
 * Idea: Only process those references whose average reference length is larger
 * than what would be expected. (threshold).
 *
 * Current setting: only process first 15 references.
 *
 *
 *
 * @author niko
 */
public class PDFtoTextPostprocessor {

    public static final String INPUT_DIR = "/home/niko/Desktop/pdftoxml/test/smalltest/";
    private static final double LINE_LENGTH_UP_TO_WHICH_EVERYTHING_IS_ALRIGHT = 300;

    public static void main(String[] args) throws FileNotFoundException {

        postprocessReferences(INPUT_DIR);

    }

    public static void postprocessReferences(String inputDir) throws FileNotFoundException {

        // Detect the files which are "currupt" in the sense
        // that the references are not properly separated.
        ArrayList<File> nontoks = new ArrayList<File>();
        collectNonTokFiles(nontoks, inputDir);

        for (File f : nontoks) {
            //System.out.println("Analyzing file: " + f.getName());

            Scanner s = new Scanner(f);
            int cumLineLength = 0;
            int lineCount = 0;
            int upTo = 15;
            while (s.hasNextLine()) {
                if (upTo > lineCount) {
                    String aLine = s.nextLine();
                    if (aLine.length() > 0) {
                        cumLineLength += aLine.length();
                        lineCount++;
                    }
                } else {
                    break;
                }
            }

            double avgLineLength = (double) cumLineLength / lineCount;
            //System.out.println("Lines: " + lineCount);
            //System.out.println("cumLineLength: " + cumLineLength);
            //System.out.println("Avg line length: " + avgLineLength);
            //System.out.println();
            // Something wrong.
            // Reseparate lines.
            if (avgLineLength > LINE_LENGTH_UP_TO_WHICH_EVERYTHING_IS_ALRIGHT) {
                System.out.println("Reanalyzing file: " + f.getName());

                // Open the tab separated file.
                String tabFileName = f.getName().substring(0, f.getName().lastIndexOf("_") + 1) + "tabinfo.txt";
                //System.out.println(tabFileName);

                Scanner tabFileScan = new Scanner(new File(inputDir + tabFileName));
                ArrayList<String> lines = new ArrayList<>();
                while (tabFileScan.hasNextLine()) {
                    String aLine = tabFileScan.nextLine();
                    lines.add(aLine);
                }
                tabFileScan.close();
                s.close();

                // Make a first iteration over the lines to collect
                // statistics for new lines and reference boundaries.
                double currentY = 0.0;
                int numberDiffs = 0;
                double cumDiffs = 0.0;
                for (int i = 0; i < lines.size(); i++) {
                    String aLine = lines.get(i);
                    String[] items = aLine.split("\t");
                    if (items.length > 2) {
                        double yVal = Double.parseDouble(items[1]);
                        if (currentY != yVal) {
                            //System.out.println("New yVal: " + yVal);

                            // Only do this if we do not jump to a new page.
                            // Then counting the difference in yValues does
                            // not make sense if we compare the jump to the previous page.
                            if (yVal > currentY) {
                                double diff = Math.abs(yVal - currentY);
                                numberDiffs++;
                                cumDiffs += diff;
                            } else {
                                // 
                            }
                            currentY = yVal;
                        }
                        //System.out.println(yVal);
                    }

                }
                double averageDiff = (double) cumDiffs / numberDiffs;
                //System.out.println("Average diff: " + averageDiff);

                // Make the separation again.
                // Ignore the EndOfReference markers. They do not properly separate
                // new references.
                PrintWriter w = new PrintWriter(new File(inputDir + f.getName() + "_reanalyzed_.txt"));
                currentY = 0.0;
                for (int i = 0; i < lines.size(); i++) {

                    String aLine = lines.get(i);
                    // BUG !!!
                    if (aLine.length() > 15 && aLine.endsWith("EndOfReference")) {
                        aLine = aLine.substring(0, aLine.indexOf("EndOfReference")) + "-";
                    }

                    String[] items = aLine.split("\t");

                    if (items.length > 2) {

                        double yVal = Double.parseDouble(items[1]);

                        String token = "";
                        if (items.length == 8) {
                            token = items[7];
                        }
                        // Bug. Line has only seven fields. The last one is the token anyways.
                        // Probably this this has not "font" attribute.
                        if (items.length == 7) {
                            token = items[6];
                        }

                        if (yVal > currentY) {
                            double diff = Math.abs(yVal - currentY);
                            if (diff > averageDiff) {
                                // It's a new reference.
                                //System.out.println("new ref!");
                                w.write("\n");
                                currentY = yVal;
                            } else {
                                // Line belongs to the same reference.
                                currentY = yVal;
                            }
                        } else {
                            // Token is on the same line.
                            currentY = yVal;
                        }

                        //System.out.println(aLine);
                        w.write(token + " ");

                    } else {
                        // Do nothing.
                    }

                }
                w.flush();
                w.close();

            } else {
                // Nothing to reanalyze.
                // Just write its content to a new file with the "reanalyzed" filename.
                // This is just a copy of the old one.
                String nonTokFileName = f.getName().substring(0, f.getName().lastIndexOf("_") + 1) + "nontok.txt";
                ArrayList<String> lines = new ArrayList<String>();
                Scanner sc = new Scanner(new File(inputDir + nonTokFileName));
                while (sc.hasNextLine()) {
                    String aLine = sc.nextLine();
                    lines.add(aLine);
                }
                sc.close();
                PrintWriter w = new PrintWriter(new File(inputDir + f.getName() + "_reanalyzed_.txt"));
                for (int i = 0; i < lines.size(); i++) {
                    String aLine = lines.get(i);
                    w.write(aLine + "\n");
                }
                w.flush();
                w.close();
            }

        }
    }

    /**
     * Collect all (non-directory) files for a specific folder.
     *
     * @param fileList
     * @param path
     */
    public static void collectNonTokFiles(ArrayList<File> fileList, String path) {
        File root = new File(path);
        File[] list = root.listFiles();

        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                collectNonTokFiles(fileList, f.getAbsolutePath());
            } else {
                if (f.getName().endsWith("nontok.txt")) {
                    fileList.add(f);
                }
            }
        }
    }
}
