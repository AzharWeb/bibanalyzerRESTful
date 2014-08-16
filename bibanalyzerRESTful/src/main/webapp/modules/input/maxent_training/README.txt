Instructions on how to train a maxent or NB classifier for bibtype classification:

Cf. BibtypeFeatureValuePairGeneratorFromPlaintextReferences.java

1. Specify plaintext references for training and test data.
You can use DBLPStyleCombinerAndMalletFormatExporter.java
and SpringerStyleCombinerAndMalletFormatExporter.java in the crfformat package to export plaintext references from the data dump.

2. Write feature vectors to new file.



3. Train classifier on this training data.
        // a) convert svm light training format to binary Mallet format.
        // ./mallet-2.0.7/bin/mallet import-svmlight --input input/maxent_training/bibtypefeaturevectors_shuf --output input/maxent_training/train.mallet
        
        // b) Train MaxEnt classifier from this training data.
        // ./mallet-2.0.7/bin/mallet train-classifier --trainer MaxEnt --input input/maxent_training/train.mallet --output-classifier input/maxent_training/my.MEclassifier
        
        // c) Alternatively, train Naive Bayes classifier from this training data.
        // ./mallet-2.0.7/bin/mallet train-classifier --input input/maxent_training/train.mallet --output-classifier input/maxent_training/my.NBclassifier
        
        // 4. Run (test) classifier on a raw (svm-light) feature vectors file.
        // Note that we use the training data for the sake of illustration.
        // ./mallet-2.0.7/bin/mallet classify-file --input input/maxent_training/bibtypefeaturevectors --output - --classifier input/maxent_training/my.MEclassifier > input/maxent_training/predicted
        
        