import java.io.*;
import java.util.*;
import java.sql.*;

import com.gzoltar.core.*;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.spectra.Spectra;
//import java.util.logging.*;
//import org.apache.log4j.Logger;
//import org.apache.log4j.BasicConfigurator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ZoltarTest {

    private static String gzAssignDesc = System.getenv("GZOLTAR_ASSIGN_DESC");
    private static String gzStudentPid = System.getenv("GZOLTAR_STUDENT_PID");
    private static String gzStudentDir = System.getenv("GZOLTAR_STUDENT_DIR");
    private static String gzStudentSubmission = System.getenv("GZOLTAR_STUDENT_SUBMISSION");
    private static String gzOutputDir = System.getenv("GZOLTAR_OUTPUT_DIR");
    private static String gzTestsDir = System.getenv("GZOLTAR_TESTS_DIR");

    private static String gzProjectLibDir = System.getenv("GZOLTAR_PROJECT_LIB_DIR");
    private static Boolean gzDebug = (System.getenv("GZOLTAR_DEBUG") != null ) ? true : false;
    private static Integer gzTestDepth = Integer.parseInt(System.getenv("GZOLTAR_TEST_DEPTH"));

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        String paramSortOrder = "CLMS";
        String paramOutput 	  = "xml";
        System.out.println("gzDebug: " + gzDebug.booleanValue());

        if ( gzDebug ){
            System.out.println("basePath: " + System.getProperty("user.dir"));
            System.out.println("gzAssignDesc: " + gzAssignDesc);
            System.out.println("gzStudentPid: " + gzStudentPid);
            System.out.println("gzStudentDir: " + gzStudentDir);
            System.out.println("gzStudentSubmission: " + gzStudentSubmission);
            System.out.println("gzOutputDir: " + gzOutputDir);
            System.out.println("gzTestsDir: " + gzTestsDir);
            System.out.println("gzProjectLibDir: " + gzProjectLibDir);
            System.out.println("gzTestDepth: " + gzTestDepth.toString());

            System.out.println("Sort: " + paramSortOrder);
            System.out.println("Output: " + paramOutput);
            System.out.println("Project: " + gzStudentPid);
        }

        try {
            ArrayList<String> allTestNames = new ArrayList<String>();
            ArrayList<String> failedTestNames = new ArrayList<String>();
            File[] testList = new File(gzTestsDir).listFiles();

            if ( gzDebug ){
                System.out.println( testList.length + " files found");
            }
            for ( File testfile: testList ){
                String testname = testfile.getName();

                if ( testname.indexOf("Test") != -1 ){ //File is a test file
                    int dot = testname.lastIndexOf(".");
                    String name = testname.substring(0, dot);
                    allTestNames.add(name);
                    if ( gzDebug ){
                        System.out.println("...."+ testname + " added");
                    }
                }else{
                    if ( gzDebug ){
                        System.out.println("...."+ testname + " skipped");
                    }

                }
            }

            Spectra spectra = execGzoltar(allTestNames);

            Thread.sleep(4000);

            Spectra failedOnlySpectra = null;
            List suspectStatesAllTests = null;
            List suspectStatesFailedOnly = null;


            //if gzTestDepth is non-zero, return the suspiciousness for the first gzTestDepth-th failed cases
            //if gzTestDepth is zero, return results for all failed cases
            int failedTests = 0;

            if (gzTestDepth > 0){
                if (gzDebug ) {
                    System.out.println("Getting Suspicious Results from first " + gzTestDepth + " failed tests...");
                }

                List results = spectra.getTestResults();
                for( Iterator<TestResult> it = results.iterator(); it.hasNext();){
                    TestResult result = it.next();

                    if ( !result.wasSuccessful() && failedTests < gzTestDepth ){
                            String testname = result.getName();
                            int dot = testname.lastIndexOf("#");
                            String name = testname.substring(0, dot);
                            failedTestNames.add(name);
                        if (gzDebug ) {
                            System.out.println(result.getName() + "("+name+")...FAILED!");
                            //System.out.println(result.getCoveredComponents().toString());
                        }
                        failedTests++;
                    }else{
                        if (gzDebug ) {
                            System.out.println(result.getName() + "...passed");
                        }
                    }
                }
                if (gzDebug ) {
                    System.out.println("failedTest Names :");
                    for (String s : failedTestNames) {
                        System.out.println("...." + s);
                    }
                }
                if ( failedTestNames.size()>0){
                    //rerun zoltar for the failed cases only.
                    failedOnlySpectra = execGzoltar(failedTestNames);
                }
            }

            suspectStatesAllTests = spectra.getComponentsBySuspiciousness();
            if (failedOnlySpectra != null ) {
                suspectStatesFailedOnly = failedOnlySpectra.getComponentsBySuspiciousness();
                List failedOnlyResults = failedOnlySpectra.getTestResults();
                for( Iterator<TestResult> it = failedOnlyResults.iterator(); it.hasNext();){
                    TestResult result = it.next();
                    if ( !result.wasSuccessful()){
                        if (gzDebug ) {
                            System.out.println(result.getName() +"...FAILED!");

                        }
                    }else{
                        if (gzDebug ) {
                            System.out.println(result.getName() + "...passed");
                        }
                    }
                }
            }else{

            }

            
            System.out.println("");
            System.out.println("");

            List suspectsLines = new ArrayList<String>();
            List failedOnlySuspectsLines = new ArrayList<String>();
            String encodedLine = "";

            Map<String,Double> maxMethods = new HashMap<String,Double>();
            Map<String,Double> sortedMaxMethods = new HashMap<String,Double>();
            Map<String,Double> mostSuspectMethods = new HashMap<String,Double>();
            Map<String,Double> fullSuspectMap = new HashMap<String,Double>();

            if ( !suspectStatesAllTests.isEmpty() ){

                for( Iterator<Statement> it = suspectStatesAllTests.iterator(); it.hasNext();){
                    Statement statement = it.next();
                    fullSuspectMap.put(statement.getLabel(), statement.getSuspiciousness());
                    if ( statement.getSuspiciousness() != 0.0) {

                        Map splitResults = splitZoltarReportLineWithoutSize(statement.getLabel());
                        String currMethodSig = splitResults.get("class").toString() + "|" + splitResults.get("method").toString();

                        if ( maxMethods.containsKey(currMethodSig)){
                            if ( maxMethods.get(currMethodSig) < statement.getSuspiciousness() ){
                                maxMethods.put(currMethodSig, statement.getSuspiciousness());
                            }
                        }else{
                            maxMethods.put(currMethodSig, statement.getSuspiciousness());
                        }
                    }
                }

                if (gzDebug){
                    printMap(sortByValue(fullSuspectMap));
                }

                if ( suspectStatesFailedOnly != null ) {
                    for (Iterator<Statement> it = suspectStatesFailedOnly.iterator(); it.hasNext(); ) {
                        Statement statement = it.next();
                        if (statement.getSuspiciousness() != 0.0) {
                            System.out.println(statement.getLabel() + ' ' + statement.getSuspiciousness());


                            Map splitResults = splitZoltarReportLineWithoutSize(statement.getLabel());
 /*                       String currMethodSig = splitResults.get("class").toString() + "|" + splitResults.get("method").toString();


                        if ( maxMethods.containsKey(currMethodSig)){
                            if ( maxMethods.get(currMethodSig) < statement.getSuspiciousness() ){
                                maxMethods.put(currMethodSig, statement.getSuspiciousness());
                            }
                        }else{
                            maxMethods.put(currMethodSig, statement.getSuspiciousness());
                        }
*/


                            switch (paramSortOrder) {
                                case "CLMS":
                                    encodedLine = splitResults.get("class").toString() + "|" + String.valueOf(statement.getLineNumber()) + "|" + splitResults.get("method").toString() + "|" + String.valueOf(fullSuspectMap.get(statement.getLabel()));
                                    break;
                                case "CSLM":
                                    encodedLine = splitResults.get("class").toString() + "|" + String.valueOf(fullSuspectMap.get(statement.getLabel())) + "|" + String.valueOf(statement.getLineNumber()) + "|" + splitResults.get("method").toString();
                                    break;
                                case "CMLS":
                                default:
                                    encodedLine = splitResults.get("class").toString() + "|" + splitResults.get("method").toString() + "|" + String.valueOf(statement.getLineNumber()) + "|" + String.valueOf(fullSuspectMap.get(statement.getLabel()));
                                    break;
                            }
                            suspectsLines.add(encodedLine);
                        }

                    }
                }else{
                    System.out.println("No suspicious lines found for failed tests");
                }
                if ( gzDebug ){
                    System.out.println("..." + suspectsLines.size() + " suspicious lines found");
                }
            }else{
                if ( gzDebug ){
                    System.out.println("... 0 (zero) suspicious lines found");
                }
            }

            sortedMaxMethods = sortByValue(maxMethods);

            //@TODO remote the lastX value here
            mostSuspectMethods = returnLastXFromMap(sortedMaxMethods, 3);
            if ( gzDebug ) {
                System.out.println("Most suspicious methods...");
                printMap(mostSuspectMethods);
            }

            System.out.println("Writing to file (" +paramOutput+ ")...");
            Collections.sort(suspectsLines);

            if (paramOutput.equals("csv")){
                FileWriter writer = new FileWriter(gzOutputDir + gzStudentPid +"_"+ gzAssignDesc + ".csv", true);
                for( Object s : suspectsLines ) {
                    Map suspectMap = splitSuspectLineWithFormat(s.toString(), paramSortOrder);
                    writer.append(gzStudentPid);
                    writer.append(',');
                    writer.append(suspectMap.get("class").toString());
                    writer.append(',');
                    writer.append(suspectMap.get("method").toString());
                    writer.append(',');
                    writer.append(suspectMap.get("line").toString());
                    writer.append(',');
                    writer.append(suspectMap.get("score").toString());
                    writer.append('\n');
                    System.out.println("Class: " + suspectMap.get("class") +
                            " Method: "  + suspectMap.get("method") +
                            " Line: " + suspectMap.get("line")+
                            " Suspect: " + suspectMap.get("score"));
                }
                writer.flush();
                writer.close();
            }else if (paramOutput.equals("xml")){
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
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                // root elements
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("gzoltar");
                doc.appendChild(rootElement);

                if (suspectsLines.size() > 0 ){
                    String lastClass = "";
                    Element suspectsElement = null;
                    String mostSuspectFlag = "no";
                    for( Object s : suspectsLines ) {

                        Map suspectMap = splitSuspectLineWithFormat(s.toString(), paramSortOrder);

                        System.out.println("Class: " + suspectMap.get("class") +
                        		" Method: "  + suspectMap.get("method") +
                        		" Line: " + suspectMap.get("line")+
                        		" Suspect: " + suspectMap.get("score"));


                        // Get the name of the current class.
                        String currentClass = suspectMap.get("class").toString();
                        String classname = "";
                        //System.out.println("currentClass: " + suspectMap.get("class").toString());

                        //Is this one of the maxSuspect methods:
                        //System.out.println("looking for " + suspectMap.get("class").toString() + "|" + suspectMap.get("method"));
                        if (  mostSuspectMethods.containsKey(suspectMap.get("class").toString()+ "|" + suspectMap.get("method"))){
                            mostSuspectFlag = "yes";
                            //System.out.println("...FOUND...");
                        }else{
                            mostSuspectFlag = "no";
                        }
                        // If it is a private helper class (ie LinkedQueue$Node), we really care about the
                        // base class name. Also the $ is not allowed as an XML node name. So we test to see
                        // if we get the $, and if we do, get the string in front of $ as the class name.
                        if ( currentClass.indexOf("$") != -1 ){ //is a class within a class
                            int dot = currentClass.lastIndexOf("$");

                            classname = currentClass.substring(0, dot);
                        }else{
                            classname = currentClass;
                        }

                        //System.out.println("classname: " + classname);

                        // Now, see if the class is the same as the last one we populated
                        if (!lastClass.equals(classname) ){
                            //System.out.println("Creating new class node. Was: " + lastClass +" now: " + classname);
                            //new class, so update the lastClass flag, append the current class node to root
                            // and create a new class node for the new class to be processed.
                            if (!lastClass.equals("")){
                                rootElement.appendChild(suspectsElement);
                            }
                            //suspectsElement = null;
                            lastClass = classname;
                            suspectsElement = doc.createElement(classname);
                        }else{
                            //System.out.println("Using same class as before: " + lastClass);
                        }

                        Element suspiciousStatementElement = doc.createElement("suspect");

                        //Element suspiciousStatementClassElement = doc.createElement("class");

                        Element suspiciousStatementMethodElement = doc.createElement("method");
                        Element suspiciousStatementLineElement = doc.createElement("line");
                        Element suspiciousStatementScoreElement = doc.createElement("score");
                        Element suspiciousStatementMostSuspectElement = doc.createElement("most");

                        //suspiciousStatementClassElement.appendChild(doc.createTextNode(suspectMap.get("class").toString()));

                        suspiciousStatementMethodElement.appendChild(doc.createTextNode(suspectMap.get("method").toString()));
                        suspiciousStatementLineElement.appendChild(doc.createTextNode(suspectMap.get("line").toString()));
                        suspiciousStatementScoreElement.appendChild(doc.createTextNode(suspectMap.get("score").toString()));
                        suspiciousStatementMostSuspectElement.appendChild(doc.createTextNode(mostSuspectFlag));

                        //suspiciousStatementElement.appendChild(suspiciousStatementClassElement);
                        suspiciousStatementElement.appendChild(suspiciousStatementMethodElement);
                        suspiciousStatementElement.appendChild(suspiciousStatementLineElement);
                        suspiciousStatementElement.appendChild(suspiciousStatementScoreElement);
                        suspiciousStatementElement.appendChild(suspiciousStatementMostSuspectElement);

                        suspectsElement.appendChild(suspiciousStatementElement);

                    }
                    //Need to have this here so that the last class node is added to the root node.
                    rootElement.appendChild(suspectsElement);
                }

                // write the content into xml file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                DOMSource source = new DOMSource(doc);
                StreamResult resultStream = new StreamResult(new File(gzOutputDir + gzStudentPid +"_"+ gzAssignDesc + "_" + gzStudentSubmission + ".xml"));

                // Output to console for testing
                //StreamResult resultStream = new StreamResult(System.out);

                transformer.transform(source, resultStream);
                System.out.println("Data written to " + gzOutputDir + gzStudentPid +"_"+ gzAssignDesc + ".xml");
            }else{
                for( Object s : suspectsLines ) {
                    Map suspectMap = splitSuspectLineWithFormat(s.toString(), paramSortOrder);
                    System.out.println("Class: " + suspectMap.get("class") +
                            " Method: "  + suspectMap.get("method") +
                            " Line: " + suspectMap.get("line")+
                            " Suspect: " + suspectMap.get("score"));
                }
            }

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    protected static Map splitZoltarReportLine(String reportLine) {
        Map reportItems = new HashMap();

        //Get the class
        String[] splitResults = reportLine.split("\\{");
        reportItems.put("class", splitResults[0]);
        String[] splitResults2 = splitResults[1].split("\\[");
        reportItems.put("line", splitResults2[1]);
        String[] splitResults3 = splitResults2[0].split("\\)");
        reportItems.put("method", splitResults3[0] + ")");

        return reportItems;
    }

    protected static Map splitZoltarReportLineWithoutSize(String reportLine) {
        Map reportItems = new HashMap();

        //Get the class
        String[] splitResults = reportLine.split("\\{");
        reportItems.put("class", splitResults[0]);
        String[] splitResults2 = splitResults[1].split("\\)");
        reportItems.put("method", splitResults2[0]  + ")");

        return reportItems;
    }

    protected static Map splitSuspectLine(String reportLine) {
        Map reportItems = new HashMap();

        //Get the class
        String[] splitResults = reportLine.split("\\|");
        reportItems.put("class", splitResults[0]);
        reportItems.put("method", splitResults[1]);
        reportItems.put("line", splitResults[2]);
        reportItems.put("score", splitResults[3]);

        return reportItems;
    }

    protected static Map splitSuspectLineWithFormat(String reportLine, String format) {
        Map reportItems = new HashMap();
        String[] splitResults = reportLine.split("\\|");

        switch(format){
            case "CLMS":
                reportItems.put("class", splitResults[0]);
                reportItems.put("line", splitResults[1]);
                reportItems.put("method", splitResults[2]);
                reportItems.put("score", splitResults[3]);
                break;
            case "CSLM":
                reportItems.put("class", splitResults[0]);
                reportItems.put("score", splitResults[1]);
                reportItems.put("line", splitResults[2]);
                reportItems.put("method", splitResults[3]);
                break;
            case "CMLS":
            default:
                reportItems.put("class", splitResults[0]);
                reportItems.put("method", splitResults[1]);
                reportItems.put("line", splitResults[2]);
                reportItems.put("score", splitResults[3]);
                break;
        }

        return reportItems;
    }

    private static Map<String, Double> sortByValue(Map<String, Double> unsortMap) {

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

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        /*
        //classic iterator example
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }*/


        return sortedMap;
    }

    public static <K, V> void printMap(Map<K, V> map) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.println("Key : " + entry.getKey()
                    + " Value : " + entry.getValue());
        }
    }

    public static <K, V> Map<String, Double> returnLastXFromMap(Map<String, Double> map, int lastX) {
        int mapSize = map.size();
        Map<String, Double> subMap = new LinkedHashMap<String, Double>();

        int inx = 1;
        for (Map.Entry<String, Double>entry : map.entrySet()) {
            if ( inx > mapSize - lastX  ){

                subMap.put(entry.getKey(),  entry.getValue());
                //System.out.println("Inx: " +inx+ " Key : " + entry.getKey()
                //	+ " Value : " + entry.getValue());
            }
            inx++;
        }
        return subMap;
    }

    public static <K, V> void printLastXMap(Map<K, V> map, int lastX) {
        int mapSize = map.size();
        if ( mapSize <= lastX ){
            for (Map.Entry<K, V> entry : map.entrySet()) {
                System.out.println("Key : " + entry.getKey()
                        + " Value : " + entry.getValue());
            }
        }else{
            int inx = 1;
            for (Map.Entry<K, V> entry : map.entrySet()) {
                if ( inx > mapSize - lastX  ){
                    System.out.println("Inx: " +inx+ " Key : " + entry.getKey()
                            + " Value : " + entry.getValue());
                }
                inx++;
            }
        }
    }

    private static Spectra execGzoltar(ArrayList<String> testList) {
        Spectra spectra = null;
        try{
            GZoltar gz = new GZoltar( gzStudentDir );
            if ( gzDebug ){
                System.out.println("Create new GZoltar instance..." + gzStudentDir);
            }


            if ( gzDebug ){
                System.out.println("Adding classes to instrument...");
            }

            File[] fileList = new File(gzStudentDir).listFiles();
            if ( gzDebug ){
                System.out.println( fileList.length + " files found");
            }

            for ( File file: fileList ){
                String filename = file.getName();

                if ( filename.indexOf("Test") == -1 ){ //File is not a student test file
                    int dot = filename.lastIndexOf(".");

                    String name = filename.substring(0, dot);
                    gz.addClassToInstrument(name);
                    if ( gzDebug ){
                        System.out.println("...."+ filename + " added");
                    }
                }else{
                    if ( gzDebug ){
                        System.out.println("...."+ filename + " skipped");
                    }

                }
            }
            if ( gzDebug ){
                System.out.println("Adding libraries...");
            }
            ArrayList<String> classPaths = new ArrayList<String>();

            //Get the files in the lib directory and add any jar files.
            File[] libList = new File(gzProjectLibDir).listFiles();
            if ( gzDebug ){
                System.out.println( libList.length + " files found");
            }
            for ( File libfile: libList ){
                String libname = libfile.getName();

                if ( libname.indexOf("jar") != -1 ){ //File is a jar file

                    classPaths.add(gzProjectLibDir + libname);
                    if ( gzDebug ){
                        System.out.println("...."+ gzProjectLibDir + libname + " added");
                    }
                }else{
                    if ( gzDebug ){
                        System.out.println("...."+ gzProjectLibDir + libname + " skipped");
                    }

                }
            }
            if ( gzDebug ){
                System.out.println("Adding Test directory...");
                System.out.println(gzTestsDir);
            }
            classPaths.add(gzTestsDir);


            if ( gzDebug ){
                System.out.println("classPaths added:");
                for (String s : classPaths)
                {
                    System.out.println("...."+ s);
                }
            }


            gz.setClassPaths(classPaths);

            if ( gzDebug ){
                System.out.println("Adding Test classes...");
            }

            //File[] testList = new File(gzTestsDir).listFiles();

            if ( gzDebug ){
                System.out.println( testList.size() + " files found");
            }
            for( String testfile: testList){
                gz.addTestToExecute(testfile);
                if ( gzDebug ){
                    System.out.println( testfile + " added");
                }
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

            System.out.println("Running GZoltar...");
            gz.run();
            System.out.println("Getting Test Results...");
            spectra = gz.getSpectra();

        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return spectra;
    }
}
