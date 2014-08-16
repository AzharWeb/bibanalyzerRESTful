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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Description:
 * 
 * Utility class to clean redundant whitespace characters from CRF data format.
 * 
 *
 * @author niko
 */
public class WhitespaceCleaner {
    
    public static final String DIR = "/home/niko/Desktop/Rohdaten/Neuer-bibtex_export/z_texte_mehr_variation_crf_training/out/article/";
    public static final String INPUT_FILE = "2000_article_TEST.txt_3";
    
    
    /**
     * Clean white space characters.
     * @param inputFile, an input file to be cleaned.
     * @throws FileNotFoundException 
     */
    public static void cleanWhitespace(String inputFile) throws FileNotFoundException {
        ArrayList<String> lines = new ArrayList<String>();
        Scanner s = new Scanner(new File(inputFile));
        while(s.hasNextLine()) {
            String aLine = s.nextLine().trim();
            aLine = aLine.replace("  ", " ");
            lines.add(aLine);
        }
        
        
        PrintWriter w = new PrintWriter(new File(inputFile));
        for(String l : lines) {
            w.write(l + "\n");
        }
        w.flush();
        w.close();
        s.close();
    }   
}
