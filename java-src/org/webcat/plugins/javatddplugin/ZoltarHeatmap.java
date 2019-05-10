package org.webcat.plugins.javatddplugin;

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
    private static Logger log = Logger.getLogger(ZoltarHeatmap.class);

    private static String gzXmlFile = System.getProperty("gzoltar.xml.input");
    private static String gzHtmlFile =
        System.getProperty("gzoltar.html.output");
    private static String gzStudentDir =
        System.getProperty("gzoltar.source.dir");
    static int gzLimitToMaxMethod =
        Integer.parseInt(System.getProperty("gzoltar.method.limit", "3"));
    private static String gzHtmlHeader =
        System.getProperty("gzoltar.html.header");
    private static String gzHtmlTitle =
        System.getProperty("gzoltar.html.title");
    private static String gzHtmlFooter =
        System.getProperty("gzoltar.html.footer");

    static List<Integer> methodStartLocs = new ArrayList<Integer>();
    static List<Integer> methodEndLocs = new ArrayList<Integer>();
    static Map<String,Double> lineScores = new HashMap<String,Double>();

    public static void main(String[] args)
    {
        BasicConfigurator.configure();
        log.setLevel(Level.DEBUG);

        DecimalFormat df = new DecimalFormat("#.#####");

        try
        {
            /**********************************************************/
            // Create writer for new file
            /**********************************************************/
            String line;
            log.info("Heatmap file: " + gzHtmlFile);
            PrintWriter writer = new PrintWriter(gzHtmlFile, "UTF-8");

            /**********************************************************/
            //Get HTML Header template and write it to the new file
            /**********************************************************/

            if (gzHtmlHeader != null)
            {
                log.info("HTML header: " + gzHtmlHeader);

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
                finally
                {
                    br.close();
                }
            }

            /**********************************************************/
            //Loop through the list of project files
            /**********************************************************/
            log.debug("Getting file list...");

            // We only want .java files, so
            File [] fileList = new File(gzStudentDir).listFiles(
                new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(".java");
                    }
                });

            if (fileList.length == 0)
            {
                // We didn't find any java files in the direction that was
                // passed in, so lets see if we can find it in the src
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
                if (fileList.length == 0)
                {
                    log.error("No class files found in <gzStudentDir> or "
                        + "<gzStudentDir>/src/");
                    log.error("Heat map file will be empty.");
                }
                else
                {
                    log.info("Found " + fileList.length
                        + " class files in <gzStudentDir>/src/...");
                }
            }
            else
            {
                log.info("Found " + fileList.length
                    + " class files in gzStudentDir...");
            }
            // File[] fileList = new File(gzStudentDir).listFiles();

            /**********************************************************/
            //Open the GZoltar results for this assignment
            /**********************************************************/
            log.info("Opening GZoltar results: " + gzXmlFile);
            File fXmlFile = new File(gzXmlFile);
            DocumentBuilderFactory dbFactory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();

            /**********************************************************/
            //Build a map of scores to locations in the files.
            /**********************************************************/

            log.debug("Root element :"
                + doc.getDocumentElement().getNodeName());

            /**********************************************************/
            //Loop through the files, building the heatmap for each
            /**********************************************************/

            for (File file: fileList)
            {
                List<Integer> maxMethods = new ArrayList<Integer>();
                String filename = file.getName();
                String className =
                    filename.substring(0, filename.lastIndexOf('.'));
                if (filename.indexOf("Test") == -1)
                {
                    //File is not a student test file
                    log.debug("+++ " + filename);
                    writer.println("<div class=\"file\">");
                    writer.println("<h2>" + filename + "</h2>");
                    writer.println("<pre class=\"heat\">");

                    /********************************************************/
                    // Get all of the records for last evaluation of the
                    // current file
                    /********************************************************/

                    try
                    {
                        NodeList nList = doc.getElementsByTagName(className);

                        log.debug("+++Looking for nodes for " + className );
                        log.debug("+++Found " + nList.getLength() + " nodes");

                        for (int temp = 0; temp < nList.getLength(); temp++)
                        {
                            Node nNode = nList.item(temp);
                            log.debug("Current Element: "
                                + nNode.getNodeName());

                            if (nNode.hasChildNodes())
                            {
                                NodeList tmpList = nNode.getChildNodes();
                                log.debug("+++Found " + tmpList.getLength()
                                    + " suspect nodes");

                                for (int temp2 = 0; temp2 < tmpList.getLength();
                                    temp2++)
                                {
                                    Node tempNode = tmpList.item(temp2);
                                    if (tempNode.getNodeType() ==
                                        Node.ELEMENT_NODE)
                                    {
                                        Element eElement = (Element) tempNode;

                                        lineScores.put(eElement
                                            .getElementsByTagName("line")
                                            .item(0).getTextContent(),
                                            Double.parseDouble(eElement
                                                .getElementsByTagName("score")
                                                .item(0)
                                                .getTextContent()));

                                        if ("yes".equals(eElement
                                            .getElementsByTagName("most")
                                            .item(0).getTextContent()))
                                        {
                                            maxMethods.add(Integer.parseInt(
                                                eElement
                                                .getElementsByTagName("line")
                                                .item(0).getTextContent()));
                                        }

                                        if (log.isDebugEnabled())
                                        {
                                            log.debug("+++Method : "
                                                + eElement
                                                .getElementsByTagName("method")
                                                .item(0).getTextContent());
                                            log.debug("+++Line : "
                                                + eElement
                                                .getElementsByTagName("line")
                                                .item(0).getTextContent());
                                            log.debug("+++Score : "
                                                + eElement
                                                .getElementsByTagName("score")
                                                .item(0).getTextContent());
                                            log.debug("+++Most : "
                                                + eElement
                                                .getElementsByTagName("most")
                                                .item(0).getTextContent());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.error(e);
                    }

                    if (log.isDebugEnabled())
                    {
                        log.debug("**************  Line Scores  **************");
                        printMap(lineScores);
                        log.debug("**************  Max Methods  **************");
                        log.debug(maxMethods);
                    }
                    /*******************************************************/
                    // Parse the current file to get the locations of
                    // constructors and methods
                    /*******************************************************/
                    double currScore = 0.0;
                    int identifiedMaxMethods = 1;

                    FileInputStream parserIn =
                        new FileInputStream(new File(gzStudentDir, filename));
                    CompilationUnit cu;
                    try {
                        // parse the file
                        cu = JavaParser.parse(parserIn);
                    }
                    finally
                    {
                        parserIn.close();
                    }
                    new MethodVisitor().visit(cu, null);
                    new ConstructorVisitor().visit(cu, null);

                    Collections.sort(methodStartLocs);
                    Collections.sort(methodEndLocs);

                    if (log.isDebugEnabled())
                    {
                        log.debug("******Start of method locs******");
                        log.debug(methodStartLocs.toString());
                        log.debug("******End of method locs******");
                        log.debug(methodEndLocs.toString());
                    }

                    BufferedReader br = new BufferedReader(
                        new FileReader(new File(gzStudentDir, filename)));
                    try
                    {
                        int linenumber = 1;
                        while ((line = br.readLine()) != null)
                        {
                           // System.out.println("Pre  [" + linenumber +  "]" + line);
                           // Replace < and > with the correseponding HTML entities
                           line = line.replace("<", "&lt;");
                           line = line.replace(">", "&gt;");

                           String lineNo = Integer.toString(linenumber);
                           if (lineScores.containsKey(lineNo))
                           {
                               currScore = lineScores.get(lineNo);
                               line = "<span style=\"background-color: hsl("
                                   + getScoreColor(currScore)
                                   + ", 100%, 50%)\"><a href=\"#\" data-toggle"
                                   + "=\"tooltip\" data-html=\"true\" title=\""
                                   + filename + ":" + linenumber
                                   + "<br/>suspicion: " + df.format(currScore)
                                   + "\">" + line + "</a></span>";
                                   log.debug("Found score:" + currScore
                                       + " at line: " + linenumber);
                           }

                           // See if the current line is the start or the end
                           // of a method...
                           if (methodStartLocs.indexOf(linenumber) != -1)
                           {
                               // Curr line has start of method
                               Integer nextMethodLine = null;
                               // See if the current method is a max suspicion
                               // method
                               if (methodStartLocs.indexOf(linenumber) + 1 <
                                   methodStartLocs.size())
                               {
                                   nextMethodLine = methodStartLocs.get(
                                       methodStartLocs.indexOf(linenumber) + 1);
                               }
                               else
                               {
                                   nextMethodLine = 1000;
                               }
                               log.debug("line: " + linenumber
                                   + " nextMethodLine " + nextMethodLine );

                               String divClass = "";
                               for (int inx = linenumber; inx <= nextMethodLine;
                                   inx++)
                               {
                                   if (maxMethods.contains(inx))
                                   {
                                       if (identifiedMaxMethods <=
                                           gzLimitToMaxMethod)
                                       {
                                            log.debug("identifiedMaxMethod "
                                                + identifiedMaxMethods
                                                + " line: " + inx);
                                            divClass = "method-suspect";
                                            identifiedMaxMethods++;
                                       }
                                   }
                               }

                               line = "<div class=\"" + divClass + "\">" + line;
                               log.debug("**********"+divClass+"***********");
                           }
                           if (methodEndLocs.indexOf(linenumber) != -1)
                           {
                               // Curr line has end of method
                               line = line + "</div>";
                           }

                           writer.println(line);
                           linenumber++;
                        }
                        linenumber = 1;
                        methodStartLocs.clear();
                        methodEndLocs.clear();
                        lineScores.clear();
                    }
                    finally
                    {
                        br.close();
                    }
                    writer.println("</pre>");
                    writer.println("</div>");
                }
            }

            /**********************************************************/
            // Get HTML footer template and write it to the new file
            /**********************************************************/
            if (gzHtmlFooter != null)
            {
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
            log.debug("Key : " + entry.getKey()
                + " Value : " + entry.getValue());
        }
    }


}
