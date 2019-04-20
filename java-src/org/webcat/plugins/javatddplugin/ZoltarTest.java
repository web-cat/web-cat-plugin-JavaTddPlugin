package org.webcat.plugins.javatddplugin;

import java.io.*;
import java.util.*;

import com.gzoltar.core.*;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.spectra.Spectra;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ZoltarTest
{
    private static Logger log = Logger.getLogger(ZoltarTest.class);
    private static String gzXmlFile = System.getProperty("gzoltar.xml.output");
    private static String gzCsvFile = System.getProperty("gzoltar.csv.output");
    private static String gzStudentPid =
        System.getProperty("gzoltar.student.pid");
    private static String gzStudentBinDir =
        System.getProperty("gzoltar.student.bin");
    private static String gzTestsDir = System.getProperty("gzoltar.tests.dir");
    private static String gzLibs = System.getProperty("gzoltar.libs");
    private static Integer gzTestDepth =
        Integer.parseInt(System.getProperty("gzoltar.test.depth", "1"));

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        log.setLevel(Level.DEBUG);

        String paramSortOrder = "CLMS";
        if (log.isDebugEnabled())
        {
          log.debug("gzXmlFile: " + gzXmlFile);
          log.debug("gzCsvFile: " + gzCsvFile);
          log.debug("gzStudentPid: " + gzStudentPid);

          log.debug("gzStudentBinDir: " + gzStudentBinDir);
          log.debug("gzTestsDir: " + gzTestsDir);
          log.debug("gzLibs: " + gzLibs);

          log.debug("Sort: " + paramSortOrder);
          log.debug("Project: " + gzStudentPid);
        }

        try
        {
            ArrayList<String> allTestNames = new ArrayList<String>();
            ArrayList<String> failedTestNames = new ArrayList<String>();
            File[] testList = new File(gzTestsDir).listFiles();

            log.info(testList.length + " files found");
            for (File testfile : testList)
            {
                String testname = testfile.getName();

                if (testname.indexOf("Test") != -1)
                {
                    //File is a test file
                    int dot = testname.lastIndexOf(".");
                    String name = testname.substring(0, dot);
                    allTestNames.add(name);
                    log.debug("...."+ testname + " added");
                }
                else
                {
                    log.debug("...."+ testname + " skipped");
                }
            }

            Spectra spectra = execGzoltar(allTestNames);

            Thread.sleep(4000);

            Spectra failedOnlySpectra = null;
            List<Component> suspectStatesAllTests = null;
            List<Component> suspectStatesFailedOnly = null;


            // if gzTestDepth is non-zero, return the suspiciousness for the
            // first gzTestDepth-th failed cases
            // if gzTestDepth is zero, return results for all failed cases
            int failedTests = 0;

            if (gzTestDepth > 0)
            {
                log.info("Getting Suspicious Results from first "
                    + gzTestDepth + " failed tests...");

                for (TestResult result : spectra.getTestResults())
                {
                    if (!result.wasSuccessful() && failedTests < gzTestDepth)
                    {
                        String testname = result.getName();
                        int dot = testname.lastIndexOf("#");
                        String name = testname.substring(0, dot);
                        failedTestNames.add(name);
                        log.debug(result.getName() + "(" + name
                            + ")...FAILED!");
                        //log.debug(result.getCoveredComponents().toString());
                        failedTests++;
                    }
                    else
                    {
                        log.debug(result.getName() + "...passed");
                    }
                }
                if (log.isDebugEnabled())
                {
                    log.debug("failedTest Names :");
                    for (String s : failedTestNames)
                    {
                        log.debug("...." + s);
                    }
                }
                if (failedTestNames.size() > 0)
                {
                    //rerun zoltar for the failed cases only.
                    failedOnlySpectra = execGzoltar(failedTestNames);
                }
            }

            suspectStatesAllTests = spectra.getComponentsBySuspiciousness();
            if (failedOnlySpectra != null)
            {
                suspectStatesFailedOnly =
                    failedOnlySpectra.getComponentsBySuspiciousness();
                for (TestResult result : failedOnlySpectra.getTestResults())
                {
                    if (!result.wasSuccessful())
                    {
                        log.debug(result.getName() + "...FAILED!");
                    }
                    else
                    {
                        log.debug(result.getName() + "...passed");
                    }
                }
            }

            System.out.println("");
            System.out.println("");

            List<String> suspectsLines = new ArrayList<String>();
//            List<String> failedOnlySuspectsLines = new ArrayList<String>();
            String encodedLine = "";

            Map<String, Double> maxMethods = new HashMap<String, Double>();
            Map<String, Double> sortedMaxMethods =
                new HashMap<String, Double>();
            Map<String, Double> mostSuspectMethods =
                new HashMap<String, Double>();
            Map<String, Double> fullSuspectMap = new HashMap<String, Double>();

            if (!suspectStatesAllTests.isEmpty())
            {
                for (Component statement : suspectStatesAllTests)
                {
                    fullSuspectMap.put(statement.getLabel(),
                        statement.getSuspiciousness());
                    if (statement.getSuspiciousness() != 0.0)
                    {
                        Map<String, String> splitResults =
                            splitZoltarReportLineWithoutSize(
                            statement.getLabel());
                        String currMethodSig = splitResults.get("class") + "|"
                            + splitResults.get("method");

                        if (maxMethods.containsKey(currMethodSig))
                        {
                            if (maxMethods.get(currMethodSig) <
                                statement.getSuspiciousness())
                            {
                                maxMethods.put(currMethodSig,
                                    statement.getSuspiciousness());
                            }
                        }
                        else
                        {
                            maxMethods.put(currMethodSig,
                                statement.getSuspiciousness());
                        }
                    }
                }

                if (log.isDebugEnabled())
                {
                    printMap(sortByValue(fullSuspectMap));
                }

                if (suspectStatesFailedOnly != null)
                {
                    for (Component component : suspectStatesFailedOnly)
                    {
                        Statement statement = (Statement)component;
                        if (statement.getSuspiciousness() != 0.0)
                        {
                            log.debug(statement.getLabel() + ' '
                                + statement.getSuspiciousness());
                            Map<String, String> splitResults =
                                splitZoltarReportLineWithoutSize(
                                statement.getLabel());
 /*                       String currMethodSig = splitResults.get("class").toString() + "|" + splitResults.get("method").toString();


                        if ( maxMethods.containsKey(currMethodSig)){
                            if ( maxMethods.get(currMethodSig) < statement.getSuspiciousness() ){
                                maxMethods.put(currMethodSig, statement.getSuspiciousness());
                            }
                        }else{
                            maxMethods.put(currMethodSig, statement.getSuspiciousness());
                        }
*/

                            if ("CLMS".equals(paramSortOrder))
                            {
                                encodedLine = splitResults.get("class")
                                    + "|" + statement.getLineNumber()
                                    + "|" + splitResults.get("method")
                                    + "|"
                                    + fullSuspectMap.get(statement.getLabel());
                            }
                            else if ("CSLM".equals(paramSortOrder))
                            {
                                encodedLine = splitResults.get("class")
                                    + "|"
                                    + fullSuspectMap.get(statement.getLabel())
                                    + "|" + statement.getLineNumber()
                                    + "|" + splitResults.get("method");
                            }
                            else if ("CMLS".equals(paramSortOrder))
                            {
                                encodedLine = splitResults.get("class")
                                    + "|" + splitResults.get("method")
                                    + "|" + statement.getLineNumber()
                                    + "|"
                                    + fullSuspectMap.get(statement.getLabel());
                            }
                            else
                            {
                                throw new IllegalStateException(" parameter "
                                    + "sort order " + paramSortOrder + " is "
                                    + "not supported.");
                            }
                            suspectsLines.add(encodedLine);
                        }
                    }
                }
                else
                {
                    log.info("No suspicious lines found for failed tests");
                }
                log.info("..." + suspectsLines.size()
                    + " suspicious lines found");
            }
            else
            {
                log.info("... 0 (zero) suspicious lines found");
            }

            sortedMaxMethods = sortByValue(maxMethods);

            // @TODO remote the lastX value here
            mostSuspectMethods = returnLastXFromMap(sortedMaxMethods, 3);
            if (log.isDebugEnabled())
            {
                log.debug("Most suspicious methods...");
                printMap(mostSuspectMethods);
            }

            Collections.sort(suspectsLines);
            if ((gzCsvFile != null) && (!gzCsvFile.isEmpty()))
            {
              log.info("Writing to file (" + gzCsvFile + ")...");
              FileWriter writer = new FileWriter(gzCsvFile, true);
              for (String s : suspectsLines)
              {
                    Map<String, String> suspectMap =
                        splitSuspectLineWithFormat(s, paramSortOrder);
                    writer.append(gzStudentPid);
                    writer.append(',');
                    writer.append(suspectMap.get("class"));
                    writer.append(',');
                    writer.append(suspectMap.get("method"));
                    writer.append(',');
                    writer.append(suspectMap.get("line"));
                    writer.append(',');
                    writer.append(suspectMap.get("score"));
                    writer.append('\n');
                    if (log.isDebugEnabled())
                    {
                        log.debug("Class: " + suspectMap.get("class") +
                            " Method: "  + suspectMap.get("method") +
                            " Line: " + suspectMap.get("line")+
                            " Suspect: " + suspectMap.get("score"));
                    }
                }
                writer.flush();
                writer.close();
            }

            if ((gzXmlFile != null) && (!gzXmlFile.isEmpty()))
            {
                //We want to build an xml file with this format:
                //<root>
                //	<classname>
                //		<suspect>
                //			<method>
                //			<line>
                //			<score>
                //          <most>
                //		</suspect>
                //		(repeat for all suspects in class)
                //	</classname>
                //	(repeat for all classes with suspects)
                //</root>

                //Initialize XML
                DocumentBuilderFactory docFactory =
                    DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                // root elements
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("gzoltar");
                doc.appendChild(rootElement);

                if (suspectsLines.size() > 0)
                {
                    String lastClass = "";
                    Element suspectsElement = null;
                    String mostSuspectFlag = "no";
                    for (String s : suspectsLines)
                    {
                        Map<String, String> suspectMap =
                            splitSuspectLineWithFormat(s, paramSortOrder);

                        if (log.isDebugEnabled())
                        {
                            log.debug("Class: " + suspectMap.get("class") +
                        		" Method: "  + suspectMap.get("method") +
                        		" Line: " + suspectMap.get("line")+
                        		" Suspect: " + suspectMap.get("score"));
                        }

                        // Get the name of the current class.
                        String currentClass = suspectMap.get("class");
                        String classname = "";
                        // System.out.println("currentClass: " + suspectMap.get("class").toString());

                        // Is this one of the maxSuspect methods:
                        // System.out.println("looking for " + suspectMap.get("class").toString() + "|" + suspectMap.get("method"));
                        if (mostSuspectMethods.containsKey(
                            suspectMap.get("class") + "|"
                            + suspectMap.get("method")))
                        {
                            mostSuspectFlag = "yes";
                            // System.out.println("...FOUND...");
                        }
                        else
                        {
                            mostSuspectFlag = "no";
                        }
                        // If it is a private helper class (ie
                        // LinkedQueue$Node), we really care about the base
                        // class name. Also the $ is not allowed as an XML
                        // node name. So we test to see if we get the $, and
                        // if we do, get the string in front of $ as the class
                        // name.
                        if (currentClass.indexOf("$") != -1)
                        {
                            // is a class within a class
                            int dot = currentClass.lastIndexOf("$");
                            classname = currentClass.substring(0, dot);
                        }
                        else
                        {
                            classname = currentClass;
                        }

                        // System.out.println("classname: " + classname);

                        // Now, see if the class is the same as the last one
                        // we populated
                        if (!lastClass.equals(classname))
                        {
                            // System.out.println("Creating new class node.
                            // Was: " + lastClass +" now: " + classname);
                            // new class, so update the lastClass flag, append
                            // the current class node to root and create a new
                            // class node for the new class to be processed.
                            if (!lastClass.equals(""))
                            {
                                rootElement.appendChild(suspectsElement);
                            }
                            // suspectsElement = null;
                            lastClass = classname;
                            suspectsElement = doc.createElement(classname);
                        }
                        else
                        {
                            // System.out.println("Using same class as before: " + lastClass);
                        }

                        Element suspiciousStatementElement =
                            doc.createElement("suspect");

                        // Element suspiciousStatementClassElement = doc.createElement("class");

                        Element suspiciousStatementMethodElement =
                            doc.createElement("method");
                        Element suspiciousStatementLineElement =
                            doc.createElement("line");
                        Element suspiciousStatementScoreElement =
                            doc.createElement("score");
                        Element suspiciousStatementMostSuspectElement =
                            doc.createElement("most");

                        // suspiciousStatementClassElement.appendChild(doc.createTextNode(suspectMap.get("class").toString()));

                        suspiciousStatementMethodElement.appendChild(
                            doc.createTextNode(suspectMap.get("method")));
                        suspiciousStatementLineElement.appendChild(
                            doc.createTextNode(suspectMap.get("line")));
                        suspiciousStatementScoreElement.appendChild(
                            doc.createTextNode(suspectMap.get("score")));
                        suspiciousStatementMostSuspectElement.appendChild(
                            doc.createTextNode(mostSuspectFlag));

                        // suspiciousStatementElement.appendChild(suspiciousStatementClassElement);
                        suspiciousStatementElement.appendChild(
                            suspiciousStatementMethodElement);
                        suspiciousStatementElement.appendChild(
                            suspiciousStatementLineElement);
                        suspiciousStatementElement.appendChild(
                            suspiciousStatementScoreElement);
                        suspiciousStatementElement.appendChild(
                            suspiciousStatementMostSuspectElement);

                        suspectsElement.appendChild(
                            suspiciousStatementElement);
                    }
                    // Need to have this here so that the last class node
                    // is added to the root node.
                    rootElement.appendChild(suspectsElement);
                }

                // write the content into xml file
                TransformerFactory transformerFactory =
                    TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "2");
                DOMSource source = new DOMSource(doc);
                StreamResult resultStream = new StreamResult(
                    new File(gzXmlFile));

                // Output to console for testing
                // StreamResult resultStream = new StreamResult(System.out);

                transformer.transform(source, resultStream);
                log.info("Data written to " + gzXmlFile);
            }
            else
            {
                for(String s : suspectsLines)
                {
                    Map<String, String> suspectMap =
                        splitSuspectLineWithFormat(s, paramSortOrder);
                    if (log.isDebugEnabled())
                    {
                        log.debug("Class: " + suspectMap.get("class") +
                            " Method: "  + suspectMap.get("method") +
                            " Line: " + suspectMap.get("line")+
                            " Suspect: " + suspectMap.get("score"));
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    protected static Map<String, String> splitZoltarReportLine(
        String reportLine)
    {
        Map<String, String> reportItems = new HashMap<String, String>();

        //Get the class
        String[] splitResults = reportLine.split("\\{");
        reportItems.put("class", splitResults[0]);
        String[] splitResults2 = splitResults[1].split("\\[");
        reportItems.put("line", splitResults2[1]);
        String[] splitResults3 = splitResults2[0].split("\\)");
        reportItems.put("method", splitResults3[0] + ")");

        return reportItems;
    }

    protected static Map<String, String> splitZoltarReportLineWithoutSize(
        String reportLine)
    {
        Map<String, String> reportItems = new HashMap<String, String>();

        //Get the class
        String[] splitResults = reportLine.split("\\{");
        reportItems.put("class", splitResults[0]);
        String[] splitResults2 = splitResults[1].split("\\)");
        reportItems.put("method", splitResults2[0]  + ")");

        return reportItems;
    }

    protected static Map<String, String> splitSuspectLine(String reportLine)
    {
        Map<String, String> reportItems = new HashMap<String, String>();

        //Get the class
        String[] splitResults = reportLine.split("\\|");
        reportItems.put("class", splitResults[0]);
        reportItems.put("method", splitResults[1]);
        reportItems.put("line", splitResults[2]);
        reportItems.put("score", splitResults[3]);

        return reportItems;
    }

    protected static Map<String, String> splitSuspectLineWithFormat(
        String reportLine, String format)
    {
        Map<String, String> reportItems = new HashMap<String, String>();
        String[] splitResults = reportLine.split("\\|");

        if ("CLMS".equals(format))
        {
            reportItems.put("class", splitResults[0]);
            reportItems.put("line", splitResults[1]);
            reportItems.put("method", splitResults[2]);
            reportItems.put("score", splitResults[3]);
        }
        else if ("CSLM".equals(format))
        {
            reportItems.put("class", splitResults[0]);
            reportItems.put("score", splitResults[1]);
            reportItems.put("line", splitResults[2]);
            reportItems.put("method", splitResults[3]);
        }
        else if ("CMLS".equals(format))
        {
            reportItems.put("class", splitResults[0]);
            reportItems.put("method", splitResults[1]);
            reportItems.put("line", splitResults[2]);
            reportItems.put("score", splitResults[3]);
        }
        else
        {
            throw new IllegalArgumentException("unknown format: " + format);
        }

        return reportItems;
    }

    private static Map<String, Double> sortByValue(
        Map<String, Double> unsortMap)
    {
        // 1. Convert Map to List of Map
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion
        //    order Map LinkedHashMap
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static <K, V> void printMap(Map<K, V> map)
    {
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            System.out.println("Key : " + entry.getKey()
                    + " Value : " + entry.getValue());
        }
    }

    public static <K, V> Map<String, Double> returnLastXFromMap(
        Map<String, Double> map, int lastX)
    {
        int mapSize = map.size();
        Map<String, Double> subMap = new LinkedHashMap<String, Double>();

        int inx = 1;
        for (Map.Entry<String, Double>entry : map.entrySet())
        {
            if (inx > mapSize - lastX)
            {
                subMap.put(entry.getKey(),  entry.getValue());
                //System.out.println("Inx: " +inx+ " Key : " + entry.getKey()
                //	+ " Value : " + entry.getValue());
            }
            inx++;
        }
        return subMap;
    }

    public static <K, V> void printLastXMap(Map<K, V> map, int lastX)
    {
        int mapSize = map.size();
        if (mapSize <= lastX)
        {
            for (Map.Entry<K, V> entry : map.entrySet())
            {
                log.debug("Key : " + entry.getKey()
                        + " Value : " + entry.getValue());
            }
        }
        else
        {
            int inx = 1;
            for (Map.Entry<K, V> entry : map.entrySet())
            {
                if (inx > mapSize - lastX)
                {
                    log.debug("Inx: " +inx+ " Key : " + entry.getKey()
                        + " Value : " + entry.getValue());
                }
                inx++;
            }
        }
    }

    private static Spectra execGzoltar(ArrayList<String> testList)
    {
        Spectra spectra = null;
        try
        {
            GZoltar gz = new GZoltar(gzStudentBinDir);
            log.info("Create new GZoltar instance..." + gzStudentBinDir);
            log.info("Adding classes to instrument...");

            File[] fileList = new File(gzStudentBinDir).listFiles();
            log.debug(fileList.length + " files found");

            for (File file : fileList)
            {
                String filename = file.getName();

                if (filename.indexOf("Test") == -1)
                {
                    // File is not a student test file
                    int dot = filename.lastIndexOf(".");

                    String name = filename.substring(0, dot);
                    gz.addClassToInstrument(name);
                    log.debug("...." + filename + " added");
                }
                else
                {
                    log.debug("...." + filename + " skipped");
                }
            }

            log.debug("Adding libraries...");
            ArrayList<String> classPaths = new ArrayList<String>();

            // Get the files in the lib directory and add any jar files.
//            File[] libList = new File(gzProjectLibDir).listFiles();
//            log.debug( libList.length + " files found");
//            for (File libfile : libList)
//            {
//                String libname = libfile.getName();
//
//                if (libname.indexOf("jar") != -1)
//                {
//                    //File is a jar file
//                    classPaths.add(gzProjectLibDir + libname);
//                    log.debug("...."+ gzProjectLibDir + libname + " added");
//                }
//                else
//                {
//                    log.debug("...."+ gzProjectLibDir + libname + " skipped");
//                }
//            }

            log.debug("building gzoltar student classpath");
            for (String libname :
                gzLibs.split(System.getProperty("path.separator")))
            {
                if (libname.endsWith("jar"))
                {
                    classPaths.add(libname);
                    log.debug("...." + libname + " added");
                }
                else
                {
                    log.debug("...." + libname + " skipped");
                }
            }

            log.debug("Adding Test directory...");
            log.debug(gzTestsDir);
            classPaths.add(gzTestsDir);

            if (log.isDebugEnabled())
            {
                log.debug("classPaths added:");
                for (String s : classPaths)
                {
                    log.debug("...." + s);
                }
            }

            gz.setClassPaths(classPaths);

            log.debug("Adding Test classes...");

            // File[] testList = new File(gzTestsDir).listFiles();

            log.debug(testList.size() + " files found");
            for (String testfile: testList)
            {
                gz.addTestToExecute(testfile);
                log.debug(testfile + " added");
            }

            /*
            if ( gzDebug ){
                System.out.println( testList.length + " files found");
            }
            for ( File testfile: testList ){
                String testname = testfile.getName();

                if ( testname.indexOf("Test") != -1 ){ //File is a test file
                    int dot = testname.lastIndexOf(".");

                    String name = testname.substring(0, dot);
                    gz.addTestToExecute(name);
                    if ( gzDebug ){
                        System.out.println("...."+ testname + " added");
                    }
                }else{
                    if ( gzDebug ){
                        System.out.println("...."+ testname + " skipped");
                    }

                }
            }
            */

            log.info("Running GZoltar...");
            gz.run();
            log.info("Getting Test Results...");
            spectra = gz.getSpectra();
        }
        catch (Exception e)
        {
            log.error(e);
        }
        return spectra;
    }
}
