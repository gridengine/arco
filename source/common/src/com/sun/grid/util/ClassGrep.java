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
package com.sun.grid.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * This class can be used to search java classes in a classpath
 */
public class ClassGrep implements Runnable {
   
   private boolean verbose;
   private String  className;
   private File [] classPathElements;
   
   /** Creates a new instance of ClassGrep */
   public ClassGrep() {
   }
   
   private void setArguments(String [] args) {
      
      int i = 0;
      
      // Parse all options
      while( i < args.length && args[i].startsWith("-")) {
          if( args[i].equals("-v")) {
             verbose = true;
          } else {
             usage("Invalid option " + args[i], 1 );
          }
          i++;
      }
      
      if( i < args.length ) {
          className = args[i];
          i++;
      } else {
         usage("Invalid paramter count", 1);
      }
      if( i >= args.length ) {
         usage("No file_list", 1);
      } else {
         classPathElements = new File [ args.length - i ];
         int ii = 0;
         
         while( i < args.length ) {
            classPathElements[ii] = new File(args[i]);
            ii++;
            i++;
         }
      }      
   }
   
   public void run() {
      
      int foundCount = 0;
      ClassLoader classLoader = null;
      URL urls [] = new URL[1];
      for( int i = 0; i < classPathElements.length; i++ ) {
         try {
            urls[0] = classPathElements[i].toURL();
            classLoader = new URLClassLoader(urls, null);
            try {
               Class.forName(className, false, classLoader);
               if( verbose ) {
                  System.out.println(classPathElements[i]);
               }
               foundCount++;
            } catch( ClassNotFoundException cnfe ) {
            }
            if( !verbose ) {
               System.out.print(".");
            }
         } catch( MalformedURLException mfue ) {
            usage("Can not convert file " + classPathElements + " into a URL", 1 );
         }         
      }
      System.out.println();
      if( foundCount > 0 ) {
         System.exit(0);
      } else {
         System.exit(2);
      }
         
      
   }
   
   public static void main( String[] args ) {
      
      ClassGrep classGrep = new ClassGrep();
      classGrep.setArguments(args);
      classGrep.run();
      
   }
   
   private static void usage(String message, int exitCode ) {
      if( message != null ) {
         System.err.println(message);
      }
      
      System.err.println("ClassGrep [-v]  class_name file_list");
      
      System.err.println( "  class_name   name of the searched classes");
      System.err.println( "  file_list    list of directories, jar or zip files");      
      System.err.println();
      System.err.println( "  Return: ");
      System.err.println( "  ======= ");
      System.err.println( "    0     class was found");
      System.err.println( "    2     class was not found");
      System.err.println( "    else   error");
      System.err.println( );
      System.err.println( "  Options:");
      System.err.println( "  ========");
      System.err.println( "     -v   verbose output" );
      System.err.println( );
      System.err.println( "  Examples:");
      System.err.println( "  ========");
      System.err.println( );
      System.err.println( "   Search a class by it's full qualified name: ");
      System.err.println( "   ClassGrep com.sun.grid.util.ClassGrep arco_common.jar");
      System.err.println();
      
      System.exit(exitCode);      
   }
}
