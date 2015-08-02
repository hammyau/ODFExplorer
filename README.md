### ODF Explorer
The ODF Explorer provides allows a user to see how much of the underlying ODF [schema](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=office) is used by a document or set of documents.

A use can also 
* compare documents - with respect to their schema use
* aggregate several groups of documents, and see how each document contributes to the coverage

### External Dependencies
The code here provides a node.js based http server as a gui for the underlying Java application.
Yeah, I'm not a graphics designer... sigh

Before you can get the stuff to run you need
* node.js - see https://nodejs.org/download/
* bower - see http://bower.io/

To run the underlying ODF Explorer, which is a Java application, you need to have Java installed.

The tool also uses Graphviz to generate graphs. It can installed from http://www.graphviz.org/Download.php

###Installation
Download this code once you have node and bower.
Using a command line window change directory to your code base and enter
```
node install
```
This will fetch the node_modules required

To get the rest of the required bits and pieces enter
```
gulp
```
This will fetch angular.js bootstrap, d3, and the required jars from the release.

###Make it run
To start the http server from a command line change directory to the code root and enter
```
npm start
```

You will see a message  
```
Express server listening on port 3000
```

Open a Chrome web browser. Windows Explorer will just lead to a world of pain.   
Firefox seems to have some issues too. I need to get onto that...   
I've not seen how it runs on a Mac.

Go to the url localhost:3000/app/index.html

Click on the red ODFE Info button to check you have the jar.  
Click on the green "What is this?" button to learn more.

