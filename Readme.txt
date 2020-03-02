Scott Kratochvil
Boolean Search Engine 

A small Java program to index and perform efficient text searches in a group of files.

Compile instructions:
javac -cp javatuples-1.2.jar:stanford-corenlp.jar:jsoup-1.12.1.jar QueryProcessor.java Stemmer.java QueryThread.java

Running Instructions:
The documentset folder and queries.txt file should be in the same folder as QueryProcessor.class. Enter the following in the terminal:
java -cp javatuples-1.2.jar:stanford-corenlp.jar:jsoup-1.12.1.jar:. QueryProcessor documentset queries.txt 

    (where documentset and queries.txt are the names of the document folder and queries file)

Run example: java -cp javatuples-1.2.jar:stanford-corenlp.jar:jsoup-1.12.1.jar:. QueryProcessor documentset queries.txt

Approximate run-time: 20 seconds
