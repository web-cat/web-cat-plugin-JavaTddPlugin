//package org.webcat.plugins.javatddplugin;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.ast.body.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

public class ZoltarHeatmap
{
    private static final String MATRIX_INCLUDED = "1";

    private static Logger log = Logger.getLogger(ZoltarHeatmap.class);

    private static String gzXmlFile = System.getProperty("gzoltar.data.dir");
    private static String gzHtmlFile =
            System.getProperty("gzoltar.html.output");
    private static String gzStudentDir =
            System.getProperty("gzoltar.source.dir");
    static int gzLimitToMaxMethod =
            Integer.parseInt(System.getProperty("gzoltar.method.limit", "3"));
    private static String gzHtmlHeader =
            System.getProperty("gzoltar.html.header");
    private static String gzHtmlTitle =
            System.getProperty("gzoltar.html.title", "Feedback for your submission");
    private static String gzHtmlFooter =
            System.getProperty("gzoltar.html.footer");
    private static int gzMaxFailedTests =
            Integer.parseInt(System.getProperty("gzoltar.failed.limit", "1"));

    static List<Integer> methodStartLocs = new ArrayList<Integer>();
    static List<Integer> methodEndLocs = new ArrayList<Integer>();
    static List<String> methodNames = new ArrayList<String>();
    static Map<String,Double> lineScores = new HashMap<String,Double>();
    static List<String> maxMethods = new ArrayList<String>();
    static List<String> suspiciousMethods = new ArrayList<String>();

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        log.setLevel(Level.INFO);

        DecimalFormat df = new DecimalFormat("#.#####");

        try
        {
            /**********************************************************/
            // Create writer for new file
            /**********************************************************/
            String line = "";
            log.info("Creating heatmap file: " + gzHtmlFile);
            PrintWriter writer = new PrintWriter(gzHtmlFile, "UTF-8");


            /**********************************************************/
            //Build array list for the tests result file file
            /**********************************************************/
            log.info("Reading test results file at " + gzXmlFile + "/sfl/txt/tests.csv");
            List<List<String>> testRecordsList = new ArrayList<>();
            try
            {
                //Create the file reader
                BufferedReader testResultsFileReader = new BufferedReader(new FileReader(gzXmlFile + "/sfl/txt/tests.csv"));

                //Read the file line by line
                while ((line = testResultsFileReader.readLine()) != null)
                {
                    //Get all tokens available in line
                    String[] tokens = line.split(",");
                    testRecordsList.add(Arrays.asList(tokens));

                    log.debug("tests.csv file contents:");
                    for(String token : tokens)
                    {
                        //Print all tokens
                        log.debug(token);
                    }
                }
                testResultsFileReader.close();
            }

            catch (Exception e) {
                e.printStackTrace();
            }

            //This loops the rows from the file, looking for the tests that have
            //FAIL in the result position (col 2 in the file). If the test was
            //failed, we add the position in the file, as well as the row info
            //to a temporary var, and then add the temp var to the final list of
            //failed tests. The number of failed tests is governed by gzMaxFailedTests
            //which is defaulted to 1, but can be overridden by a system property
            List<List<String>> failedTestsList = new ArrayList<>();
            int filePosition = -1;
            for (List testRecord : testRecordsList) {
                if ( testRecord.get(1).equals("FAIL")) {
                    if ( failedTestsList.size() < gzMaxFailedTests ) {
                        ArrayList<String> newFailedTestEntry = new ArrayList<String>();
                        newFailedTestEntry.add(Integer.toString(filePosition));
                        log.debug("FirstFailed Test: " + testRecord.get(0) + " at index position " + filePosition);
                        for( int inx=0; inx<testRecord.size(); inx++ ) {
                            newFailedTestEntry.add(testRecord.get(inx).toString());
                        }
                        failedTestsList.add(newFailedTestEntry);
                    }
                }
                filePosition += 1;
            }
            if (failedTestsList.size() > 0) {
                for (List failedTest : failedTestsList) {
                    log.info("Failed Test List Item: " + failedTest.get(1) + " at index position " + failedTest.get(0));
                    log.debug("---STACKTRACE---");
                    if (failedTest.size() == 6) {
                        log.debug(failedTest.get(4).toString() + failedTest.get(5).toString());
                    } else {
                        log.debug(failedTest.get(4));
                    }
                    log.debug("---END STACKTRACE---");
                }


                /**********************************************************/
                //Build array list for the Matrix file
                /**********************************************************/
                List<List<String>> matrixRecordsList = new ArrayList<>();
                try {
                    //Create the file reader
                    BufferedReader matrixFileReader = new BufferedReader(new FileReader(gzXmlFile + "/sfl/txt/matrix.txt"));

                    //Read the file line by line
                    while ((line = matrixFileReader.readLine()) != null) {
                        //Get all tokens available in line
                        String[] tokens = line.split(" ");
                        matrixRecordsList.add(Arrays.asList(tokens));

                    /*log.debug("Matrix file contents:");
                    for(String token : tokens)
                    {
                        //Print all tokens
                        log.debug(token);
                    }*/
                    }
                    matrixFileReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                log.info("Matrix file has " + Integer.toString(matrixRecordsList.size()) + " entries");


                int jnx = 0;
                for (List matrixRecord : matrixRecordsList) {
                    log.debug("Matrix Record: " + matrixRecord.get(1) + " at position " + Integer.toString(jnx));
                    log.debug("---MATRIX RECORD---");
                    for (int knx = 0; knx < matrixRecord.size(); knx++) {
                        log.debug("Position " + Integer.toString(knx) + ":" + matrixRecord.get(knx).toString());
                    }
                    log.debug("---END MATRIX RECORD---");
                    jnx++;
                }

                /**********************************************************/
                //Read the results line into a string array
                /**********************************************************/
                List<String> rankingRecordsList = new ArrayList<>();
                try {
                    //Create the file reader
                    BufferedReader rankingFileReader = new BufferedReader(new FileReader(gzXmlFile + "/sfl/txt/ochiai.ranking.csv"));

                    //Read the file line by line
                    while ((line = rankingFileReader.readLine()) != null) {
                        rankingRecordsList.add(line);
                    }
                    rankingFileReader.close();
                    log.debug("---Ranking file contents:---");
                    for (String rankingRecord : rankingRecordsList) {
                        //Print all tokens
                        log.debug(rankingRecord);
                    }
                    log.debug("---End Ranking file contents:---");
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ArrayList<String> maxMethodsList = new ArrayList<>();
                ArrayList<String> encounteredMethods = new ArrayList<String>();
                String lastClass = "";
                String lastMethod = "";
                for (int snx = 1; snx < rankingRecordsList.size(); snx++) {
                    String[] spectraTokens = rankingRecordsList.get(snx).toString().split("[(;#:]+");
                    for (String token : spectraTokens) {
                        //Print all tokens
                        log.debug(token);
                    }
                    if (maxMethodsList.size() < 3) {
                        if (!spectraTokens[0].equals(lastClass) && !spectraTokens[1].equals(lastMethod)) {
                            if (!encounteredMethods.contains(spectraTokens[0] + ":" + spectraTokens[1])) {
                                ArrayList<String> newMaxMethodEntry = new ArrayList<String>();
                                encounteredMethods.add(spectraTokens[0] + ":" + spectraTokens[1]);
                                maxMethodsList.add(spectraTokens[0] + ":" + spectraTokens[1]);
                                lastClass = spectraTokens[0];
                                lastMethod = spectraTokens[1];
                                //newMaxMethodEntry.add(spectraTokens[1]);
                                //newMaxMethodEntry.add(spectraTokens[2]);
                                //maxMethodsList.add(newMaxMethodEntry);
                            } else {
                                log.debug(spectraTokens[0] + ":" + spectraTokens[1] + " already in encounteredMethods");
                            }
                        } else {
                            log.debug(lastClass + " = " + spectraTokens[0] + " and " + lastMethod + " = " + spectraTokens[1] + "...skipping");

                        }
                    } else {
                        log.debug("maxMethodList size at limit: " + maxMethodsList.size());
                    }
                }

                log.debug("---MAX METHOD RECORDS---");
                for (String maxMethodRecord : maxMethodsList) {
                    //log.debug("Max method: " + maxMethodRecord.get(0) + ":" + maxMethodRecord.get(1));
                    log.debug("Max method: " + maxMethodRecord);
                }
                log.debug("---MAX METHOD RECORDS---");

                for (int snx = 1; snx < rankingRecordsList.size(); snx++) {
                    String[] spectraTokens = rankingRecordsList.get(snx).toString().split("[(;#:]+");
                    if (maxMethodsList.contains(spectraTokens[0] + ":" + spectraTokens[1])) {
                        log.debug("For " + spectraTokens[0] + ":" + spectraTokens[1] + " adding line number: " + spectraTokens[3]);
                        maxMethods.add(spectraTokens[0] + ":" + spectraTokens[3]);
                    } else {
                        log.debug(spectraTokens[0] + ":" + spectraTokens[1] + " not found...checking rank " + spectraTokens[4] + " to see if suspicious");
                        if (Float.parseFloat(spectraTokens[4]) > 0) {
                            suspiciousMethods.add(spectraTokens[0] + ":" + spectraTokens[3]);
                        } else {
                            log.debug(spectraTokens[0] + ":" + spectraTokens[1] + " not suspicious: " + spectraTokens[4]);
                        }
                    }
                }


                /**********************************************************/
                //Get the scores from the ochiai.ranking for the spectra line
                //if the matrix says the line was executed.
                /**********************************************************/
                List<List<String>> spectraRecordsList = new ArrayList<>();
                try {
                    //Create the file reader
                    BufferedReader spectraFileReader = new BufferedReader(new FileReader(gzXmlFile + "/sfl/txt/spectra.csv"));

                    //Read the file line by line
                    while ((line = spectraFileReader.readLine()) != null) {
                        for (String rankingRecord : rankingRecordsList) {
                            if (rankingRecord.contains(line) == true && !rankingRecord.equals("name;suspiciousness_value")) {
                                String[] rankingTokens = rankingRecord.split("[;#:]+");
                                spectraRecordsList.add(Arrays.asList(rankingTokens));
                                break;
                            }
                        }
                    }
                    spectraFileReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                log.debug("---SPECTRA RECORD---");
                for (List spectraRecord : spectraRecordsList) {
                    log.debug("Ranking for Class" + spectraRecord.get(0) + ": Method " + spectraRecord.get(1) + ": line " + spectraRecord.get(2) + ": " + spectraRecord.get(3));
                }
                log.debug("---END SPECTRA RECORD---");


                /**********************************************************/
                //At this point, we know which tests failed, the matrix for each
                //failed test, and the lines and rankings, ordered by the matrix
                //exec pattern.
                //
                //Now, we need to create a final list of ranked lines that only
                //include those lines that were part of the matrix for the failed
                //test(s). Find the matrix for each failed test, loop over the
                //ranked lines, and if the line was included in the matrix
                //(matrix line value = MATRIX_INCLUDED).
                /**********************************************************/
                List<List<String>> heatmapRecordsList = new ArrayList<>();
                for (List failedTest : failedTestsList) {
                    log.info("Failed Test List Item: " + failedTest.get(1) + " at index position " + failedTest.get(0));
                    List<String> matrixRecord = matrixRecordsList.get(Integer.parseInt(failedTest.get(0).toString()));
                    //Note: matrix record is marked at the end by a '+' for pass or
                    // a '-' for failed. This isi ncluded in the matrix record, but
                    // it makes the matrix rec 1 row longer than the spectra list.
                    // Account for that diff here by having matrixInx stop at
                    // matrixRecord.size()-1
                    for (int matrixInx = 0; matrixInx < matrixRecord.size() - 1; matrixInx++) {
                        if (matrixRecord.get(matrixInx).equals(MATRIX_INCLUDED)) {
                            if (Float.valueOf(spectraRecordsList.get(matrixInx).get(3).toString()) > 0.0) {
                                heatmapRecordsList.add(spectraRecordsList.get(matrixInx));
                                log.debug("Including Class" + spectraRecordsList.get(matrixInx).get(0) + ": Method " + spectraRecordsList.get(matrixInx).get(1) + ": line " + spectraRecordsList.get(matrixInx).get(2) + ": " + spectraRecordsList.get(matrixInx).get(3));
                            } else {
                                log.debug("Excluding - Zero Suspicion: Class" + spectraRecordsList.get(matrixInx).get(0) + ": Method " + spectraRecordsList.get(matrixInx).get(1) + ": line " + spectraRecordsList.get(matrixInx).get(2) + ": " + spectraRecordsList.get(matrixInx).get(3));
                            }
                        } else {
                            log.debug("Excluding - Not in Matrix:  Class" + spectraRecordsList.get(matrixInx).get(0) + ": Method " + spectraRecordsList.get(matrixInx).get(1) + ": line " + spectraRecordsList.get(matrixInx).get(2) + ": " + spectraRecordsList.get(matrixInx).get(3));
                        }
                    }
                }

                log.info(Integer.toString(heatmapRecordsList.size()) + " of " + Integer.toString(spectraRecordsList.size()) + " marked for inclusion in heat map.");

                log.debug("---HEAT MAP RECORD---");
                for (List heatmapRecord : heatmapRecordsList) {
                    log.debug("Heat map record - Class " + heatmapRecord.get(0) + ": Method " + heatmapRecord.get(1) + ": line " + heatmapRecord.get(2) + ": " + heatmapRecord.get(3));
                }
                log.debug("---END HEAT MAP RECORD---");
                /**********************************************************/
                //Loop through the list of project files
                /**********************************************************/
                log.info("Getting file list...");

                // We only want .java files, so filter them here.
                File[] fileList = new File(gzStudentDir).listFiles(
                        new FilenameFilter()
                        {
                            public boolean accept(File dir, String name)
                            {
                                return name.endsWith(".java");
                            }
                        });

                if (fileList.length == 0) {
                    // We didn't find any java files in the directory that was
                    // passed in, so lets see if we can find it in the ./src
                    // directory. This is the directory the webcat plugin uses.
                    log.info("Not found in gzStudentDir... "
                            + "look in <gzStudentDir>/src/...");

                    gzStudentDir = gzStudentDir + "/src/";
                    fileList = new File(gzStudentDir).listFiles(
                            new FilenameFilter()
                            {
                                public boolean accept(File dir, String name)
                                {
                                    return name.endsWith(".java");
                                }
                            });
                    if (fileList.length == 0) {
                        log.error("No class files found in <gzStudentDir> or "
                                + "<gzStudentDir>/src/");
                        log.error("Heat map file will be empty.");
                    } else {
                        log.info("Found " + fileList.length
                                + " class files in <gzStudentDir>/src/...");
                    }
                } else {
                    log.info("Found " + fileList.length
                            + " class files in gzStudentDir...");
                }
                log.debug("---FILES FOUND---");
                for (File file : fileList) {
                    log.debug(file.getName());
                }
                log.debug("---END FILES FOUND---");

                /**********************************************************/
                //Get HTML Header template and write it to the new file
                /**********************************************************/

                if (gzHtmlHeader != null)
                {
                    log.info("Reading in HTML header: " + gzHtmlHeader);

                    BufferedReader br = new BufferedReader(
                            new FileReader(gzHtmlHeader));
                    try
                    {
                        while ((line = br.readLine()) != null)
                        {
                            line = line.replace("[title]", gzHtmlTitle);
                            writer.println(line);
                        }
                    }
                    catch (NullPointerException ex)
                    {
                        log.error("Could not open " + gzHtmlHeader);
                        log.error(ex.toString());
                    }
                    finally
                    {
                        br.close();
                    }
                }else{
                    log.error("Path to HTML header not set.");
                }


                writer.println("<div class=\"firstFailedTestInfo\">");
                writer.println("<span class=\"dijitTitlePaneTextNode\">Failed lines " +
                        "associated with the " + failedTestsList.get(0).get(1) +
                        " instructor test.</span>");
                writer.println("</div>");

                /**********************************************************/
                //Loop through the files, building the heatmap for each
                /**********************************************************/
                log.info("Applying GZoltar results to source code...");

                for (File file : fileList) {

                    String filename = file.getName();
                    String className =
                            filename.substring(0, filename.lastIndexOf('.'));
                    if (filename.indexOf("Test") == -1) {
                        //File is not a student test file
                        log.info("+++ Processing file: " + filename);

                        /********************************************************/
                        // Get all of the heat map records for the current file
                        /********************************************************/

                        for (List heatmapRecord : heatmapRecordsList) {
                            if (heatmapRecord.get(0).equals("$" + className)) {
                                lineScores.put(heatmapRecord.get(2).toString(), Double.parseDouble(heatmapRecord.get(3).toString()));
                                log.debug("Class record - Class " + heatmapRecord.get(0) + ": Method " + heatmapRecord.get(1) + ": line " + heatmapRecord.get(2) + ": " + heatmapRecord.get(3));
                            } else {
                                log.debug("$" + className + " does not match " + heatmapRecord.get(0));
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("---CLASS LINES FOUND---");
                            printMap(lineScores);
                            log.debug("**************  Max Methods  **************");
                            log.debug(maxMethods);
                            log.debug("**************  Non-max Methods  **************");
                            log.debug(suspiciousMethods);
                            log.debug("---END CLASS LINES FOUND---");
                        }
                        /*******************************************************/
                        // Parse the current file to get the locations of
                        // constructors and methods
                        /*******************************************************/
                        if (lineScores.size() > 0) {
                            writer.println("<div class=\"file\">");
                            writer.println("<h2>" + filename + "</h2>");
                            writer.println("<pre class=\"heat\">");

                            double currScore = 0.0;
                            int identifiedMaxMethods = 0;

                            FileInputStream parserIn =
                                    new FileInputStream(new File(gzStudentDir, filename));
                            CompilationUnit cu;
                            try {
                                // parse the file
                                cu = JavaParser.parse(parserIn);
                            } finally {
                                parserIn.close();
                            }
                            new MethodVisitor().visit(cu, null);
                            new ConstructorVisitor().visit(cu, null);

                            Collections.sort(methodStartLocs);
                            Collections.sort(methodEndLocs);

                            if (log.isDebugEnabled()) {
                                log.debug("******Start of method locs******");
                                log.debug(methodStartLocs.toString());
                                log.debug("******End of method locs******");
                                log.debug(methodEndLocs.toString());
                                log.debug("******End of method locs******");
                                log.debug(methodNames.toString());
                            }


                            BufferedReader br = new BufferedReader(
                                    new FileReader(new File(gzStudentDir, filename)));
                            try {
                                int linenumber = 1;
                                while ((line = br.readLine()) != null) {
                                    // System.out.println("Pre  [" + linenumber +  "]" + line);
                                    // Replace < and > with the correseponding HTML entities
                                    line = line.replace("<", "&lt;");
                                    line = line.replace(">", "&gt;");

                                    String lineNo = Integer.toString(linenumber);
                                    if (lineScores.containsKey(lineNo)) {
                                        currScore = lineScores.get(lineNo);
                                        line = "<div class=\"tooltip\"><span id=\"" + filename + "_" + linenumber + "\" style=\"color:black; background-color: hsl("
                                                + getScoreColor(currScore)
                                                + ", 100%, 50%)\">" + line + "</span>"
                                                + "<span class=\"tooltiptext\">"
                                                + "Line:" + linenumber
                                                + "<br>Suspicion: " + df.format(currScore)
                                                + "</span>"
                                                + "</div>";


                                        log.debug("Found score:" + currScore
                                                + " at line: " + linenumber);
                                    }

                                    // See if the current line is the start or the end
                                    // of a method...
                                    if (methodStartLocs.indexOf(linenumber) != -1) {
                                        // Curr line has start of method
                                        Integer nextMethodLine = null;
                                        // See if the current method is a max suspicion
                                        // method
                                        if (methodStartLocs.indexOf(linenumber) + 1 <
                                                methodStartLocs.size()) {
                                            nextMethodLine = methodStartLocs.get(
                                                    methodStartLocs.indexOf(linenumber) + 1);
                                        } else {
                                            nextMethodLine = 1000;
                                        }
                                        log.debug("line: " + linenumber
                                                + " nextMethodLine " + nextMethodLine);

                                        String divClass = "nonmethod-suspect";
                                        for (int inx = linenumber; inx <= nextMethodLine;
                                             inx++) {
                                            if (maxMethods.contains("$"+className+":"+inx)) {
                                                divClass = "zoom method-suspect";
                                            } else {
                                                if (suspiciousMethods.contains("$"+className+":"+inx)) {
                                                    divClass = "zoom nonmethod-suspect";
                                                }
                                            }
                                        }

                                        line = "<div class=\"" + divClass + "\">" + line;
                                        log.debug("**********" + divClass + "***********");
                                    }
                                    if (methodEndLocs.indexOf(linenumber) != -1) {
                                        // Curr line has end of method
                                        line = line + "</div>";
                                    }

                                    writer.println(line);
                                    linenumber++;
                                }
                                linenumber = 1;
                                methodStartLocs.clear();
                                methodEndLocs.clear();
                                methodNames.clear();
                                lineScores.clear();
                            } finally {
                                br.close();
                            }
                            writer.println("</pre>");
                            writer.println("</div>");
                        }
                    }
                }
                /**********************************************************/
                // Get HTML footer template and write it to the new file
                /**********************************************************/
                if (gzHtmlFooter != null)
                {
                    log.info("Reading in HTML footer: " + gzHtmlHeader);

                    BufferedReader br =
                            new BufferedReader(new FileReader(gzHtmlFooter));
                    try
                    {
                        while ((line = br.readLine()) != null)
                        {
                            writer.println(line);
                        }
                    }
                    finally
                    {
                        br.close();
                    }
                    log.info("Completed heatmap file can be found at " + gzHtmlFile);
                }else{
                    log.error("Path to HTML footer not set.");
                }
            }else{
                log.info("No test cases identified by GZoltar.");
            }


            writer.close();
        }
        catch (Exception e)
        {
            log.error(e);
        }
    }

    protected static int getScoreColor(Double score)
    {
        Double scoreColor = 0.0;

        scoreColor = 70.0 - (score * 70.0);

        return scoreColor.intValue();

    }

    private static class MethodVisitor
            extends VoidVisitorAdapter<Object>
    {
        @Override
        public void visit(MethodDeclaration n, Object arg)
        {
            // here you can access the attributes of the method.
            // this method will be called for all methods in this
            // CompilationUnit, including inner class methods
            if (log.isDebugEnabled())
            {
                log.debug("From [" + n.getBeginLine() + "] to ["
                        + n.getEndLine() + "] is method " + n.getName());
            }
            methodStartLocs.add(n.getBeginLine());
            methodEndLocs.add(n.getEndLine());
            methodNames.add(n.getName());
        }
    }

    private static class ConstructorVisitor
            extends VoidVisitorAdapter<Object>
    {
        @Override
        public void visit(ConstructorDeclaration n, Object arg)
        {
            // here you can access the attributes of the method.
            // this method will be called for all methods in this
            // CompilationUnit, including inner class methods
            if (log.isDebugEnabled())
            {
                log.debug("From [" + n.getBeginLine() + "] to ["
                        + n.getEndLine() + "] is constructor " + n.getName());
            }
            methodStartLocs.add(n.getBeginLine());
            methodEndLocs.add(n.getEndLine());
            methodNames.add(n.getName());
        }
    }

    private static Map<String, Double> sortByValue(
            Map<String, Double> unsortMap)
    {
        // 1. Convert Map to List of Map
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
        {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order
        //    Map LinkedHashMap
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static <K, V> void printMap(Map<K, V> map)
    {
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            log.info("Key : " + entry.getKey()
                    + " Value : " + entry.getValue());
        }
    }


}