# GZoltar Analysis Plugin

Performs a spectrum-based fault localization analysis of the student's code submission, using the [GZoltar](http://www.gzoltar.com/) framework. It applies the instructor reference tests to the submission, and generates a suspiciousness score for each line of code executed in the test cases. The results are then output into a location specified.

## Executing the analysis

This verison uses an Ant take to control the execution of the analysis.  

To execute the analysis, execute these two commands.

```
ant -f <path to this tool>/build.xml clean
ant -f <path to this tool>/build.xml gz.report
```


## Notes

* Currently does not support reference tests that are in packages. 

## Built With

* [GZoltar](http://www.gzoltar.com/) - The GZoltar Framework
* [Apache Ant](http://ant.apache.org/) - The Ant Build System
