package org.webcat.plugins.javatddplugin;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.spectra.Spectra;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ZoltarTest
{
    static Logger log = Logger.getLogger(ZoltarTest.class);
    static String basePath = System.getProperty("user.dir");
    static String gzXmlFile = System.getProperty("gzoltar.xml.output");
    static String gzCsvFile = System.getProperty("gzoltar.csv.output");
    static String gzStudentPid = System.getProperty("gzoltar.student.pid");
    static String gzStudentBinDir = System.getProperty("gzoltar.student.bin");
    static String gzTestsDir = System.getProperty("gzoltar.tests.dir");
    static String gzLibs = System.getProperty("gzoltar.libs");

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
            log.info("Create new GZoltar instance..." + gzStudentBinDir);
            GZoltar gz = new GZoltar(gzStudentBinDir);
            log.info("Adding classes to instrument...");

            addClassesToInstrument(gz, new File(gzStudentBinDir));

            log.info("Adding libraries...");
            ArrayList<String> classPaths = new ArrayList();

            log.debug("building gzoltar student classpath");
            for (String libname : gzLibs.split(System.getProperty("path.separator")))
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

            log.info("Adding Test classes...");

            File[] testList = new File(gzTestsDir).listFiles();
            log.info(testList.length + " files found");
            for (File testfile : testList)
            {
                String testname = testfile.getName();
                int dot = testname.lastIndexOf(".");
                if (dot > 0) {
                    testname = testname.substring(0, dot);
                }
                if ((!testname.contains("$")) && (
                    (testname.endsWith("Test")) ||
                    (testname.endsWith("Tests"))))
                {
                    gz.addTestToExecute(testname);
                    log.debug("...." + testname + " added");
                }
                else
                {
                    log.debug("...." + testname + " skipped");
                }
            }
            log.info("Run GZoltar...");
            gz.run();
            Spectra spectra = gz.getSpectra();
            log.info("Get Test Results...");

            List<TestResult> results = spectra.getTestResults();

            log.info("Get Suspicious Statements To Map...");
            List<String> suspectsLines = new ArrayList<String>();
            String encodedLine = "";

            List<Component> suspectStates2 =
                spectra.getComponentsBySuspiciousness();
            Map<String, Double> maxMethods = new HashMap<String, Double>();
            Map<String, Double> sortedMaxMethods =
                new HashMap<String, Double>();
            Map<String, Double> mostSuspectMethods =
                new HashMap<String, Double>();
            Map<String, String> splitResults;
            if (!suspectStates2.isEmpty())
            {
                for (Component component : suspectStates2)
                {
                    Statement statement = (Statement)component;
                    if (statement.getSuspiciousness() != 0.0D)
                    {
                        log.debug(statement.getLabel() + statement.getSuspiciousness());
                        splitResults = splitZoltarReportLineWithoutSize(statement.getLabel());
                        String currMethodSig = splitResults.get("class") + "|" + splitResults.get("method");
                        if (maxMethods.containsKey(currMethodSig))
                        {
                            if (maxMethods.get(currMethodSig).doubleValue() < statement.getSuspiciousness())
                            {
                                maxMethods.put(currMethodSig, Double.valueOf(statement.getSuspiciousness()));
                            }
                        }
                        else
                        {
                            maxMethods.put(currMethodSig, Double.valueOf(statement.getSuspiciousness()));
                        }
                        if ("CLMS".equals(paramSortOrder))
                        {
                            encodedLine = splitResults.get("class") + "|" + statement.getLineNumber() + "|" + splitResults.get("method") + "|" + statement.getSuspiciousness();
                        }
                        else if ("CSLM".equals(paramSortOrder))
                        {
                            encodedLine = splitResults.get("class") + "|" + statement.getSuspiciousness() + "|" + statement.getLineNumber() + "|" + splitResults.get("method");
                        }
                        else
                        {
                            encodedLine = splitResults.get("class") + "|" + splitResults.get("method") + "|" + statement.getLineNumber() + "|" + statement.getSuspiciousness();
                        }
                        suspectsLines.add(encodedLine);
                    }
                }
                log.info("..." + suspectsLines.size() + " suspicious lines found");
            }
            else
            {
                log.info("... 0 (zero) suspicious lines found");
            }
            sortedMaxMethods = sortByValue(maxMethods);

            mostSuspectMethods = returnLastXFromMap(sortedMaxMethods, 3);

            System.out.println("Most suspicious methods...");
            printMap(mostSuspectMethods);

            Collections.sort(suspectsLines);
            if ((gzCsvFile != null) && (!gzCsvFile.isEmpty()))
            {
                log.info("Writing to file (" + gzCsvFile + ")...");
                FileWriter writer = new FileWriter(gzCsvFile, true);
                for (String s : suspectsLines)
                {
                    Map<String, String> suspectMap = splitSuspectLineWithFormat(s, paramSortOrder);
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
                            " Method: " + suspectMap.get("method") +
                            " Line: " + suspectMap.get("line") +
                            " Suspect: " + suspectMap.get("score"));
                    }
                }
                writer.flush();
                writer.close();
            }
            DocumentBuilder docBuilder;
            if ((gzXmlFile != null) && (!gzXmlFile.isEmpty()))
            {
                log.info("Writing to file (" + gzXmlFile + ")...");

                DocumentBuilderFactory docFactory =
                    DocumentBuilderFactory.newInstance();
                docBuilder = docFactory.newDocumentBuilder();

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
                                " Method: " + suspectMap.get("method") +
                                " Line: " + suspectMap.get("line") +
                                " Suspect: " + suspectMap.get("score"));
                        }
                        String currentClass = suspectMap.get("class");
                        String classname = "";
                        log.debug("currentClass: " + suspectMap.get("class"));
                        if (mostSuspectMethods.containsKey(suspectMap.get("class") + "|" + suspectMap.get("method")))
                        {
                            mostSuspectFlag = "yes";
                        }
                        else
                        {
                            mostSuspectFlag = "no";
                        }
                        if (currentClass.indexOf("$") != -1)
                        {
                            int dot = currentClass.lastIndexOf("$");

                            classname = currentClass.substring(0, dot);
                        }
                        else
                        {
                            classname = currentClass;
                        }
                        log.debug("classname: " + classname);
                        if (!lastClass.equals(classname))
                        {
                            log.debug("Creating new class node. Was: " + lastClass + " now: " + classname);
                            if (!lastClass.equals(""))
                            {
                                rootElement.appendChild(suspectsElement);
                            }
                            lastClass = classname;
                            suspectsElement = doc.createElement(classname);
                        }
                        else
                        {
                            log.debug("Using same class as before: " + lastClass);
                        }
                        Element suspiciousStatementElement =
                            doc.createElement("suspect");

                        Element suspiciousStatementMethodElement =
                            doc.createElement("method");
                        Element suspiciousStatementLineElement =
                            doc.createElement("line");
                        Element suspiciousStatementScoreElement =
                            doc.createElement("score");
                        Element suspiciousStatementMostSuspectElement =
                            doc.createElement("most");

                        suspiciousStatementMethodElement.appendChild(doc.createTextNode(suspectMap.get("method")));
                        suspiciousStatementLineElement.appendChild(doc.createTextNode(suspectMap.get("line")));
                        suspiciousStatementScoreElement.appendChild(doc.createTextNode(suspectMap.get("score")));
                        suspiciousStatementMostSuspectElement.appendChild(doc.createTextNode(mostSuspectFlag));

                        suspiciousStatementElement.appendChild(suspiciousStatementMethodElement);
                        suspiciousStatementElement.appendChild(suspiciousStatementLineElement);
                        suspiciousStatementElement.appendChild(suspiciousStatementScoreElement);
                        suspiciousStatementElement.appendChild(suspiciousStatementMostSuspectElement);

                        suspectsElement.appendChild(suspiciousStatementElement);
                    }
                    rootElement.appendChild(suspectsElement);
                }
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty("indent", "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                DOMSource source = new DOMSource(doc);
                StreamResult resultStream = new StreamResult(new File(gzXmlFile));

                transformer.transform(source, resultStream);
                log.info("Data written to " + gzXmlFile);
            }
            else
            {
                for (String s : suspectsLines)
                {
                    Map<String, String> suspectMap = splitSuspectLineWithFormat(s, paramSortOrder);
                    if (log.isDebugEnabled())
                    {
                        log.debug("Class: " + suspectMap.get("class") +
                            " Method: " + suspectMap.get("method") +
                            " Line: " + suspectMap.get("line") +
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

    protected static void addClassesToInstrument(GZoltar gz, File dir)
    {
        for (File file : dir.listFiles())
        {
            if (file.isDirectory())
            {
                addClassesToInstrument(gz, file);
            }
            else
            {
                String filename = file.getName();
                if (filename.indexOf("Test") == -1)
                {
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
        }
    }

    protected static Map<String, String> splitZoltarReportLine(
        String reportLine)
    {
        Map<String, String> reportItems = new HashMap<String, String>();

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

        String[] splitResults = reportLine.split("\\{");
        reportItems.put("class", splitResults[0]);
        String[] splitResults2 = splitResults[1].split("\\)");
        reportItems.put("method", splitResults2[0] + ")");

        return reportItems;
    }

    protected static Map<String, String> splitSuspectLine(String reportLine)
    {
        Map<String, String> reportItems = new HashMap<String, String>();

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
        Map<String, String> reportItems = new HashMap();
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
        else
        {
            reportItems.put("class", splitResults[0]);
            reportItems.put("method", splitResults[1]);
            reportItems.put("line", splitResults[2]);
            reportItems.put("score", splitResults[3]);
        }
        return reportItems;
        }

    private static Map<String, Double> sortByValue(
        Map<String, Double> unsortMap)
    {
        List<Map.Entry<String, Double>> list =
            new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
            {
            public int compare(
                Map.Entry<String, Double> o1, Map.Entry<String, Double> o2)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
            });
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
            System.out.println("Key : " + entry.getKey() +
                " Value : " + entry.getValue());
        }
    }

    public static <K, V> Map<String, Double> returnLastXFromMap(
        Map<String, Double> map, int lastX)
        {
        int mapSize = map.size();
        Map<String, Double> subMap = new LinkedHashMap<String, Double>();

        int inx = 1;
        for (Map.Entry<String, Double> entry : map.entrySet())
        {
            if (inx > mapSize - lastX)
            {
                subMap.put(entry.getKey(), entry.getValue());
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
                System.out.println("Key : " + entry.getKey() +
                    " Value : " + entry.getValue());
            }
        }
        else
        {
            int inx = 1;
            for (Map.Entry<K, V> entry : map.entrySet())
            {
                if (inx > mapSize - lastX)
                {
                    System.out.println("Inx: " + inx
                        + " Key : " + entry.getKey()
                        + " Value : " + entry.getValue());
                }
                inx++;
            }
        }
    }
}
