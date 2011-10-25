package net.sf.webcat.plugins.javatddplugin;

import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.util.StringUtils;

public class CsvJUnitResultFormatter
    extends PlistJUnitResultFormatter
{
    // ----------------------------------------------------------
    protected void formatTestResultAsPlist(TestResultDescriptor result)
    {
        String prefix = System.getProperty(
            CsvJUnitResultFormatter.class.getName() + ".prefix", "");
        if (prefix.length() > 0 && !prefix.endsWith(","))
        {
            prefix += ",";
        }
        testResultsPlist.append(prefix);
        testResultsPlist.append(result.suite.getName());
        testResultsPlist.append(',');
        String testName = "";
        if ( result.test != null )
        {
            testName = result.test.toString();
            int pos = testName.indexOf( "(" );
            if ( pos >= 0 )
            {
                testName = testName.substring( 0, pos );
            }
        }
        testResultsPlist.append(testName);
        testResultsPlist.append(',');
        testResultsPlist.append(result.level);
        testResultsPlist.append(',');
        testResultsPlist.append(result.code);
        testResultsPlist.append(',');
        if (result.error != null)
        {
            testResultsPlist.append(result.error.getClass().getName());
        }
        else
        {
            testResultsPlist.append("null");
        }
        testResultsPlist.append(StringUtils.LINE_SEP);
    }


    // ----------------------------------------------------------
    protected void outputForSuite(StringBuffer buffer, JUnitTest suite)
    {
        buffer.append(testResultsPlist.toString());
        testResultsPlist.setLength(0);
    }

}
