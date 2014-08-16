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
import java.util.Scanner;

/**
 * Description:
 * 
 * Removes font tags from CRF training and test data format.
 *
 * @author niko
 */
public class FeaturesFontRemover {
    
    public static void main(String[] args) throws FileNotFoundException {
        
        String path = "input/dumps/SPRINGER_dump/export/11000/bibchapters/";
        String bibtype = "bibchapter";
        
        //String analyze = "11000_" + bibtype + "_TRAIN.txt";
        String analyze = "1100_" + bibtype + "_TEST.txt";
        
       
        
        PrintWriter w = new PrintWriter(new File(path + analyze + "withoutFont.txt"));
        Scanner s = new Scanner(new File(path + analyze));
        
        while(s.hasNextLine()) {
            String aLine = s.nextLine();
            if(aLine.length() > 0) {
                String[] split = aLine.split("\\s");
                if(split.length > 2) {
                    // Remove intermediate features.
                    // Only write token + sequence label.
                    w.write(split[0] + " " + split[split.length-1] + "\n");
                }
                else {
                    w.write(aLine + "\n");
                }
                
            }
            else {
                w.write("\n");
            }
        }
        
        s.close();
        w.flush();
        w.close();
        
    }
    
}
