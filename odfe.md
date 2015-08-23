---
layout: page
title: Blogging Like a Hacker
---
### ODF Explorer
The ODF Explorer provides allows a user to see how much of the underlying ODF [schema](https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=office) is used by a document or set of documents. See the Readme in the download or code base of this project to jump straight in.

#### Why would I want an ODF Explorer?
As a developer or tester of an ODF application you want to know if your beast is dealing with all the features and combinations that can be thrown at it. Most testing is done on \"the road most travelled\" and rightly so. But the trouble starts and error cases occur when a user travels down a branch not seen before. That\'s where this tool can come in handy.

Most developers and testers have heard of, or even used, code coverage analysis tools. The ODF Explorer is an example of a Production Coverage analysis tool. In code coverage, test cases are run and the code used as to run the tests is highlighted. And in practice the interesting thing is not always what has been exercised, it is what has been missed that you pay attention too. That suggests new test cases. So it is with Production Coverage, except we are not examining the code, we are examining how much of the schema (or grammar) of the system is being tested. And again it is often more interesting to see what is not being tested that is important. When we examine the results we can for instance see that we do not test bookmarks, or some feature of bookmarking. Or that we never use a particular attribute of an element.

#### What do I get for investing my time here?
The ODF Explorer is in two main pieces, the node.js based web server that is this project, and the Java command line application that does the actual trolling through the documents and spitting out JSON and dot files.

The server was created to provide a graphical framework (I\'m not a GUI designer) in which to run the command line tool and to examine the results. The details of the command line tool will be captured in an associated Github project real soon now :-)

I guess the easiest way to show you what you get is to show some scenarios.

* [Opening Page](opening.md)
* [Create and look into a single simple document](single.md)
* [Edit the document and compare](compare.md)
* [Aggregate groups of documents](aggregate.md)

#### Is this thing being used anywhere?
It is being used by your truly as the basis for a post graduate research project at [Curtin University](http://cs.curtin.edu.au/). And I will be applying it to the [Apache Corinthia](http://corinthia.incubator.apache.org/) development.

#### Okay I\'m interested how do I get to use it?
Clone the project and follow the destructions in the readme. Or download the release and do the same.
