# GZoltar Analysis Heat Map generator

Creates an overlay of the suspiciousness scores from the GZoltar analysis, onto the student's source code. Uses the native output from GZoltar to create the heat map. The newly created heat map is put into a specified location.

## Executing the analysis

This verison uses an Ant take to control the execution of the heat map.  

To execute the analysis, execute these two commands.

```
ant -f <path to this tool>/build.xml clean
ant -f <path to this tool>/build.xml run
```


## Notes

* Currently does not support reference tests that are in packages. 

## Built With

* [GZoltar](http://www.gzoltar.com/) - The GZoltar Framework
* [Apache Ant](http://ant.apache.org/) - The Ant Build System
