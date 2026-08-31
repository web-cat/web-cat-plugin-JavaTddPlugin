package net.sf.webcat.plugins.javatddplugin;

import junit.framework.Test;

public class ValidationPlistJUnitResultFormatter
    extends PlistJUnitResultFormatter {
    @Override
    protected TestResultDescriptor describe(Test test, Throwable error) {
        String customMsg = ValidationMessageFormatter.formatMessage(error);

        if (customMsg != null) {
            int code = codeOf(error);
            int level = levelOf(code);
            String stackTrace = stackTraceMessage(error, false);
            return new TestResultDescriptor(currentSuite, test, error, code,
                level, customMsg, stackTrace);
        }
        else {
            return super.describe(test, error);
        }
    }
}
