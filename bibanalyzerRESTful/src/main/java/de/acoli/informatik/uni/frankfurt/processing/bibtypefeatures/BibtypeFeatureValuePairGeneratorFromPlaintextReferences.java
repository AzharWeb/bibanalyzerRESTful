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

package de.acoli.informatik.uni.frankfurt.processing.bibtypefeatures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.acoli.informatik.uni.frankfurt.processing.bibfieldfeatures.DictReader;

/**
 * Description:
 * 
 * A class with utility methods to train a bibtype classifier
 * and to use an already trained model for the classification of unknown
 * references.
 * 
 * The possible classes used in our analysis are:
 * 
 * BibArticle with label:       1
 * BibBook with label:          0
 * BibChapter with label:      -1
 * 
 *
 * @author niko
 */
public class BibtypeFeatureValuePairGeneratorFromPlaintextReferences {

    
    public static ArrayList<String> journalnames;
    public static ArrayList<String> pubnames;
    public static ArrayList<String> publocs;
    

    // Define three equal proportions of plaintext bibtypes.
    // BibArticles, BibBooks and BibChapters.
    // Note that the file names need to match(!):
    // "bibarticles.txt", "bibbooks.txt", "bibchapters.txt".
    public static final String PLAINTEXT_REFERENCES_A = "input/crf_training/SPRINGER_articles/2_bibarticle_TEST_raw.txt";
    public static final String PLAINTEXT_REFERENCES_B = "input/crf_training/SPRINGER_books/2_bibbook_TEST_raw.txt";
    public static final String PLAINTEXT_REFERENCES_C = "input/crf_training/SPRINGER_chapters/2_bibchapter_TEST_raw.txt";

    
    public static final ArrayList<String> bibtypes = new ArrayList<String>();
        
    
    
    // article 1
    // book 0
    // chapter -1
    static {
        //System.out.println("Reading in...");
        try {
            //System.out.println("journal titles...");
            journalnames = DictReader.getDBLPJournalTitles();
            //System.out.println("publisher names...");
            pubnames = DictReader.getDBLPPublisherNames();
            //System.out.println("publisher locations...");
            publocs = DictReader.getSpringerPublisherLocations();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BibtypeFeatureValuePairGeneratorFromPlaintextReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    
       

    /**
     * Demo used to illustrate how to train a bibtype classifier 
     * for a given set of plaintext reference strings.
     * 
     * 
     * @param args
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException {

        //System.out.println("Generating training data from plaintext references...");
        // Procedure to train own classifier:
        
        // 1. Get plaintext refs file used for training.
        bibtypes.add(PLAINTEXT_REFERENCES_A);
        bibtypes.add(PLAINTEXT_REFERENCES_B);
        bibtypes.add(PLAINTEXT_REFERENCES_C);
        
        // 2. Write feature vectors for three plaintext reference files 
        // to single file.
        String featureFile = "input/maxent_training/bibtypefeaturevectors";
        convertPlaintextReferencesToFeatureVectorsForTraining(featureFile);
        // Shuffle items before training.
        Shuffle.shuffle(featureFile);
        
        
        // 3. Train classifier on this training data.
        // a) convert svm light training format to binary Mallet format.
        // ./mallet-2.0.7/bin/mallet import-svmlight --input input/maxent_training/bibtypefeaturevectors_shuf --output input/maxent_training/train.mallet
        
        // b) Train MaxEnt classifier from this training data.
        // ./mallet-2.0.7/bin/mallet train-classifier --trainer MaxEnt --input input/maxent_training/train.mallet --output-classifier input/maxent_training/my.MEclassifier
        
        // c) Alternatively, train Naive Bayes classifier from this training data.
        // ./mallet-2.0.7/bin/mallet train-classifier --input input/maxent_training/train.mallet --output-classifier input/maxent_training/my.NBclassifier
        
        // 4. Run (test) classifier on a raw (svm-light) feature vectors file.
        // Note that we use the training data for the sake of illustration.
        // ./mallet-2.0.7/bin/mallet classify-file --input input/maxent_training/bibtypefeaturevectors --output - --classifier input/maxent_training/my.MEclassifier > input/maxent_training/predicted
        
        
        // 5. Write unique predictions to final file (and probabilities for classification experiments).
        // These two files have just been generated.
        String featVecFile = "input/maxent_training/bibtypefeaturevectors";
        String classifiedOutput = "input/maxent_training/predicted";
        // This generates the final result.
        BibtypeClassifierOutputReader.getPredictions(featVecFile, classifiedOutput, "input/maxent_training/predicted_final");
        
        
    }

    
    /**
     * Converts plaintext reference strings to feature vectors which
     * are relevant for bibtype classification.
     * 
     * @param plaintextReferences, path to plaintext references.
     * @param featVecOutputFile, path to output file containing the feature vectors.
     * @throws FileNotFoundException 
     */
    public static void convertPlaintextReferencesToFeatureVectors(ArrayList<String> plaintextReferences, String featVecOutputFile) throws FileNotFoundException {

        PrintWriter w = new PrintWriter(new File(featVecOutputFile));

        // Not needed later.
        // Dummy label.
        String label = "8";

        int lineCnt = 0;

        for (int i = 0; i < plaintextReferences.size(); i++) {
            String aRef = plaintextReferences.get(i);
            if (aRef.length() > 0) {
                doComputeFeatureVector(aRef, label, w);
                lineCnt++;
            }
        }
        System.out.println("Wrote " + lineCnt + " feature vectors to output file.");

        w.flush();
        w.close();

    }

    /**
     * 
     * @param fileToFeaturerefsRefs
     * @throws FileNotFoundException 
     */
    private static void convertPlaintextReferencesToFeatureVectorsForTraining(String fileToFeaturerefsRefs) throws FileNotFoundException {

        PrintWriter w = new PrintWriter(new File(fileToFeaturerefsRefs));

        String label = "";
        for (String aplaintextrefs : bibtypes) {

            if (aplaintextrefs.contains("article")) {
                label = "1";
            }
            if (aplaintextrefs.contains("book")) {
                label = "0";
            }
            if (aplaintextrefs.contains("chapter")) {
                label = "-1";
            }

            Scanner sA = new Scanner(new File(aplaintextrefs), "UTF-8");
            int lineCnt = 1;
            while (sA.hasNextLine()) {
                String aLine = sA.nextLine().trim();

                doComputeFeatureVector(aLine, label, w);

                lineCnt++;
            }
            sA.close();
        }
        w.flush();
        w.close();

    }

    private static int contains(String aLine, String what) {
        if (aLine.contains(what)) {
            return 1;
        } else {
            return 0;
        }

    }

    /**
     * Computes a feature vector for a given plaintext reference.
     * Individual features are described below.
     * @param aLine, the plaintext reference.
     * @param label
     * @param w 
     */
    private static void doComputeFeatureVector(String aLine, String label, PrintWriter w) {
        Pattern digitPattern = Pattern.compile("(\\D?\\d+\\D?)");
        Matcher digitMatcher = digitPattern.matcher(aLine);
        //System.out.println(aLine);
        int numDigits = 0;
        while (digitMatcher.find()) {
            //System.out.println(">" + digitMatcher.group()  + "<");
            numDigits++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(label + " ");
        sb.append("1:");
        //System.out.print(label + " ");
        //System.out.print("1:");
        double length = (double) aLine.length() / (double) 100;
        //System.out.print(length); // Length of the reference in characters.
        //System.out.print(" 2:");
        sb.append(length); // Length of the reference in characters.
        sb.append(" 2:");

        String[] split = aLine.split("\\s");
        //System.out.print(split.length); // Length of the reference split by
        // whitespace. (number of tokens.)

        //System.out.print(" 3:");
        sb.append(split.length);
        sb.append(" 3:");

        // Number of distinct digits.
        // Idea: Books have less numbers than articles, for example.
        //System.out.print(numDigits);
        //System.out.print(" 4:");
        sb.append(numDigits);
        sb.append(" 4:");

        Pattern twoDigits = Pattern.compile("(\\d+:\\d+)");
        Matcher digitMatcher2 = twoDigits.matcher(aLine);
        int foundDigitColonDigit = 0;
        while (digitMatcher2.find()) {
            //System.out.println(">" + digitMatcher.group()  + "<");
            foundDigitColonDigit = 1;
        }

        //System.out.print(foundDigitColonDigit); // 223:34
        //System.out.print(" 5:");
        sb.append(foundDigitColonDigit);
        sb.append(" 5:");

        Pattern bracketDigitBracket = Pattern.compile("(\\(\\d{1,3}\\))");
        Matcher digitMatcher3 = bracketDigitBracket.matcher(aLine);
        int foundBracketDigitBracket = 0;
        while (digitMatcher3.find()) {
            foundBracketDigitBracket = 1;
        }
        //System.out.print(foundBracketDigitBracket); // (3)
        sb.append(foundBracketDigitBracket);

        int numLowerCaseWords = 0;
        for (int i = 0; i < split.length; i++) {
            if (split[i].length() > 3) {
                if (Character.isLowerCase(split[i].charAt(0))) {
                    //System.out.println(split[i]);
                    numLowerCaseWords++;
                }
            }
        }
        //System.out.print(" 6:");
        sb.append(" 6:");
        // Number of words starting with lower-case letters.
        // Idea: article titles consists of many of them.
        //System.out.println(">>" + numLowerCaseWords + "<<<>>>" + split.length + "<<<");
        double ratio = (double) numLowerCaseWords / (double) split.length * 10;
        DecimalFormat df = new DecimalFormat("#.###");
        //System.out.print(df.format(ratio));
        //System.out.print(ratio); // ratio of lower case words compared to all tokens.
        sb.append(df.format(ratio));

        //System.out.print(" 7:");
        //System.out.print(contains(aLine, ";"));  // contains colon.
        sb.append(" 7:");
        sb.append(contains(aLine, ";"));

        //System.out.print(" 8:");
        //System.out.print(contains(aLine, "vol"));
        sb.append(" 8:");
        sb.append(contains(aLine, "vol")); // contains "vol"

        //System.out.print(" 9:");
        //System.out.print(contains(aLine, "vol."));
        sb.append(" 9:");
        sb.append(contains(aLine, "vol."));

        //System.out.print(" 10:");
        //System.out.print(contains(aLine, "no"));
        sb.append(" 10:");
        sb.append(contains(aLine, "no"));

        //System.out.print(" 11:");
        //System.out.print(contains(aLine, "no."));
        sb.append(" 11:");
        sb.append(contains(aLine, "no."));

        //System.out.print(" 12:");
        //System.out.print(contains(aLine, " J "));
        sb.append(" 12:");
        sb.append(contains(aLine, " J "));

        //System.out.print(" 13:");
        //System.out.print(contains(aLine, " J. "));
        sb.append(" 13:");
        sb.append(contains(aLine, " J. "));

        //System.out.print(" 14:");
        //System.out.print(contains(aLine, " In "));
        sb.append(" 14:");
        sb.append(contains(aLine, " In "));

        //System.out.print(" 15:");
        //System.out.print(contains(aLine, " In: "));
        sb.append(" 15:");
        sb.append(contains(aLine, " In: "));

        //System.out.print(" 16:");
        //System.out.print(contains(aLine, " in: "));
        sb.append(" 16:");
        sb.append(contains(aLine, " in: "));

        //System.out.print(" 17:");
        //System.out.print(contains(aLine, "ed"));
        sb.append(" 17:");
        sb.append(contains(aLine, "ed"));

        //System.out.print(" 18:");
        //System.out.print(contains(aLine, "ed."));
        sb.append(" 18:");
        sb.append(contains(aLine, "ed."));

        //System.out.print(" 19:");
        //System.out.print(contains(aLine, "Ed."));
        sb.append(" 19:");
        sb.append(contains(aLine, "Ed."));

        //System.out.print(" 20:");
        //System.out.print(contains(aLine, "eds"));
        sb.append(" 20:");
        sb.append(contains(aLine, "eds"));

        //System.out.print(" 21:");
        //System.out.print(contains(aLine, "eds."));
        sb.append(" 21:");
        sb.append(contains(aLine, "eds."));

        //System.out.print(" 22:");
        //System.out.print(contains(aLine, "edited"));
        sb.append(" 22:");
        sb.append(contains(aLine, "edited"));

        //System.out.print(" 23:");
        //System.out.print(contains(aLine, "Proc"));
        sb.append(" 23:");
        sb.append(contains(aLine, "Proc"));

        //System.out.print(" 24:");
        //System.out.print(contains(aLine, "Proceedings"));
        sb.append(" 24:");
        sb.append(contains(aLine, "Proceedings"));

        //System.out.print(" 25:");
        //System.out.print(contains(aLine, "Inproceedings"));
        sb.append(" 25:");
        sb.append(contains(aLine, "Inproceedings"));
        
        
        // Feature: contains a journal title.
        sb.append(" 26:");
        sb.append(containedInList(aLine, journalnames));
        
        // Feature: contains a publisher name.
        sb.append(" 27:");
        sb.append(containedInList(aLine, pubnames));
        
        // Feature: contains a publisher location.
        sb.append(" 28:");
        sb.append(containedInList(aLine, publocs));
        
        //System.out.print(" # " + aLine);
        sb.append(" # " + aLine);

        w.write(sb.toString() + "\n");

        //System.out.println();
        
        
        // TODO:
        // Add more features here if necessary!
        

    }
    
    
    /**
     * Checks if an item is part of a dictionary (1) or not (0).
     * @param aRef
     * @param aList
     * @return 
     */
    private static String containedInList(String aRef, ArrayList<String> aList) {
        for(String anEntry : aList) {
            if(aRef.contains(anEntry)) {
                return "1";
            }
            else {
                //return "0";
            }
        }
        return "0";
    }
}
