Instructions on how to train a CRF model for bibfield classification.

1. Properly export test and train data from the dump of "gold" annotated references.
Classes in the Java package "crfformat" are useful for this purpose.

- DBLPStyleCombinerAndMalletFormatExporter.java exports DBLP train and test data from the data dump.
- SpringerStyleCombinerAndMalletFormatExporter.java exports SPRINGER train and test data from the data dump.


2. Having exported training and test data, we can now train a CRF model:
For the sake of illustration, we assume that we have already exported some toy data. From within the DBLP_articles directory, for example,
in order to train a CRF model based on the token features, simply exectute the following command:

DBLP_articles $ java -jar ../../../crf_tagger.jar --train true --model-file articleModel 5_article_TRAIN.txt 

This will generate a model file "articleModel" which can be used for bibfield classification for unseen references.

Applying this to our test data, we can simply run the model and evaluate the overall test accuracy:

java -jar ../../../crf_tagger.jar --test lab --model-file articleModel 2_article_TEST.txt


More information on how to train and test models is available on the original MALLET homepage:
http://mallet.cs.umass.edu/sequences.php

3. 
Note that we haven't used any features for training (except the plain tokens).
Adding additional features to our training data can simply be achieved by means of some helper class(es)
(cf. the main method of processing.bibfieldfeatures.TokenizedPlaintextFeatureAnnotation.java)
(Note that features can also be removed, such as font information / cf. FeaturesFontRemover.java)