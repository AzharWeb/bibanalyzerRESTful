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

package de.acoli.informatik.uni.frankfurt.processing.bibfieldfeatures;

import java.io.FileNotFoundException;

/**
 * Description:
 *
 * This class provided methods to annotate a plaintext tokenized CRF format
 * equipping it with additional features from dictionaries, e.g.
 *
 * A potential pipeline might add 1. year features 2. initial features. 3. DBLP
 * lexicon words. 4. Springer Publisher names.
 *
 *
 * @author niko
 */
public class TokenizedPlaintextFeatureAnnotation {

    /**
     * A simple demo client which adds features to plaintext tokenized training
     * data.
     *
     * 1. years 2. initials 3. DBLP dict words 4. publisher names (springer)
     *
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException {

        String inputFile = "11000_bibchapter_TRAIN.txtwithoutFont.txt";
        String pathToTokenized = "input/dumps/SPRINGER_dump/export/11000/bibchapters/" + inputFile;
        String articleOutputFolder = "input/dumps/SPRINGER_dump/export/11000/bibchapters/";

        // Annotate article features.
        
        // YEAR tagging.
        String outputFile_1 = articleOutputFolder + inputFile + "_year";
        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);
        WhitespaceCleaner.cleanWhitespace(outputFile_1);
        
        // Initials tagging.
        String outputFile_2 = articleOutputFolder + inputFile + "_year_initials";
        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);
        WhitespaceCleaner.cleanWhitespace(outputFile_2);
        
        
        // DICT tagging.
        String outputFile_3 = articleOutputFolder + inputFile + "_year_initials_dict";
        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");
        WhitespaceCleaner.cleanWhitespace(outputFile_3);
        
        
        // Springer PUBLISHER NAME tagging.
        // * FOR TRAINING FILES (which have a label) SET PARAMETER TO TRUE !!! *
        String outputFile_4 = articleOutputFolder + inputFile + "_year_initials_dict_pubnamesspringer";
        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.PUBNAMES_SPRINGER, "", true);
        WhitespaceCleaner.cleanWhitespace(outputFile_4);
        
//        String inputFile = "200_incollection-inproceedings_TEST.txt";
//        String pathToTokenized = "input/dumps/DBLP_dump/export/bibchapters/onlytokens/" + inputFile;
//        String articleOutputFolder = "input/dumps/DBLP_dump/export/bibchapters/features/";
//
//        // Annotate article features.
//        // YEAR tagging.
//        String outputFile_1 = articleOutputFolder + inputFile + "_year";
//        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);
//        WhitespaceCleaner.cleanWhitespace(outputFile_1);
//
//        // Initials tagging.
//        String outputFile_2 = articleOutputFolder + inputFile + "_year_initials";
//        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);
//        WhitespaceCleaner.cleanWhitespace(outputFile_2);
//
//        // DICT tagging.
//        String outputFile_3 = articleOutputFolder + inputFile + "_year_initials_dict";
//        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");
//        WhitespaceCleaner.cleanWhitespace(outputFile_3);
//
//        // DBLP PUBLISHER NAME tagging.
//        // * FOR TRAINING FILES (which have a label) SET PARAMETER TO TRUE !!! *
//        String outputFile_4 = articleOutputFolder + inputFile + "_year_initials_dict_pubnamedblp";
//        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.PUBNAMES_DBLP, "", true);
//        WhitespaceCleaner.cleanWhitespace(outputFile_4);

    }

    /**
     * Add features to Springer article data. Note that the last file in the
     * pipeline needs to contain the word "final".
     *
     * @param pathToTokenized
     * @throws FileNotFoundException
     */
    public static void augmentTokenizedSpringerArticles(String pathToTokenized) throws FileNotFoundException {
        // Annotate article features.
        String bibtypeToBeAugmented = "feature_augmented_SPRINGER/BibArticle_FeatureAnnotationPipeline/";
        String articleOutputFolder = pathToTokenized.substring(0, pathToTokenized.lastIndexOf("/") + 1) + bibtypeToBeAugmented;

        // YEAR tagging.
        String outputFile_1 = articleOutputFolder + "out_1_temp.txt";
        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);
        //WhitespaceCleaner.cleanWhitespace(outputFile_1);

        // Initials tagging.
        String outputFile_2 = articleOutputFolder + "out_2_temp.txt";
        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);
        //WhitespaceCleaner.cleanWhitespace(outputFile_2);

        // DBLP DICT tagging.
        String outputFile_3 = articleOutputFolder + "out_3_temp.txt";
        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");
        //WhitespaceCleaner.cleanWhitespace(outputFile_3);

        // SPRINGER JOURNAL TITLE tagging.
        String outputFile_4 = articleOutputFolder + "out_final_temp.txt";
        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.JOURTIT_SPRINGER, "", false);
        //WhitespaceCleaner.cleanWhitespace(outputFile_4);

    }

    /**
     * Add features to Springer book data. Note that the last file in the
     * pipeline needs to contain the word "final".
     *
     * @param pathToTokenized
     * @throws FileNotFoundException
     */
    public static void augmentTokenizedSpringerBooks(String pathToTokenized) throws FileNotFoundException {
        // Annotate book features.
        String bibtypeToBeAugmented = "feature_augmented_SPRINGER/BibBook_FeatureAnnotationPipeline/";
        String bookOutputFolder = pathToTokenized.substring(0, pathToTokenized.lastIndexOf("/") + 1) + bibtypeToBeAugmented;

        // YEAR tagging.
        String outputFile_1 = bookOutputFolder + "out_1_temp.txt";
        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);

        // Initials tagging.
        String outputFile_2 = bookOutputFolder + "out_2_temp.txt";
        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);

        // DBLP DICT tagging.
        String outputFile_3 = bookOutputFolder + "out_3_temp.txt";
        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");

        // SPRINGER PUBLISHER NAME tagging.
        String outputFile_4 = bookOutputFolder + "out_final_temp.txt";
        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.PUBNAMES_SPRINGER, "", false);

    }

    /**
     * Add features to Springer chapter data. Note that the last file in the
     * pipeline needs to contain the word "final".
     *
     * @param pathToTokenized
     * @throws FileNotFoundException
     */
    public static void augmentTokenizedSpringerChapters(String pathToTokenized) throws FileNotFoundException {
        // Annotate chapter features.
        String bibtypeToBeAugmented = "feature_augmented_SPRINGER/BibChapter_FeatureAnnotationPipeline/";
        String chapterOutputFolder = pathToTokenized.substring(0, pathToTokenized.lastIndexOf("/") + 1) + bibtypeToBeAugmented;

        // YEAR tagging.
        String outputFile_1 = chapterOutputFolder + "out_1_temp.txt";
        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);

        // Initials tagging.
        String outputFile_2 = chapterOutputFolder + "out_2_temp.txt";
        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);

        // DBLP DICT tagging.
        String outputFile_3 = chapterOutputFolder + "out_3_temp.txt";
        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");

        // SPRINGER PUBLISHER NAME tagging.
        String outputFile_4 = chapterOutputFolder + "out_final_temp.txt";
        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.PUBNAMES_SPRINGER, "", false);

    }

    /**
     * Add features to DBLP article data. Note that the last file in the
     * pipeline needs to contain the word "final".
     *
     * @param pathToTokenized
     * @throws FileNotFoundException
     */
    public static void augmentTokenizedDBLPArticles(String pathToTokenized) throws FileNotFoundException {
        // Annotate article features.
        String bibtypeToBeAugmented = "feature_augmented_DBLP/BibArticle_FeatureAnnotationPipeline/";
        String articleOutputFolder = pathToTokenized.substring(0, pathToTokenized.lastIndexOf("/") + 1) + bibtypeToBeAugmented;

        // YEAR tagging.
        String outputFile_1 = articleOutputFolder + "out_1_temp.txt";
        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);

        // Initials tagging.
        String outputFile_2 = articleOutputFolder + "out_2_temp.txt";
        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);

        // DBLP DICT tagging.
        String outputFile_3 = articleOutputFolder + "out_3_temp.txt";
        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");

        // DBLP JOURNAL TITLE tagging.
        String outputFile_4 = articleOutputFolder + "out_final_temp.txt";
        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.JOURTIT_DBLP, "", false);

    }

    /**
     * Add features to DBLP book data. Note that the last file in the pipeline
     * needs to contain the word "final".
     *
     * @param pathToTokenized
     * @throws FileNotFoundException
     */
    public static void augmentTokenizedDBLPBooks(String pathToTokenized) throws FileNotFoundException {
        // Annotate book features.
        String bibtypeToBeAugmented = "feature_augmented_DBLP/BibBook_FeatureAnnotationPipeline/";
        String bookOutputFolder = pathToTokenized.substring(0, pathToTokenized.lastIndexOf("/") + 1) + bibtypeToBeAugmented;

        // YEAR tagging.
        String outputFile_1 = bookOutputFolder + "out_1_temp.txt";
        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);

        // Initials tagging.
        String outputFile_2 = bookOutputFolder + "out_2_temp.txt";
        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);

        // DBLP DICT tagging.
        String outputFile_3 = bookOutputFolder + "out_3_temp.txt";
        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");

        // DBLP PUBLISHER NAME tagging.
        String outputFile_4 = bookOutputFolder + "out_final_temp.txt";
        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.PUBNAMES_DBLP, "", false);

    }

    /**
     * Add features to DBLP chapter data. Note that the last file in the
     * pipeline needs to contain the word "final".
     *
     * @param pathToTokenized
     * @throws FileNotFoundException
     */
    public static void augmentTokenizedDBLPChapters(String pathToTokenized) throws FileNotFoundException {
        // Annotate chapter features.
        String bibtypeToBeAugmented = "feature_augmented_DBLP/BibChapter_FeatureAnnotationPipeline/";
        String chapterOutputFolder = pathToTokenized.substring(0, pathToTokenized.lastIndexOf("/") + 1) + bibtypeToBeAugmented;

        // YEAR tagging.
        String outputFile_1 = chapterOutputFolder + "out_1_temp.txt";
        FeaturesAdderLowLevel.addYearFeature(pathToTokenized, outputFile_1);

        // Initials tagging.
        String outputFile_2 = chapterOutputFolder + "out_2_temp.txt";
        FeaturesAdderLowLevel.addInitialsFeature(outputFile_1, outputFile_2);

        // DBLP DICT tagging.
        String outputFile_3 = chapterOutputFolder + "out_3_temp.txt";
        FeaturesAdderDictionaryWords.addDictionaryFeature(outputFile_2, outputFile_3, "DBLP");

        // DBLP PUBLISHER NAME tagging.
        String outputFile_4 = chapterOutputFolder + "out_final_temp.txt";
        FeaturesAdderDBLPSpringerData.addDataBaseFeatures(outputFile_3, outputFile_4, DictReader.PUBNAMES_DBLP, "", false);

    }
}
