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
package com.sun.grid.reporting.dbwriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.sun.grid.reporting.dbwriter.model.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;


public class DerivedValueTable {
   
   /** configuration of the dbwriter */
   private DbWriterConfig config;
   
   /** Printer for the output */
   private DerivedValueTablePrinter printer;
   
   
   public DerivedValueTable(File calcFile, DerivedValueTablePrinter printer) throws JAXBException {
      JAXBContext ctx = JAXBContext.newInstance( "com.sun.grid.reporting.dbwriter.model" );
      Unmarshaller u = ctx.createUnmarshaller();
      config = (DbWriterConfig)u.unmarshal( calcFile );
      
      this.printer = printer;
   }
   
   
   /**
    * Print the derived value table into a PrintWriter
    *
    * @param pw  the PrintWriter
    */
   public void print(PrintWriter pw) {
      
      
      Map objectMap = new HashMap();
      Map variableMap  = new HashMap();
      
      Iterator iter = config.getDerive().iterator();
      
      List lines = new ArrayList();
      
      while(iter.hasNext()) {
         
         DeriveRuleType dvr = (DeriveRuleType)iter.next();
         
         DerivedValueLine dvl = new DerivedValueLine();
         
         dvl.setObject(dvr.getObject() + "_values");
         dvl.setVariable( dvr.getVariable() );
         // JG: TODO: it should be possible to pass an integer to setInterval
         dvl.setInterval( dvr.getInterval().value() );
         
         lines.add(dvl);
      }
      
      iter = config.getDelete().iterator();
      
      
      while(iter.hasNext()) {
         DeletionRuleType dr = (DeletionRuleType)iter.next();
         String object = dr.getScope();
         
         if(dr.isSetSubScope()) {
            // The deletion rules only deletes some variables            
            Iterator subScopeIter = dr.getSubScope().iterator();
            while(subScopeIter.hasNext()) {
               String variable = (String)subScopeIter.next();
               
               Iterator lineIter = lines.iterator();
               
               DerivedValueLine dvl = null;
               
               while(lineIter.hasNext()) {
                  
                  DerivedValueLine tmpdvl = (DerivedValueLine)lineIter.next();
                  
                  if(tmpdvl.getObject().equals(object) && tmpdvl.getVariable().equals(variable)) {
                     dvl = tmpdvl;
                  }
               }
               
               if(dvl == null) {
                  dvl = new DerivedValueLine();
                  dvl.setObject(dr.getScope());
                  dvl.setInterval("raw value");
                  dvl.setVariable(variable);
                  lines.add(dvl);
               }
               dvl.setLifeTime( getLifetime(dr) );
            }
         } else {
            // The deletion rules deletes all variables of the object         
            Iterator lineIter = lines.iterator();
            while(lineIter.hasNext()) {
               
               DerivedValueLine dvl = (DerivedValueLine)lineIter.next();
               
               if(dvl.getObject().equals(object) && dvl.getLifeTime() == null ) {
                  dvl.setLifeTime( getLifetime(dr) );
               }
            }
            
            DerivedValueLine dvl = new DerivedValueLine();
            dvl.setObject(dr.getScope());
            dvl.setVariable("*");
            dvl.setLifeTime(getLifetime(dr));
            dvl.setInterval("*");
            lines.add(dvl);
         }
      }
      
      Collections.sort(lines,new DerivedValueLineComparator());
      
      printer.print(pw, lines);
   }

   
   /**
    * Get the lifetime string of a deletion rule
    * @param dr  the deletion rule
    * @return    the lifetime string
    */
   private static String getLifetime(DeletionRuleType dr) {
      StringBuffer ret = new StringBuffer();
      ret.append(dr.getTimeAmount());
      ret.append(' ');
      ret.append(dr.getTimeRange());
      if(dr.getTimeAmount() > 1 ) {
         ret.append('s');
      }
      return ret.toString();
   }
   
   
   
   public static void main(String [] args) {
      
      try {
         
         DerivedValueTablePrinter printer = null;
         File calcFile = null;

         switch(args.length) {
            case 1:
               if(args[0].equals("-help")) {
                  usage(null, 0);
               }
               calcFile = new File(args[0]);
               printer = new DerivedValueTableTextPrinter();
               break;
            case 3:
               if ( args[0].equals("-o") ) {
                 if (args[1].equals("txt") ) {
                    printer = new DerivedValueTableTextPrinter();
                 }  else if (args[1].equals("csv")) {
                    printer = new DerivedValueTableCSVPrinter();
                 }  else {
                    usage("Unknown output format " + args[1], 1);
                 }
                 calcFile = new File(args[2]);                  
               } else {
                  usage("Unknown option " + args[0], 1 );
               }
               break;
            default:
              usage("Invalid number of arguments", 1);
         }
         
         
         if(!calcFile.exists()) {
            usage("file " + args[0] + " does not exists", 1);
         }
         
         if(!calcFile.canRead()) {
            usage("Can not read " + args[0], 1);
         }
         
         DerivedValueTable dvt = new DerivedValueTable(calcFile, printer);
         
         PrintWriter pw = new PrintWriter(System.out);
         dvt.print(pw);
         
         pw.flush();
         
      } catch( Exception e ) {
         e.printStackTrace();
      }
      
   }
   
   private static void usage(String message, int exitCode) {
      if( message != null ) {
         System.err.println(message);
      }
      
      System.err.println("DerivedValueTable [-o (txt|csv)] <calcFile>");
      System.exit(exitCode);
      
   }
   
   static class DerivedValueLineComparator implements java.util.Comparator {
      
      
      public int compare(Object obj, Object obj1) {
         DerivedValueLine line = (DerivedValueLine)obj;
         DerivedValueLine line1 = (DerivedValueLine)obj1;
         
         int ret = line.getObject().compareTo(line1.getObject());
         if(ret == 0) {
            ret = line.getInterval().compareTo(line1.getInterval());
         }
         if(ret == 0) {
            ret = line.getVariable().compareTo(line1.getVariable());
         }
         return ret;
      }
      
      
   }
   
   static class DerivedValueLine {
      
      private String object;
      private String variable;
      private String interval;
      private String lifeTime;
      
      public String getObject() {
         return object;
      }
      
      public void setObject(String object) {
         this.object = object;
      }
      
      public String getVariable() {
         return variable;
      }
      
      public void setVariable(String variable) {
         this.variable = variable;
      }
      
      public String getInterval() {
         return interval;
      }
      
      public void setInterval(String interval) {
         this.interval = interval;
      }
      
      public String getLifeTime() {
         return lifeTime;
      }
      
      public void setLifeTime(String lifeTime) {
         this.lifeTime = lifeTime;
      }
      
      
   }
   
   static interface DerivedValueTablePrinter {
      
      public void print(PrintWriter pw, List lines);
   }
   

   static class DerivedValueTableCSVPrinter implements  DerivedValueTablePrinter {
      
      public void print(PrintWriter pw, List lines) {
         Iterator iter = lines.iterator();
         
         String lastObject = "";
         String lastInterval = "";
         String object = "";
         String interval = "";
         
         boolean objectChanged = false;
         boolean intervalChanged = false;
         
         pw.print("Object");
         pw.print(",");
         pw.print("Interval");
         pw.print(",");
         pw.print("Variable");
         pw.print(",");
         pw.print("Lifetime");
         pw.println();
         while(iter.hasNext()) {
            
            DerivedValueLine dvl = (DerivedValueLine)iter.next();
            
            if(lastObject.equals(dvl.getObject())) {
               object = "";
               objectChanged = false;
               if(lastInterval.equals(dvl.getInterval())) {
                  interval = "";
                  intervalChanged = false;
               } else {
                  intervalChanged = true;
                  interval = dvl.getInterval();
                  lastInterval = interval;
               }
               
            } else {
               object = dvl.getObject();
               objectChanged = true;
               intervalChanged = false;
               lastObject = object;
               interval = dvl.getInterval();
               lastInterval = interval;
            }

            pw.print(object);
            pw.print(",");
            pw.print(interval);
            pw.print(",");
            pw.print(dvl.getVariable());
            pw.print(",");
            pw.print(dvl.getLifeTime());
            pw.println();
         }
      }
   }
   
   
   static class DerivedValueTableTextPrinter implements  DerivedValueTablePrinter {
      public void print(PrintWriter pw, List lines) {
         Iterator iter = lines.iterator();
         
         String lastObject = "";
         String lastInterval = "";
         String object = "";
         String interval = "";
         
         boolean objectChanged = false;
         boolean intervalChanged = false;
         
         print(pw, "Object", 20, ' ' );
         print(pw, "Interval", 20, ' ' );
         print(pw, "Variable", 20, ' ' );
         print(pw, "Lifetime", 20, ' ' );
         pw.println();
         while(iter.hasNext()) {
            
            DerivedValueLine dvl = (DerivedValueLine)iter.next();
            
            if(lastObject.equals(dvl.getObject())) {
               object = "";
               objectChanged = false;
               if(lastInterval.equals(dvl.getInterval())) {
                  interval = "";
                  intervalChanged = false;
               } else {
                  intervalChanged = true;
                  interval = dvl.getInterval();
                  lastInterval = interval;
               }
               
            } else {
               object = dvl.getObject();
               objectChanged = true;
               intervalChanged = false;
               lastObject = object;
               interval = dvl.getInterval();
               lastInterval = interval;
            }
            
            if( objectChanged ) {
               print(pw, "", 4*20, '-');
               pw.println();
            } else if ( intervalChanged ) {
               print(pw, "", 20, ' ');
               print(pw, "", 3*20, '-');
               pw.println();
            }
            print(pw, object, 20, ' ');
            print(pw, interval, 20, ' ' );
            print(pw, dvl.getVariable(), 20, ' ');
            print(pw, dvl.getLifeTime(), 20, ' ');
            pw.println();
         }
      }
      
      private static void print(PrintWriter pw, Object obj,  int len, char filler) {
         
         String str = obj == null ? "null" : obj.toString();
         
         pw.print(str);
         len -= str.length();
         
         while(len > 0) {
            pw.print(filler);
            len--;
         }
         
      }
      
   }
}
