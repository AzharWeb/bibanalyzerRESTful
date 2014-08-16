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

/**
 * TODO:
 * This program is supposed to normalize the plaintext input before the
 * actual analysis starts.
 * 
 * For example, "firstpage-lastpage" patterns should be normalized to have 
 * a hyphen instead of m-dashes or similar related separators which the CRF
 * trained models are not aware of and could potentially cause problems for the
 * analysis.
 * 
 * 
 * This is another rule-based component of the bibanalyzer.
 *
 * @author niko
 */
public class PlaintextPreprocessor {
    
    
    // TODO:
    // CRF is trained on pageFirstpart - pageSecondpart for example.
    // such as p. 30-40.
    
    // CRF models get confused if they encounter something like 30--40
    // (two hyphens).
    
    // TODO: preprocess plaintext refs and detect cases like that.
    
}
