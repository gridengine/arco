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
package com.sun.grid.arco;

import java.util.*;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.upgrade.*;
import com.sun.grid.logging.SGELog;
import java.io.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

public class AbstractXMLFileManager {

   private List namedObjectList = new ArrayList();
   private Set upgraderList = new TreeSet();
   
   private File  dir;
   private JAXBContext jc;
   private ObjectFactory faq;
   private Object sync = new Object();
   Thread scanThread;
   private Class  type;
   
   private com.sun.grid.arco.model.Toc toc;
   private File tocFile;
   private int version;
           
   /** Creates a new instance of AbstractFileManager */
   protected AbstractXMLFileManager( Class type, File dir, ClassLoader classLoader ) {
      if( !NamedObject.class.isAssignableFrom( type ) ) {
         throw new IllegalArgumentException("type has to be a NamedObject(" + type.getName() + ")");
      }
      this.type = type;
      this.dir = dir;
      this.tocFile = new File( dir, "toc.xml" );
      this.faq = new ObjectFactory();
      jc = createJAXBContext();
   }
   
   public ObjectFactory getObjectFactory() {
      return faq;
   }
   
   protected JAXBContext getJAXBContext() {
      return jc;
   }
   
   protected static TOCEntry getTOCEntryByFile( List list, File file ) {
      Iterator iter = list.iterator();
      TOCEntry entry = null;
      while( iter.hasNext() ) {
         entry = (TOCEntry)iter.next();
         if( entry.getFile().equals( file.getName() ) )
         {
            return entry;
         }
      }
      return null;
   }
   
   
   protected static TOCEntry getTOCEntryByName( List list, String name ) {
      Iterator iter = list.iterator();
      TOCEntry elem = null;
      while( iter.hasNext() ) {
         elem = (TOCEntry)iter.next();
         if( elem.getName().equals( name ) )
         {
            return elem;
         }
      }
      return null;
   }
   
   protected TOCEntry getTOCEntryByName( String name ) throws ArcoException {
      return getTOCEntryByName( getTOC().getEntry(), name );
   }
   
   private Toc getTOC() throws ArcoException {
      if( toc == null ) {
         parseTOC();
      }
      return toc;
   }
   
   protected boolean validate( NamedObject object ) {
      
      Validator validator = null;
      try {
         validator = getJAXBContext().createValidator();
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("Can't create validator");
         ilse.initCause( jaxbe );
         throw ilse;
      }
      try {
         return validator.validate( object );
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("validate error");
         ilse.initCause( jaxbe );
         throw ilse;
      }      
   }

   public void remove( String name ) throws ArcoException {
      
      synchronized( sync ) {
         
         TOCEntry entry = this.getTOCEntryByName( name );
         if( entry != null ) {
            getTOC().getEntry().remove( entry );
            File file = new File( dir, entry.getFile() );
            if( !file.delete() ) {
               SGELog.warning( "Can't delete file for entry with name ''{0}''", name );
            }
            save( getTOC(), tocFile );
            SGELog.info( "entry with name ''{0}'' removed", name );
         } else {
               SGELog.warning( "entry with name ''{0}'' is unknown, can't remove it", name );
         }
      }
      
   }
   protected NamedObject load( String name ) throws ArcoException {
      
      TOCEntry entry = this.getTOCEntryByName( name );
      if( entry != null ) {
         File file = new File( dir, entry.getFile() );
         NamedObject ret = (NamedObject)parse( file, NamedObject.class );
         upgrade(ret);
         return ret;
      }
      return null;
   }
   
   protected void registerUpgrader(Upgrader upgrader) {
      upgraderList.add(upgrader);
   }
           
   protected void upgrade(NamedObject obj) throws UpgraderException {

      Iterator iter = upgraderList.iterator();
      int version = 0;
      if( obj.isSetVersion() ) {
         version = obj.getVersion();

      }
      int maxVersion = obj.getVersion();
      while (iter.hasNext()) {
         Upgrader upgrader = (Upgrader) iter.next();
         if( upgrader.getVersion() > version ) {
            upgrader.upgrade(obj);
            maxVersion = Math.max(maxVersion, upgrader.getVersion());
         }
      }
      obj.setVersion(maxVersion);
   }
   
   protected void save( NamedObject obj ) throws ArcoException {

         if( !type.isAssignableFrom( obj.getClass() ) ) {
            throw new IllegalArgumentException( "obj must be an instance of " + type );
         }
        TOCEntry entry = null;
        
        synchronized( sync ) {
           if( scanThread != null ) {
              try {
                 SGELog.fine( "wait for end of scan thread before obj is stored" );
                 sync.wait();
              } catch( InterruptedException ire ) {
                 throw new ArcoException("xml.saveInterrupted", new Object[] { obj.getName() } );
              }
           }
           entry = getTOCEntryByName( obj.getName() );           
           if( entry == null ) {
              entry = createTOCEntry();
              entry.setFile( generateFilename( obj ) );
              getTOC().getEntry().add( entry );
           } 
           entry.setName( obj.getName() );
           entry.setImgURL( obj.getImgURL() );
           entry.setCategory( obj.getCategory() );
           entry.setType( obj.getType() );
           entry.setDescription(obj.getDescription());
           save( obj, new File( dir, entry.getFile() ) );
           entry.setLastModified( System.currentTimeMillis() );
           save( getTOC() , tocFile );
        }
   }   
   
   private String generateFilename( NamedObject obj ) {
      String name = obj.getName().replace( ' ', '_' ).replace( '/', '_' );
      String filename = name + ".xml";
      File file = new File( dir, filename );
      int i = 0;
      while( file.exists() ) {
         i++;
         filename = name + i + ".xml";
         file = new File( dir, filename );
      }      
      return filename;
   }
   
   public void save( Object obj, Writer writer )  {
      save(obj,writer,jc);
   }
   
   public static void save(Object obj, Writer writer, JAXBContext jc ) {
      Marshaller marshaller = null;
      try {
         marshaller = jc.createMarshaller();
         marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("Can't create marshaller");
         ilse.initCause( jaxbe );
         throw ilse;
      }
      try {
         marshaller.marshal( obj, writer );
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("marshal error");
         ilse.initCause( jaxbe );
         throw ilse;
      }      
   }
   
   public void save( Object obj, File file ) throws ArcoException {
      try {
         save(obj,file, jc);
      } catch( java.io.IOException ioe ) {
         throw new ArcoException("xml.saveError", ioe, 
                       new Object[] { file, ioe.getMessage() } );
      }
   }
   
   public static JAXBContext createJAXBContext() {
      try {
         return JAXBContext.newInstance( "com.sun.grid.arco.model" );
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException( "Can't create JAXBContext" );
         ilse.initCause( jaxbe );
         throw ilse;
      }
   }
   
   public static void save( Object obj, File file, JAXBContext jc ) throws IOException {
      
      FileWriter fw = null;
      fw = new FileWriter( file );
      
      try {
         save( obj, fw, jc );
      } finally {
         fw.close();
      }
   }
   
   // ----------- File Filter for the directory scan ---------------------------
   private final static FileFilter XML_FILE_FILTER = new QueryFileFilter();
   
   static class QueryFileFilter implements FileFilter {
      
      public boolean accept(File pathname) {
               return !pathname.isDirectory() &&
                      pathname.getName().toLowerCase().endsWith( ".xml" ) &&
                      !pathname.getName().equals( "toc.xml" );
      }      
   }

   // ---------------------- SCAN methods --------------------------------------
   
   
   
   /** 
    *  scan the query directory
    *  the method ensurce that only on thread at one time scans the directory
    *  The other thread wait for the end of the scan and returns immediatly
    */
   protected void scan() throws ArcoException {
      boolean iAmIn = false;
      try {
         synchronized( sync ) {
            if( scanThread == null ) {
               iAmIn = true;
               scanThread = Thread.currentThread();
            } else {
               sync.wait();
            }
         }
         if( iAmIn ) {
            try {
               scanDir();
            } finally {
               scanThread = null;
               synchronized( sync ) {
                  sync.notifyAll();
               }
            }
         }
      } catch (InterruptedException ire ) {
         throw new ArcoException("xml.scanInterrupted" );
      }
   }
      
   public NamedObject[] getAvailableObjects() throws ArcoException {
      scan();      
      NamedObject [] ret;
      synchronized( sync ) {
         Toc toc = getTOC();
         ret = new NamedObject[ toc.getEntry().size() ];
         toc.getEntry().toArray( ret );
      }      
      return ret;
   }
   
   private Object parse( File file, Class expectedType ) throws ArcoException {
      try {
         SGELog.fine( "parse file {0}", file );
         Unmarshaller um = getJAXBContext().createUnmarshaller();

         Object obj = um.unmarshal( file );
         if( !expectedType.isAssignableFrom( obj.getClass() ) ) {
            SGELog.warning( "file {0} does not contain an instance of {1}", file, type.getName() );
            return null;
         } else {
            return obj;
         }
      } catch (Exception e ) {
         throw new ArcoException("xml.parseError", e, 
                                 new Object[] { file, e.getMessage() } );
                                              
      }
   }
   
   public Object parse(Reader reader ) throws JAXBException {
         Unmarshaller um = getJAXBContext().createUnmarshaller();
         org.xml.sax.InputSource in = new org.xml.sax.InputSource(reader);
         return um.unmarshal( in );
   }
   
   public Object parse( File file ) throws JAXBException {
      
         Unmarshaller um = getJAXBContext().createUnmarshaller();
         return um.unmarshal(file);
   }
   
   private void parseTOC() throws ArcoException {
      
      if( tocFile.exists() ) {  
         SGELog.fine( "toc file exists, parse it");
         toc = (Toc)parse(tocFile, Toc.class );
         SGELog.fine( "toc has {0} entries", new Integer( toc.getEntry().size() ));
      }                  
      if( toc == null ) {
         SGELog.info( "toc file does not exist, create an empty toc");
         try {
            toc = this.getObjectFactory().createToc();
         } catch( JAXBException jaxbe ) {
            IllegalStateException ilse = new IllegalStateException( "Can't create instancoef Toc");
            ilse.initCause( jaxbe );
            throw ilse;
         }
      }      
   }
   
   
   /** 
    *   Scan the directory for new files
    */
   private void scanDir() throws ArcoException {
      SGELog.fine( "scanning directory {0}", dir );
      
      if( toc == null ) {
          parseTOC();
      }
      
      File [] files = dir.listFiles( XML_FILE_FILTER );
      String name = null;
      
      if( files != null && files.length > 0 ) {
         SGELog.fine( "found {0} xml files", new Integer( files.length ) );

         List tmpList = new ArrayList( toc.getEntry().size() );
         tmpList.addAll( toc.getEntry() );
         
         TOCEntry entry = null;
         long lastModified = 0L;         
         NamedObject obj = null;
         boolean newEntry = false;
         boolean tocModified = false;
         for( int i = 0; i < files.length; i++ ) {
            newEntry = false;
            entry = getTOCEntryByFile( toc.getEntry(), files[i] );
            if( entry == null ) {
               try {
                  entry = getObjectFactory().createTOCEntry();
               } catch( JAXBException jaxbe ) {
                  IllegalStateException ilse = new IllegalStateException( "Can't create instancoef TOCEntry");
                  ilse.initCause( jaxbe );
                  throw ilse;
               }               
               entry.setFile( files[i].getName() );
               newEntry = true;
            } else {
               tmpList.remove( entry );
            }
            if( entry.getLastModified() < files[i].lastModified() ) {
               obj = (NamedObject)parse(files[i], NamedObject.class );
               if( obj == null ) {
                  entry.setName( generateUniqueName("Unknown") );
                  entry.setCategory( "Error" );
                  entry.setLastModified( files[i].lastModified() );
                  entry.setType( "Error" );
                  tocModified = true;
               } else {
                  
                  if(obj.getName() == null || obj.getName().length() == 0 ) {
                     obj.setName(generateUniqueName("Unknown"));
                  }
                  TOCEntry tmpEntry = this.getTOCEntryByName( obj.getName() );
                  if( tmpEntry == null ) {
                     entry.setName( obj.getName() );
                  } else if( tmpEntry != entry ) {
                     // We a have an entry with a dupliation name
                     obj.setName(generateUniqueName( obj.getName()));
                     this.save(obj, files[i]);
                     entry.setName( obj.getName() );
                  } else {
                     entry.setName(obj.getName());
                  }
                  
                  entry.setCategory( obj.getCategory() );
                  entry.setImgURL( obj.getImgURL() );
                  entry.setType( obj.getType() );
                  entry.setLastModified( files[i].lastModified() );
                  entry.setDescription(obj.getDescription());
                  tocModified = true;
               }
            }
            if( newEntry ) {
               toc.getEntry().add( entry );  
               tocModified = true;
            }
         }

         SGELog.fine( "Found {0} removed files", new Integer( tmpList.size() ) );
         Iterator iter = tmpList.iterator();
         while( iter.hasNext() ) {
            entry = (TOCEntry)iter.next();
            SGELog.info( "remove entry " + entry.getName() );
            getTOC().getEntry().remove( entry );
            tocModified = true;
         }
         if( tocModified ) {
            save( toc, tocFile );
         }
      } else {
         SGELog.fine( "No xml files found");
      }
   }
   
   private String generateUniqueName( String name ) throws ArcoException {
      String currentName = name;
      int i = 0;
      while( this.getTOCEntryByName( currentName ) != null ) {
         i++;
         currentName = name + "("+ i + ")";
      }
      return currentName;
   }
   
   private TOCEntry createTOCEntry() {
      try {
         return getObjectFactory().createTOCEntry();
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException( "Can't create instancoef TOCEntry");
         ilse.initCause( jaxbe );
         throw ilse;
      }               
   }
}   
   
