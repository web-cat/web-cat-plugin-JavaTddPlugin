package net.sf.webcat.plugins.javatddplugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class retest
{
    private static String[] tests = {
        "expected:<...e: 40960\n"
        + "Buffers: 5\n"
        + "[1023]9 records processed\n"
        + "> but was:<...e: 40960\n"
        + "Buffers: 5\n"
        + "[asdlkjalksdjasdlkjasldkjasdlkj]9records processed\n>",
        "expected:<...e: 40960 Buffers: 5> but was:<...e: 40960 Buffers: 5>",
        "expected:<...e: [40960 Buffers:]5> but was:<...e: []5>"
    };

    private static Pattern exp =
        Pattern.compile("expected:\\s*<([^\\[]*(\\[(.*)\\].*)?)> but "
            + "was:\\s*<([^\\[]*(\\[(.*)\\].*)?)>$", Pattern.DOTALL);

    public static void main(String[] args)
    {
        for (String test : tests)
        {
            System.out.println("checking: " + test);
            Matcher m = exp.matcher(test);
            if (m.find())
            {
                int start = m.start();
                int end = m.end();
                System.out.print("    found: ");
                System.out.println(test.substring(0, start)
                    + "|" + test.substring(start, end)
                    + "|" + test.substring(end, test.length()));
                for (int i = 1; i <= m.groupCount(); i++)
                {
                    System.out.println("    group " + i + " = |"
                        + m.group(i) + "|");
                }

                String msg = test;
                String prefix = msg.substring(0, m.start(1));
                String expected = m.group(1);
                String middle = msg.substring(m.end(1), m.start(4));
                String actual = m.group(4);
                String suffix = msg.substring(m.end(4));
                msg = prefix + expected + middle + actual + suffix;
                System.out.println("rebuild = " + msg.equals(test));
            }
            else
            {
                System.out.println("    not found");
            }
        }
    }
}
