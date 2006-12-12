/*___INFO__MARK_BEGIN__*/
/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the Sun Industry Standards Source License Version 1.2
 *
 *  Sun Microsystems Inc., March, 2001
 *
 *
 *  Sun Industry Standards Source License Version 1.2
 *  =================================================
 *  The contents of this file are subject to the Sun Industry Standards
 *  Source License Version 1.2 (the "License"); You may not use this file
 *  except in compliance with the License. You may obtain a copy of the
 *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
 *
 *  Software provided under this License is provided on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
 *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 *  See the License for the specific provisions governing your rights and
 *  obligations concerning the Software.
 *
 *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
 *
 *   Copyright: 2001 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/
package com.sun.grid.logging;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This filter filters a LogRecord be regular
 * expressions.
 */
public class RegExFilter implements Filter {
    /** this patter filters the source class name. */
    private Pattern sourceClassPattern = null;
    /** this pattern filter the source methode name. */
    private Pattern sourceMethodPattern = null;
    /** the max. log level. */
    private Level   level = null;

    /** Creates a new instance of RegExFilter. */
    public RegExFilter() {
    }

    /**
     * Creates a new instance of RegExFilter.
     * @param aLevel the max. level
     */
    public RegExFilter(final Level aLevel) {
        this(null, null, aLevel);
    }

    /**
     * Creates a new instance of RegExFilter.
     * @param aSourceClassPattern regular expression for the source class
     * @param aLevel  the max. level
     */
    public RegExFilter(final String aSourceClassPattern, final Level aLevel) {
        this(aSourceClassPattern, null, aLevel);
    }

    /**
     *
     * Creates a new instance of RegExFilter.
     * @param aSourceClassPattern regular expression for the source class
     * @param aSourceMethodPattern regular expression for the source method
     * @param aLevel  the max. level
     */
    public RegExFilter(final String aSourceClassPattern,
                       final String aSourceMethodPattern,
                       final Level aLevel) {
        setSourceClassPattern(aSourceClassPattern);
        setSourceMethodPattern(aSourceMethodPattern);
        setLevel(aLevel);
    }

    /**
     * set the regular expression which filters the source class name.
     * @param pattern  regular expression
     */
    public final void setSourceClassPattern(final String pattern) {
        sourceClassPattern = compile(pattern);
    }

    /**
     * get the regular expression which filters the source class name.
     * @return the regular expression
     */
    public final String getSourceClassPattern() {
        if (sourceClassPattern != null) {
            return sourceClassPattern.pattern();
        } else {
            return null;
        }
    }

    /**
     * set the regular expression which filters the source method name.
     * @param pattern  regular expression
     */
    public final void setSourceMethodPattern(final String pattern) {
        sourceMethodPattern = compile(pattern);
    }

    /**
     * get the regular expression which filters the source method name.
     * @return the regular expression
     */
    public final String getSourceMethodPattern() {
        if (sourceMethodPattern != null) {
            return sourceMethodPattern.pattern();
        } else {
            return null;
        }
    }

    /**
     * help function for compiling a regular expression.
     * @param pattern  the regular expression
     * @return the pattern object
     */
    private Pattern compile(final String pattern) {
        if (pattern != null) {
            try {
                return Pattern.compile(pattern);
            } catch (PatternSyntaxException pse) {
                IllegalArgumentException iae = new IllegalArgumentException(
                   "Syntax error in pattern (" + pattern + ")");
                iae.initCause(pse);
                throw iae;
            }
        } else {
            return null;
        }
    }

    /**
     * set the max log level for this filter.
     * @param aLevel the max. log level
     */
    public final void setLevel(final Level aLevel) {
        this.level = aLevel;
    }

    /**
     * get the max log level for this filter.
     * @return max log level
     */
    public final Level getLevel() {
        return level;
    }

    /**
     * is the log record loggable.
     * @param logRecord  the log record
     * @return true  the log record is loggable
     */
    public final boolean isLoggable(final LogRecord logRecord) {
        if (level != null
            && logRecord.getLevel().intValue() < level.intValue()) {
            return false;
        }
        if (sourceClassPattern != null
            && !sourceClassPattern.matcher(
                logRecord.getSourceClassName()).matches()) {
            return false;
        }
        if (sourceMethodPattern != null
            && !sourceMethodPattern.matcher(
                  logRecord.getSourceMethodName()).matches()) {
            return false;
        }
        return true;
    }

}
