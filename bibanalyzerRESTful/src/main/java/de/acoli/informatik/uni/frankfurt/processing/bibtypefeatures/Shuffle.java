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
import java.util.Collections;
import java.util.Scanner;

/**
 * Description:
 * Program to shuffle a set of feature vectors.
 * 
 * @author niko
 */
public class Shuffle {

    public static void shuffle(String inputFile) throws FileNotFoundException {

        Scanner s = new Scanner(new File(inputFile));
        ArrayList<String> lines = new ArrayList<String>();
        int lineCount = 0;
        while (s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            lines.add(aLine);
        }
        s.close();
        Collections.shuffle(lines);
        PrintWriter w = new PrintWriter(new File(inputFile + "_shuf"));
        for (String l : lines) {
            w.write(l + "\n");
        }
        w.flush();
        w.close();
    }
}
