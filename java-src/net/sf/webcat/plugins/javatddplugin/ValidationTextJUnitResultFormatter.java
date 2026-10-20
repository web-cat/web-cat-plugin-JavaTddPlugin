package net.sf.webcat.plugins.javatddplugin;

import junit.framework.Test;
import junit.framework.AssertionFailedError;

public class ValidationTextJUnitResultFormatter
    extends BasicJUnitResultFormatter {

    private static class FormattedThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        private final Throwable original;
        private final String message;

        public FormattedThrowable(String message, Throwable original) {
            super(message, original);
            this.message = message;
            this.original = original;
            this.setStackTrace(original.getStackTrace());
        }


        @Override
        public String toString() {
            return original.getClass().getName() + ": " + message;
        }
    }

    @Override
    public void addError(Test test, Throwable error) {
        String msg = formatMessage(error);
        Throwable newError = new FormattedThrowable(msg, error);
        super.addError(test, newError);
    }


    @Override
    public void addFailure(Test test, AssertionFailedError error) {
        String msg = formatMessage(error);
        Throwable newFailure = new FormattedThrowable(msg, error);
        super.addFailure(test, newFailure);
    }


    private String formatMessage(Throwable error) {
        String customMsg = ValidationMessageFormatter.formatMessage(error);

        if (customMsg != null) {
            return customMsg;
        }

        if (error != null) {
            if (error.getMessage() != null) {
                return error.getMessage();
            }
            else {
                return error.toString();
            }
        }
        return "";
    }
}
