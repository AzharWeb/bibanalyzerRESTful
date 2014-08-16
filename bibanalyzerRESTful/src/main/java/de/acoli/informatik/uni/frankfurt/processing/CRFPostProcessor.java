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

package de.acoli.informatik.uni.frankfurt.processing;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Description:
 * This program is supposed to fix CRF errors in a postprocessing step.
 * 
 * This program is completely rule-based.
 * The underlying idea is that, e.g.,
 *
 * Something like "Hans-Müller" should NEVER be tagged as "pages".
 * even though the hyphen makes the CRF believe that we have a "firstpage-lastpage" pattern.
 * 
 * A pattern of the form "vol." followed by a number, should never be tagged as "dummy"
 * but rather "number" instead.
 * 
 * A dummy page before "pages" should be a VolumeID (BibArticles).
 * 
 * Moreover, something like "Springer" is always a publisher. 
 * Idea: Read in a static dictionary of publisher names and overwrite misclassified tokens.
 * 
 * TODO:
 * - "author" tags which are either followed or proceeded by Hrsg. editor, ed.
 * eds, etc should be replaced by "editors" (HTML pink) "editors" instead of "authors".
 *
 * Add tags which definitely ARE confevent names and stuff like that which are
 * too rare to serve as features for the model.
 *
 * TODO:
 * Match all tokens from a list of series titles.
 * (We can be sure that we have high precision with these types of dictionaries.)
 * 
 * TODO:
 * Numbers (digits) in general must not be "dummy".
 * 
 * A string must never be tagged as "year".
 * 
 * TODO:
 * Patterns of the form "Title" In: "Title" should be "ChapterTitle" In: "BookTitle".
 * "ChapterTitle" In: "ChapterTitle" is not plausible.
 * 
 *
 *
 * @author niko
 */
public class CRFPostProcessor {

    public static final String INPUT_CRF_FILE = "/home/niko/Desktop/tagged_combined.txt";

    public static void main(String[] args) throws FileNotFoundException {

        ArrayList<ArrayList<String[]>> sentences = CRFOutputReader.getPredictedTokensAndTagsForReferences(INPUT_CRF_FILE, true);
        ArrayList<String> bibtypePredictions = new ArrayList<>();
        // Careful: This demo client assumes that we only have "BibArticles".
        for (int i = 0; i < sentences.size(); i++) {
            bibtypePredictions.add("1");
        }
        postProcessCRFOutput(sentences, bibtypePredictions);

    }

    /**
     *
     * @param sentences
     * @param bibtypePredictions (A vector containing three String indices 1, 0,
     * -1 for each of the bibtypes (BibArticle, BibBook, BibChapter).
     * @return
     * @throws FileNotFoundException
     */
    public static ArrayList<ArrayList<String[]>> postProcessCRFOutput(ArrayList<ArrayList<String[]>> sentences, ArrayList<String> bibtypePredictions) throws FileNotFoundException {

        System.out.println("Postprocessing output.");

        // Make one pass and substitute everything.
        for (int s = 0; s < sentences.size(); s++) {
            ArrayList<String[]> sentencetuples = sentences.get(s);
            String currentBibtype = bibtypePredictions.get(s);
            //System.out.println(currentBibtype);

            switch (currentBibtype) {
                // BibArticle.
                case "1":
                    // A digit must not be a "<FamilyName>"
                    for (int i = 0; i < sentencetuples.size(); i++) {
                        String[] tuple = sentencetuples.get(i);
                        String label = tuple[0];
                        String token = tuple[1];
                        //System.out.println("label: " + label + "\t\ttoken:" + token);
                        if (isDigit(token) && label.equals("<FamilyName>")) {
                            //System.out.println("tok: "+ token);
                            // Check if we find the word "volume" "vol. "vol" "Vol"
                            // up to four tokens ahead.
                            for (int j = i - 1; j > i - 4; j--) {
                                if (j > 0) {
                                    String[] aPreviousTuple = sentencetuples.get(j);
                                    String aPreviousLabel = aPreviousTuple[0];
                                    if (aPreviousLabel.equals("<VolumeID>")) {
                                        tuple[0] = "<FirstPage>";
                                        sentencetuples.set(i, tuple);
                                    }
                                }
                            }
                        }
                    }

                    break;
                // BibBook.
                case "0":
                    // TODO: Author -> Editor.
                    break;
                // BibChapter.
                case "-1":
                    for (int i = 0; i < sentencetuples.size(); i++) {
                        String[] tuple = sentencetuples.get(i);
                        String label = tuple[0];
                        String token = tuple[1];
                        //System.out.println("label: " + label + "\t\ttoken:" + token);
                        if (isDigit(token) && label.equals("<dummy>")) {
                            //System.out.println("tok: "+ token);
                            // Check if we find the word "volume" "vol. "vol" "Vol"
                            // up to four tokens ahead.
                            for (int j = i - 1; j > i - 4; j--) {
                                if (j > 0) {
                                    String[] aPreviousTuple = sentencetuples.get(j);
                                    String aPreviousToken = aPreviousTuple[1];
                                    //System.out.println("prev tok: " + aPreviousToken);
                                    if (aPreviousToken.equalsIgnoreCase("vol")
                                            || aPreviousToken.equalsIgnoreCase("volume")) {
                                        tuple[0] = "<NumberInSeries>";
                                        sentencetuples.set(i, tuple);
                                    }
                                }
                            }
                        }
                    }
                    // Postprocess data 
                    // Read in Series Titles from data base and add exact matches to CRF
                    // output.
                    // E.g., Lecture Notes in Mathematics
                    //

                    break;
                default:
                    break;
            }

        }

        //System.out.println("\n\n\n\n");
        
        for (ArrayList<String[]> sentencetuples : sentences) {
            for (int i = 0; i < sentencetuples.size(); i++) {
                String[] tuple = sentencetuples.get(i);
                String label = tuple[0];
                String token = tuple[1];
                //System.out.println("label: " + label + "\t\ttoken:" + token);
            }
            //System.out.println();
        }

        return sentences;

    }

    public static boolean isDigit(String str) {
        try {
            int i = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
