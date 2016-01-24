### ODF Explorer
The ODF Explorer allows a user to see how much of the underlying ODF [schema](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=office) is used by a document or set of documents.

A user can also 
* compare documents - with respect to their schema use
* aggregate several groups of documents, and see how each document contributes to the coverage

### External Dependencies
The code here provides a node.js based http server as a gui for the underlying Java application.
Yeah, I'm not a graphics designer... sigh

Before you can get the stuff to run you need
* node.js - see https://nodejs.org/download/
* bower - see http://bower.io/

To build the underlying ODF Explorer, which is a Java application, you need to have both [Maven](https://maven.apache.org/) and Java installed.

The tool also uses Graphviz to generate graphs. It can installed be from http://www.graphviz.org/Download.php

###Installation
Clone this code, once you have NodeJS, Java, Maven, and Grpahviz (optional but you will not be able to generate the graphs if you don't have it available on your command line).
Using a command line window change directory to your code base and enter
```
npm install
```
This will fetch the node_modules required.

###Make it run
To start the http server type
```
npm start
```

This will build the Java application using Maven on the first run and start the server.

Subsequently it will just start the server.

You will see a message  
```
Express server listening on port 3000
```

Open a Chrome or Firefox web browser. Windows Explorer will just lead to a world of pain.   
I've not seen how it runs on a Mac.

Go to the url localhost:3000/app/index.html (which I can't make show up as a link here)

There is a simple test HelloWorld document located in the testDoc directory to play with.
Submit it and see the kind of results you can get.

Click on the red ODFE Info button to check you have the jar.  
Click on the green "What is this?" button to learn more.  
Or the project web page [ODF Explorer](http://hammyau.github.io/ODFExplorer/)

