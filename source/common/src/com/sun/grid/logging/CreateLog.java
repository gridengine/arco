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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This class create the source code for the SGELog class.
 */
public final class CreateLog {
    /** the indent string. */
    private static final String INDENT = "   ";

    /** for this parameter types log methods will be created. */
    private static final ParamType [] PARAM_TYPES = {
       new ParamType("Object", null),
       new ParamType("int", "Integer")
    };

    /** max. number of parameter for log methods. */
    private static final int MAX_PARAM_COUNT = 10;

    /** print stream for output. */
    private static PrintStream ps;
    /** buffer for indenting. */
    private static StringBuffer indent = new StringBuffer();
    /** this flag indicates that a indent should
     *  be added.
     *  @see #print(String)
     */
    private static boolean addIndent = false;

    /** private constrcutor for Utility class. */
    private CreateLog() {

    }

    /**
     * add an indent.
     */
    private static void indent() {
        indent.append(INDENT);
    }
    /**
     * remove an indent.
     */
    private static void deindent() {
        indent.setLength(indent.length() - INDENT.length());
    }


    /**
     * print a string.
     * @param str the string
     */
    private static void print(final String str) {
        if (addIndent) {
            ps.print(indent);
            addIndent = false;
        }
        ps.print(str);
    }

    /**
     * print an integer.
     * @param i the integer
     */
    private static void print(final int i) {
        if (addIndent) {
            ps.print(indent);
            addIndent = false;
        }
        ps.print(i);
    }

    /**
     * print a string and go to the next line.
     * @param str the string
     */
    private static void println(final String str) {
        print(str);
        ps.println();
        addIndent = true;
    }

    /**
     * print a integer and go to the next line.
     * @param i  the integer
     */
    private static void println(final int i) {
        print(i);
        ps.println();
        addIndent = true;
    }

    /** prefix source code. */
    private static final String [] PREFIX = {
        "package com.sun.grid.logging;",
        "import java.util.logging.Logger;",
        "import java.util.logging.Level;",
        "import java.util.logging.LogRecord;",
        "",
        "/*  Utilitiy class for logging ",
        " *",
        " * @see BaseSGELog",
        " */",
        "public class SGELog extends BaseSGELog {",
    };

    /** suffix source code .*/
    private static final String [] SUFFIX = {
      "}"
    };

    /** the names of the generated messages. */
    private static final String [] METHODS = {
        "severe", "warning" , "config", "info", "fine", "finer", "finest"
    };

    /** the name of the levels according the the method names. */
    private static final String [] LEVELS = {
        "Level.SEVERE", "Level.WARNING", "Level.CONFIG", "Level.INFO",
        "Level.FINE", "Level.FINER", "Level.FINEST"
    };

    /**
     *  instances of the class describes a parameter.
     *  of a log message
     */
    static class ParamType {
       /** type of the paramter (e.g. int, double, ...). */
       private String type;
       /** wraper type of the parameter (e.g. Integer, Double, ...). */
       private String wrapper;

       /**
        *  Create a new ParamType.
        * @param aType     the type (e.g. int, double, ...)
        * @param aWrapper  the wrapper(e.g. Integer, Double, ...)
        */
       public ParamType(final String aType, final String aWrapper) {
          this.type = aType;
          this.wrapper = aWrapper;
       }

       /**
        * get the source code which creates a wrapper
        * object for a primitive parameter
        * (e.g. "new Integer(&lt;paramName&gt;)"
        * @param paramName name of the parameter
        * @return the source code
        */
       public final String getWrapper(final String paramName) {
          if (wrapper != null) {
             return "new " + wrapper + "(" + paramName + ")";
          }
          return paramName;
       }
    }


    /**
     * Generate all log method with the different signatures for a methode
     * name.<br>
     * <b>Example</b><br>
     * <pre>
     *     public void warning() { ...
     *     public void warning(Object param1) { ...
     *     public void warning(Object param1, Object param2) { ...
     *     ...
     * </pre>
     * @param method  the method name (e.g. "warning", "info", ...)
     * @param level   corresponding log level for the method name
     */
    private static void genMethods(final String method, final String level) {
        print("/* write a log message in level ");
        print(level);
        println(".");
        println("* @param msg the log message");
        println("*/");
        print("public static void ");
        print(method);
        println("(String msg) {");
        indent();
        print("  getLogger().log(createRecord(");
        print(level);
        println(", msg));");
        deindent();
        println("}");

        print("/* write a log message in level ");
        print(level);
        println(".");
        println("* @param t   the throwable which should be included");
        println("* @param msg the log message");
        println("*/");
        print("public static void ");
        print(method);
        println("(Throwable t, String msg) {");
        indent();
        print("LogRecord lr = createRecord(");
        print(level);
        print(", msg); ");
        println("lr.setThrown(t);");
        println("getLogger().log(lr); ");
        deindent();
        println("}");

        print("/* write a log message in level ");
        print(level);
        println(".");
        println("* @param t   the throwable which should be included");
        println("* @param msg the log message");
        println("* @param params array of parameter for the message");
        println("*/");
        print("public static void ");
        print(method);
        print("_p");
        println("(Throwable t, String msg, Object[] params) {");
        indent();
        print("if (getLogger().isLoggable(");
        print(level);
        println(")) { ");
        indent();
        print("LogRecord lr = createRecord(");
        print(level);
        println(", msg);");
        println("lr.setThrown(t);");
        println("lr.setParameters( params );");
        println("getLogger().log(lr);");
        deindent();
        println("}");
        deindent();
        println("}");


        for (int i = 1; i <= MAX_PARAM_COUNT; i++) {
           for (int ii = 0; ii < PARAM_TYPES.length; ii++) {
               genParamMethods(method, level, PARAM_TYPES[ii], i, false);
               genParamMethods(method, level, PARAM_TYPES[ii], i, true);
           }
        }
    }

    /**
     *  generate the entering methods.
     */
    private static void genEnteringMethods() {
        genEnteringMethod(PARAM_TYPES[0], 0);
        for (int i = 1; i <= MAX_PARAM_COUNT; i++) {
           for (int ii = 0; ii < PARAM_TYPES.length; ii++) {
              genEnteringMethod(PARAM_TYPES[ii], i);
           }
        }
    }

    /**
     *  get the producer of a method.
     *  @return  the producer of the method
     */
    private static String getProducer() {
       Exception e = new Exception();
       StackTraceElement elm = e.getStackTrace()[1];
       return elm.getClassName() + "." + elm.getMethodName()
              + ":" + elm.getLineNumber();
    }


    /**
     * generate the entering methods for a parameter type.
     * @param paramType   the parameter type
     * @param paramCount  max. number of parameters
     */
    private static void genEnteringMethod(final ParamType paramType,
                                          final int paramCount) {

       println("/* Log the entry of a method.");
       println(" * @param theClass the class of the method");
       println(" * @param sourceMethod name of the method");
       for (int i = 0; i < paramCount; i++) {
          print(" * @param param");
          print(i);
          println(" parameter for the message");
       }
       println(" * producer is " + getProducer());
       println(" */");
       print("public static void entering(Class theClass, String sourceMethod");
       if (paramCount > 0) {
          print(",");
          for (int i = 0; i < paramCount; i++) {
             if (i > 0) {
                print(",");
             }
             print(paramType.type);
             print(" param");
             print(i);
          }
       }
       println(") {");
       indent();
       println("if (getLogger().isLoggable(Level.FINER)) {");
       indent();
       print("entering(theClass.getName(), sourceMethod");
       for (int i = 0; i < paramCount; i++) {
          print(", param" + i);
       }
       println(");");
       deindent();
       println ("}");
       deindent();
       println("}");


       println("/* Log the entry of a method.");
       println(" * @param sourceClass name of the class of the method");
       println(" * @param sourceMethod name of the method");
       for (int i = 0; i < paramCount; i++) {
          print(" * @param param");
          print(i);
          println(" parameter for the message");
       }
       println(" * producer is " + getProducer());
       println(" */");
       print("public static void entering(String sourceClass,");
       print(" String sourceMethod");
       if (paramCount > 0) {
          print(",");
          for (int i = 0; i < paramCount; i++) {
             if (i > 0) {
                print(",");
             }
             print(paramType.type);
             print(" param" + i);
          }
       }
       println(") {");
       indent();
       println("if (getLogger().isLoggable(Level.FINER)) {");
       indent();

       print("getLogger().entering(sourceClass, sourceMethod ");

       switch(paramCount) {
          case 0:
            break;
          case 1: print(", " + paramType.getWrapper("param0"));
            break;
          default:
             print(", new Object [] { ");
             for (int i = 0; i < paramCount; i++) {
                if (i > 0) {
                   print(", ");
                }
                print(paramType.getWrapper("param" + i));
             }
             print("} ");
       }
       println(");");
       deindent();
       println ("}");
       deindent();
       println("}");
    }

    /**
     *   generate the existing methods.
     */
    private static void genExitingMethods() {

       println("/* Log the exit of a method.");
       println(" * @param sourceClass name of the class of the method");
       println(" * @param sourceMethod name of the method");
       println(" * producer is " + getProducer());
       println(" */");
       println("public static void exiting(String sourceClass,");
       println("                           String sourceMethod) {");
       indent();
       println("if (getLogger().isLoggable(Level.FINER)) {");
       indent();
       println("getLogger().exiting(sourceClass, sourceMethod);");
       deindent();
       println("}");
       deindent();
       println("}");

       println("/* Log the exit of a method.");
       println(" * @param sourceClass the class of the method");
       println(" * @param sourceMethod name of the method");
       println(" * producer is " + getProducer());
       println(" */");
       println("public static void exiting(Class sourceClass,");
       println("                           String sourceMethod) {");
       indent();
       println("if (getLogger().isLoggable(Level.FINER)) {");
       indent();
       println("getLogger().exiting(sourceClass.getName(), sourceMethod);");
       deindent();
       println("}");
       deindent();
       println("}");


       for (int i = 0; i < PARAM_TYPES.length; i++) {
          println("/* Log the exit of a method.");
          println(" * @param sourceClass name of the class of the method");
          println(" * @param sourceMethod name of the method");
          println(" * @param returnCode   the return code of the method");
          println(" * producer is " + getProducer());
          println(" */");
          println("public static void exiting(String sourceClass,");
          print("                           String sourceMethod,");
          print(PARAM_TYPES[i].type);
          println(" returnCode) {");
          indent();
          println("if (getLogger().isLoggable(Level.FINER)) {");
          indent();
          print("getLogger().exiting(sourceClass, sourceMethod, ");
          print(PARAM_TYPES[i].getWrapper("returnCode"));
          println(");");
          deindent();
          println("}");
          deindent();
          println("}");

          println("/* Log the exit of a method.");
          println(" * @param sourceClass the class of the method");
          println(" * @param sourceMethod name of the method");
          println(" * @param returnCode   the return code of the method");
          println(" * producer is " + getProducer());
          println(" */");
          println("public static void exiting(Class sourceClass,");
          print("                            String sourceMethod, ");
          print(PARAM_TYPES[i].type);
          println(" returnCode) {");
          indent();
          println("if (getLogger().isLoggable(Level.FINER)) {");
          indent();
          println("exiting(sourceClass.getName(), sourceMethod, returnCode);");
          deindent();
          println("}");
          deindent();
          println("}");
       }
    }


    /**
     * generate a method.
     * @param method         name of the method
     * @param level          log level of the method
     * @param paramType      parameter type of the methode
     * @param paramCount     max. number of parameters
     * @param withException  should the exception parameter be generated
     */
    private static void genParamMethods(final String method, final String level,
                                        final ParamType paramType,
                                        final int paramCount,
                                        final boolean withException) {
       print("/* Write a log message in level ");
       println(level);
       if (withException) {
          println(" * @param t  include this throwable");
       }
       println(" * @param msg  the log message");
       for (int i = 0; i < paramCount; i++) {
          print(" * @param param");
          print(i);
          println(" parameter for the message");
       }
       println(" * producer is " + getProducer());
       println(" */");
       print("public static void ");
       print(method);
       print("(");
       if (withException) {
          print("Throwable t, ");
       }
       print("String msg, ");
       for (int i = 0; i < paramCount; i++) {
          if (i > 0) {
             print(", ");
          }
          print(paramType.type);
          print(" param");
          print(i);
       }
       println(") {");
       indent();
       print("if (getLogger().isLoggable(");
       print(level);
       println(")) {");

       indent();
       print("LogRecord lr = createRecord(");
       print(level);
       println(", msg);");
       if (withException) {
          println("lr.setThrown(t);");
       }
       print("lr.setParameters(new Object [] { ");
       for (int i = 0; i < paramCount; i++) {
          if (i > 0) {
             print(", ");
          }
          print(paramType.getWrapper("param" + i));
       }
       println(" });");
       println("getLogger().log(lr);");
       deindent();
       println("} ");
       deindent();
       println("}");
    }

    /**
     * main method.
     * @param args  emtpy or a filename if args[0]
     */
    public static void main(final String[] args) {
       if (args.length == 0) {
         ps = System.out;
       } else if (args.length == 1) {
          try {
             File f = new File(args[0]);

             FileOutputStream fout = new FileOutputStream(f);

             ps = new PrintStream(fout);
             System.err.println("print output into file " + args[0]);
          } catch (IOException ioe) {
             System.err.println("Can't open output file " + args[0]);
             usage();
             System.exit(1);
          }
       } else {
          System.err.println("Illegal number of arguments");
          usage();
          System.exit(1);
       }


        println("// This class was generated by " + CreateLog.class.getName());
        println("// DO NOT EDIT THIS FILE");

        int i;
        for (i = 0; i < PREFIX.length; i++) {
            println(PREFIX[i]);
        }

        indent();
        for (i = 0; i < METHODS.length; i++) {
            genMethods(METHODS[i], LEVELS[i]);
        }
        genEnteringMethods();
        genExitingMethods();
        deindent();
        for (i = 0; i < SUFFIX.length; i++) {
            println(SUFFIX[i]);
        }
    }
    /**
     *  Print a usage message to stderr.
     */
    private static void usage() {
       PrintStream out = System.err;
       out.println("CreateLog [fileName]");
       out.println("   fileName  in this filename the source code for");
       out.println("             the class SGELog will be written.");
       out.println("             If no filename is given, the source code");
       out.println("             will be written to stdout");
    }
}
