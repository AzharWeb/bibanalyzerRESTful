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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Description:
 *
 * Utility class to obtain a list of tuples (token + lable) for a CRF mallet
 * format file.
 *
 *
 *
 * @author niko
 */
public class CRFOutputReader {

    /**
     * Reads in a tagged CRF file and returns a list (for each reference) of
     * String array lists (for each token-label tuple).
     *
     * @param fileName
     * @param includeBOSandEOS
     * @return
     * @throws FileNotFoundException
     */
    public static ArrayList<ArrayList<String[]>> getPredictedTokensAndTagsForReferences(String fileName, boolean includeBOSandEOS) throws FileNotFoundException {

        ArrayList<ArrayList<String[]>> referencesPlusTokens = new ArrayList<>();

        Scanner crfOutputScan = new Scanner(new File(fileName));

        while (crfOutputScan.hasNextLine()) {

            String aLine = crfOutputScan.nextLine().trim();

            if (aLine.length() > 0) {
                ArrayList<String[]> aRefToks = new ArrayList<String[]>();
                while (true) {
                    String[] split = aLine.split("\\s");

                    // Handle features.
                    String lab = split[0];
                    String tok = "";

                    for (String item : split) {
                        if (!(item.contains("<") && item.contains(">"))) {
                            tok = item;
                        }
                    }

                    split[0] = lab;
                    split[1] = tok;

                    //System.out.println(split[0] + "---" + split[1]);
                    if (split[1].equals("EOR")) {
                        if (includeBOSandEOS) {
                            aRefToks.add(new String[]{"<EOR>", "EOR"});
                        }
                        referencesPlusTokens.add(aRefToks);
                        break;
                    } else {
                        if (!split[1].equals("BOR")) {
                            aRefToks.add(split);
                        } else {
                            if (includeBOSandEOS) {
                                aRefToks.add(new String[]{"<BOR>", "BOR"});
                            }
                        }
                        aLine = crfOutputScan.nextLine();
                    }

                }
            }
        }
        crfOutputScan.close();

        //System.out.println("Read in " + referencesPlusTokens.size() + " complete references.");
        return referencesPlusTokens;
    }
}
