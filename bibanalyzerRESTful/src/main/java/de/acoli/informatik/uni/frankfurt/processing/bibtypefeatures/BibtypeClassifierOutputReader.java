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
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Description:
 * Produces a final output file with unique prediction results
 * for bibtype classification (including probabilities for the labels).
 * 
 * @author niko
 */
public class BibtypeClassifierOutputReader {

    /**
     * Get unique predictions for multi label classification.
     * 
     * @param featvecFile, the original file with the feature vectors and the reference strings as comments.
     * @param classifiedOutput, the classified (multi-label) output.
     * @param predictionsForPlaintextRefs, the new output with the unique predictions.
     * @return
     * @throws FileNotFoundException 
     */
    public static ArrayList<String> getPredictions(String featvecFile, String classifiedOutput, String predictionsForPlaintextRefs) throws FileNotFoundException {

        ArrayList<String> predictions = new ArrayList<String>();
        
        // Read in comment references.
        ArrayList<String> refs = new ArrayList<String>();
        Scanner sc = new Scanner(new File(featvecFile)); // "bibtypes/test.txt"
        while (sc.hasNextLine()) {
            String aLine = sc.nextLine();
            String ref = aLine.substring(aLine.indexOf("#") + 1).trim();
            //System.out.println(ref);
            refs.add(ref);
        }
        sc.close();

        PrintWriter w = new PrintWriter(new File(predictionsForPlaintextRefs));

        //String test_tagged = "outMEbig.txt"; // 0.7833566713342669
        //String test_tagged = "outNBbig.txt"; // 0.8007601520304061
        //String test_tagged = "outMEsmall.txt"; // 0.8287657531506302
        //String test_tagged = "outNBsmall.txt"; // 0.7875575115023005
        
        Scanner s = new Scanner(new File(classifiedOutput));
        int correct = 0;
        int wrong = 0;
        int lineCount = 0;
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim();

            if (aLine.length() > 0) {
                Scanner lineScan = new Scanner(aLine);
                //System.out.println("--> " +aLine);
                lineScan.useDelimiter("\t");
                String label = lineScan.next();
                //System.out.println(lineCnt);
                //System.out.print("Gold: " + label + " ");

                String zero = lineScan.next();
                double probZero = lineScan.nextDouble();
                String minusOne = lineScan.next();
                double probMinusone = lineScan.nextDouble();
                String one = lineScan.next();
                double probOne = lineScan.nextDouble();

                double greatestProb = 0.0;
                String predictedLabel = "null";
                if (probZero > probMinusone) {
                    greatestProb = probZero;
                    predictedLabel = zero;
                } else {
                    greatestProb = probMinusone;
                    predictedLabel = minusOne;
                }
                if (probOne > greatestProb) {
                    greatestProb = probOne;
                    predictedLabel = one;
                }

                //System.out.println(" Greatest prob: " + greatestProb + " label: " + predictedLabel);
                
                // TODO:
                // Improve classification.
                // Articles with low probability are 'probably' no articles. ;- )
                
                w.write(predictedLabel + "\t" + greatestProb + "\n");
                predictions.add(predictedLabel);
                
                
                if (label.equals(predictedLabel)) {
                    correct++;
                } else {
                    wrong++;
                    //System.out.println("Misclassified (" + lineCount + "): " + refs.get(lineCount));
                }
            }
            lineCount++;
        }
        //System.out.println("correct: " + correct + " wrong: " + wrong);
        double accuracy = (double) correct / (double) (correct + wrong);
        //System.out.println("Accuracy: " + accuracy);

        s.close();

        w.flush();
        w.close();
        
        return predictions;
    }
}
