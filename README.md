ADW
===

ADW (Align, Disambiguate and Walk) version 1.0 -- October 13, 2014.

Online demo: http://lcl.uniroma1.it/adw/

CONTENTS
=========================

1. [Introduction](#1-introduction)
2. [Installation](#2-installation)
3. [Requirements](#3-requirements)
4. [Quick start](#4-quick-start)
    - 4.1. [Input formats](#41-input-formats)
    - 4.2. [Signature comparison methods](#42-signature-comparison-methods)
5. [License](#5-license)



1. INTRODUCTION
=========================

This package provides a Java implementation of ADW, a state-of-the-art semantic similarity approach that enables the comparison of lexical items at different lexical levels: from senses to texts. For more details about the approach please refer to:
http://wwwusers.di.uniroma1.it/~navigli/pubs/ACL_2013_Pilehvar_Jurgens_Navigli.pdf

This release version is prepared by Mohammad Taher Pilehvar (Sapienza University of Rome).


2. INSTALLATION
=========================

1- Download the Semantic signatures (for all the 118K concepts in WordNet 3.0, size ~ 1.4 GB) using the following link:
http://lcl.uniroma1.it/adw/ppvs.30g.5k.tar.bz2

2- Extract the downloaded file in the directory of your choice. 

For example:

	/home/username/signatures/

3- Update the `wn30g.ppv.path` entry in the `config/adw.properties` file with the directory containing semantic signatures.

For example:

	wn30g.ppv.path=/home/username/signatures/ppvs.30g.5k/

4- You are ready to go! To get started, continue reading [Quick start](#4-quick-start).



3. REQUIREMENTS
=========================

- Java 6 (JRE 1.6) or higher
- Semantic signatures (see [Installation](#2-installation))
- WordNet 3.0 dictionary files (already included in the resources directory)



4. QUICK START
=========================

The following is a usage example for measuring semantic similarity using ADW.

	ADW pipeLine = new ADW();
	
	double score = pipeLine.getPairSimilarity(text1, text2,
        					disMethod, measure,
       						srcTextType, trgTextType); 


Where:

`text1` and `text2` are the two lexical items to be compared. The types of these lexical items is denoted by `srcTextType` and `trgTextType`, respectively. For supported input formats please see 4.1.

`disMethod` specifies if the pair of lexical items have to be disambiguated or not. In the current version we support:
- **ALIGNMENT_BASED** Alignment-based disambiguation (see Pilehvar et al., 2013)
- **NONE** No disambiguation

`measure` denotes the method utilized for comparing pairs of semantic signatures. For supported methods please see 4.2.


For example:

	//the two lexical items to be compared
	String text1 = "a mill that is powered by the wind";    
	String text2 = "windmill.n.1";

	//types of the two lexical items
	ItemType srcTextType = ItemType.SURFACE;  
	ItemType trgTextType = ItemType.WORD_SENSE;

	//if lexical items has to be disambiguated
	DisambiguationMethod disMethod = DisambiguationMethod.ALIGNMENT_BASED;      

	//measure for comparing semantic signatures
	SignatureComparison measure = new WeightedOverlap(); 

	ADW pipeLine = new ADW();

	double similarity = pipeLine.getPairSimilarity(text1, text2,
        				      disMethod, measure,
       					      srcTextType, trgTextType); 
	System.out.println(similarity);


### 4.1 INPUT FORMATS

In this version, we support five different input formats:

- **SURFACE** Text in surface form (e.g., `A baby is playing with a dog`)
- **SURFACE_TAGGED** Lemmas with part of speech tags (e.g., `baby#n be#v play#n dog#n`). We support only four parts of speech: nouns (n), verbs (v), adjectives (a), and adverbs (r).
- **SENSE_KEYS** WordNet 3.0 sense keys (e.g., `baby%1:18:00:: play%2:33:00:: dog%1:05:00::`)
- **SENSE_OFFSETS** WordNet 3.0 synset offsets (e.g., `09827683-n 01072949-v 02084071-n`)
- **WORD_SENSE** WordNet 3.0 Word sense (e.g., `baby.n.1 play.v.1 dog.n.1` or `baby#n#1 play#v#1 dog#n#1`)

### 4.2 SIGNATURE COMPARISON METHODS

Different signature comparison methods are included (implement the SignatureComparison interface):

- **WeightedOverlap**
- **Cosine**
- **Jaccard**

For details of the above three, please see Pilehvar et al. (2013)

- **KLDivergence** 
    - details: http://en.wikipedia.org/wiki/Kullback%E2%80%93Leibler_divergence
- **JensenShannon** divergence 
    - details: http://en.wikipedia.org/wiki/Jensen%E2%80%93Shannon_divergence


5. LICENSE
=========================

ADW (Align, Disambiguate and Walk) -- A Unified Approach for Measuring Semantic Similarity.

Copyright (c) 2014 Sapienza University of Rome.
All Rights Reserved.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

If you use this system, please cite the following paper:

> M. T. Pilehvar, D. Jurgens and R. Navigli. Align, Disambiguate and Walk: A Unified Approach for Measuring Semantic Similarity.
> Proceedings of the 51st Annual Meeting of the Association for Computational Linguistics (ACL 2013), Sofia, Bulgaria, August 4-9, 2013, pp. 1341-1351.


For more information please contact:

> pilehvar atsign di (dot) uniroma1 (dot) it

For bug reports, fixes and issues please use our github page:
https://github.com/pilehvar/ADW



