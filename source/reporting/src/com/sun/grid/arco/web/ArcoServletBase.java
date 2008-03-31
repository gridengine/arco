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
package com.sun.grid.arco.web;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.StringTokenizer;
import com.sun.web.common.ConsoleServletBase;
import com.iplanet.jato.RequestContext;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.ModelManager;
import com.iplanet.jato.RequestContextImpl;
import com.iplanet.sso.*;
import com.sun.grid.arco.ArcoException;
import javax.security.auth.Subject;
import java.util.logging.*;
import java.util.*;
import com.sun.management.services.common.ConsoleConfiguration;
import com.sun.management.services.registration.*;
import com.sun.grid.logging.*;
import com.sun.grid.arco.sql.ArcoDbConnectionPool;
import com.sun.grid.arco.QueryManager;
import com.sun.grid.arco.ResultManager;
import com.sun.grid.arco.model.Configuration;
import java.security.Principal;

public class ArcoServletBase extends ConsoleServletBase implements com.sun.grid.arco.ArcoVersion {
   
   public final static String PROPERTY_LOGGING_FILTER = "arco_logging_filter";
   public final static String PROPERTY_LOGGING_LEVEL = "arco_logging_level";
   public final static String PROPERTY_CONFIG_FILE ="arco_config_file";
   public final static String PROPERTY_APP_DIR = "arco_app_dir";
   
   public static final String ATTR_QUERY_MANAGER = "queryManager";
   public static final String ATTR_RESULT_MANAGER = "resultManager";
   public static final String ATTR_CONNECTION_POOL = "connectionPool";
   public static final String ATTR_RESULT_EXPORT_MANAGER = "resultExportManager";
   
   private String configFile;
   
   private static ModelTypeMapImpl  MODEL_TYPE_MAP;
   
   public final static String APPL_NAME = PLUGIN_NAME + "_" + VERSION;
   
   private static File applDir;
   
   public static File getApplDir() {
      if (applDir == null) {
         synchronized (ArcoServletBase.class) {
            if (applDir == null) {
               String applStr = ConsoleConfiguration.getEnvProperty(PROPERTY_APP_DIR);
               
               if (applStr == null) {
                  throw new IllegalStateException("application " + APPL_NAME + " is not registered");
               }
               applDir = new File(applStr);
            }
         }
      }
      return applDir;
   }
   
   
   
   /** Initializes the servlet.  */
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      
      this.initLogging();
      
      this.initConfigFile();
      
      initManagers();
      
      reinitLogging();
      
      
      SGELog.info( "application {0} is installed at {1}", APPL_NAME, getApplDir().getAbsoluteFile() );
      
      MODEL_TYPE_MAP=new ModelTypeMapImpl();
      
      
   }
   
   
   
   public String getConfigFile() {
      return configFile;
   }
   
   private void initConfigFile() throws ServletException {
      // Setup the configuration file
      configFile= ConsoleConfiguration.getEnvProperty( PROPERTY_CONFIG_FILE );
      
      
      if( configFile == null ) {
         throw new ServletException( "property " + PROPERTY_CONFIG_FILE + " not found" );
      }
      
      File file = null;
      if( !configFile.startsWith( File.separator ) ) {
         file = new File( this.getApplDir(), configFile );
      } else {
         file = new File( configFile );
      }
      
      if( !file.exists() || !file.canRead() ) {
         throw new ServletException( "Can't open config file '" + configFile +
               "', check the property " + PROPERTY_CONFIG_FILE );
      }
      configFile = file.getAbsolutePath();
   }
   
   private com.sun.grid.arco.model.Logging getLoggingInternal() {
      
      ServletContext sc = getServletContext();
      com.sun.grid.arco.model.Logging ret = null;
      synchronized(sc) {
         ret = (com.sun.grid.arco.model.Logging)sc.getAttribute("LOGGING");
         if( ret == null ) {
            ret = loadLogging();
            sc.setAttribute("LOGGING", ret);
         }
      }
      return ret;
   }
   
   public static ArcoServletBase getCurrentInstance() {
      return (ArcoServletBase)RequestManager.getHandlingServlet();
   }
   
   public com.sun.grid.arco.model.Logging getLogging() {
      try {
         com.sun.grid.arco.model.Logging ret = getLoggingInternal();
         ret = (com.sun.grid.arco.model.Logging)
         com.sun.grid.arco.Util.clone(ret);
         return ret;
      } catch( CloneNotSupportedException cnse ) {
         IllegalStateException ilse = new IllegalStateException("Clone Error: " + cnse.getMessage() );
         ilse.initCause(cnse);
         throw ilse;
      }
   }
   
   
   public void setLogging( com.sun.grid.arco.model.Logging logging ) throws ArcoException {
      
      try {
         com.sun.grid.arco.model.Logging newLogging =
               (com.sun.grid.arco.model.Logging)
               com.sun.grid.arco.Util.clone(logging);
         
         ServletContext sc = getServletConfig().getServletContext();
         
         synchronized(sc) {
            sc.setAttribute("LOGGING", newLogging);
            reinitLogging();
            saveLogging(newLogging);
         }
      } catch( CloneNotSupportedException cnse ) {
         IllegalStateException ilse = new IllegalStateException("Clone Error: " + cnse.getMessage() );
         ilse.initCause(cnse);
         throw ilse;
      }
   }
   
   private File getLoggingFile() {
      com.sun.grid.arco.model.Configuration conf = getConfiguration();
      File spoolDir = new File(conf.getStorage().getRoot());
      return new File(spoolDir,"logging.xml");
   }
   
   private void saveLogging(com.sun.grid.arco.model.Logging logging ) throws ArcoException {
      
      File loggingFile = getLoggingFile();
      
      getQueryManager().save(logging, loggingFile);
   }
   
   
   private com.sun.grid.arco.model.Logging loadLogging() {
      
      File loggingFile = getLoggingFile();
      
      com.sun.grid.arco.model.Logging ret = null;
      try {
         ret = (com.sun.grid.arco.model.Logging)getQueryManager().parse(loggingFile);
      } catch( Exception e ) {
         
         // Error while loading the logging configuration
         // Create a default logging
         try {
            ret = getQueryManager().getObjectFactory().createLogging();
            ret.setLevel( Level.INFO.toString() );
         } catch( javax.xml.bind.JAXBException jaxbe ) {
            IllegalStateException ilse = new IllegalStateException("Can't create default logging: " + jaxbe.getMessage());
            ilse.initCause(jaxbe);
            throw ilse;
         }
      }
      
      return ret;
      
   }
   
   private void reinitLogging() {
      
      com.sun.grid.arco.model.Logging logging = getLoggingInternal();
      
      if( logging != null ) {
         Logger logger = Logger.getLogger( "arcoLogger" );
         String str = logging.getLevel();
         if( str != null ) {
            Level level = Level.parse(logging.getLevel());
            
            SGELog.info("set general log level to {0}", level);
            
            logger.setLevel(level);
            
            Handler [] handlers = logger.getHandlers();
            for( int i = 0; i < handlers.length; i++ ) {
               handlers[i].setLevel(level);
            }
         }
         java.util.List filterList = logging.getFilter();
         
         java.util.logging.Filter filter = null;
         com.sun.grid.arco.model.LoggingFilter f = null;
         
         java.util.Iterator iter = filterList.iterator();
         
         
         java.util.ArrayList activeFilters = new java.util.ArrayList();
         while( iter.hasNext() ) {
            f = (com.sun.grid.arco.model.LoggingFilter)iter.next();
            if(f.isActive()) {
               filter = new RegExFilter(f.getClassPattern(),
                     f.getMethodPattern(),
                     Level.parse(f.getLevel()));
               
               SGELog.info("add log filter {0}", filter );
               activeFilters.add(filter);
            }
         }
         
         
         
         if( activeFilters.size() == 1 ) {
            filter = (java.util.logging.Filter)activeFilters.get(0);
         } else if ( activeFilters.size() > 0 ) {
            CompositeFilter compFilter = new CompositeFilter( activeFilters.size() );
            
            iter = activeFilters.iterator();
            while( iter.hasNext() ) {
               compFilter.addFilter( (java.util.logging.Filter)iter.next() );
            }
            filter = compFilter;
         }
         logger.setFilter(filter);
      }
   }
   
   
   
   private void initLogging() {
      
      Logger logger = Logger.getLogger( "arcoLogger" );
      
      ConsoleHandler consoleHandler = new ConsoleHandler();
      java.util.logging.Formatter  formatter = new com.sun.grid.logging.SGEFormatter( "arco", true );
      
      consoleHandler.setFormatter( formatter );
      logger.addHandler( consoleHandler );
      logger.setUseParentHandlers( false );
      
      com.sun.grid.logging.SGELog.init( logger );
      
      
      String levelStr = ConsoleConfiguration.getEnvProperty( PROPERTY_LOGGING_LEVEL );
      Level level;
      if( levelStr == null ) {
         level = Level.INFO;
      } else {
         try {
            level = Level.parse( levelStr.toUpperCase() );
         } catch ( IllegalArgumentException ilse  ) {
            com.sun.grid.logging.SGELog.warning("system report PROPERTY_LOGGING_LEVEL does not define a valid log level" );
            level = Level.INFO;
         }
      }
      com.sun.grid.logging.SGELog.info( "Set logging level to {0}" , level );
      logger.setLevel( level  );
      consoleHandler.setLevel( level );
      
      // Filter
      
      String filterStr = ConsoleConfiguration.getEnvProperty( PROPERTY_LOGGING_FILTER );
      if( filterStr != null ) {
         
         StringTokenizer st = new StringTokenizer( filterStr, ";");
         
         if( st.countTokens() > 1 ) {
            CompositeFilter compFilter = new CompositeFilter( st.countTokens() );
            
            while( st.hasMoreTokens() ) {
               compFilter.addFilter( parseRegExFilter( st.nextToken() ) );
            }
            logger.setFilter( compFilter );
         } else if ( st.countTokens() == 1 ) {
            logger.setFilter( parseRegExFilter( st.nextToken() ) );
         }
      }
      
   }
   
   private static RegExFilter parseRegExFilter( String filter ) {
      RegExFilter ret = new RegExFilter();
      StringTokenizer st = new StringTokenizer( filter, "," );
      String token = null;
      if( st.hasMoreTokens() ) {
         token = st.nextToken();
         if( !token.equals( "*" ) ) {
            ret.setSourceClassPattern( token );
         }
         
         if( st.hasMoreTokens() ) {
            token = st.nextToken();
            if( !token.equals( "*" ) ) {
               ret.setSourceMethodPattern( token );
            }
            if( st.hasMoreTokens() ) {
               token = st.nextToken();
               if( !token.equals( "*" ) ) {
                  ret.setLevel( Level.parse( token ) );
               }
            }
         }
      }
      return ret;
   }
   
   
   protected void initializeRequestContext(RequestContext requestContext) {
      
      super.initializeRequestContext(requestContext);
      
      ModelManager modelManager=
            new ModelManager(requestContext,MODEL_TYPE_MAP);
      ((RequestContextImpl)requestContext).setModelManager(modelManager);
      
   }
   
   private static boolean managersIntialied = false;
   
   private void initManagers() {
      
      synchronized( ArcoServletBase.class) {
         if( !managersIntialied ) {
            try {
               SGELog.fine( "initialize the query and result manager" );
               
               ArcoDbConnectionPool cp = ArcoDbConnectionPool.getInstance();
               
               cp.setConfigurationFile(getConfigFile());
               
               getServletContext().setAttribute(ATTR_CONNECTION_POOL, cp );
               
               
               com.sun.grid.arco.model.StorageType storage = cp.getConfig().getStorage();
               
               File storageDir = new File( storage.getRoot() );
               File queryDir = new File( storageDir, storage.getQueries() );
               File resultDir = new File( storageDir, storage.getResults() );
               
               QueryManager.createInstance( queryDir, getClass().getClassLoader() );
               getServletContext().setAttribute( ATTR_QUERY_MANAGER, QueryManager.getInstance() );
               
               ResultManager.createInstance(resultDir, getClass().getClassLoader());
               getServletContext().setAttribute( ATTR_RESULT_MANAGER, ResultManager.getInstance() );
               
               
               com.sun.grid.arco.ResultExportManager rem = new com.sun.grid.arco.ResultExportManager(getApplDir());
               
               getServletContext().setAttribute( ATTR_RESULT_EXPORT_MANAGER, rem );
               
               managersIntialied = true;
               SGELog.fine( "the query and result manager successfully initialized" );
            } catch( Exception e ) {
               SGELog.severe( e, "Exception occured {0}", e );
            }
         }
      }
   }
   
   
   /** Returns a short description of the servlet.
    */
   public String getServletInfo() { return "ArcoServletBase"; }
   
   public QueryManager getQueryManager() {
      ServletContext sc = getServletContext();
      return (QueryManager)sc.getAttribute(ATTR_QUERY_MANAGER);
   }
   
   public ResultManager getResultManager() {
      ServletContext sc = getServletContext();
      return (ResultManager)sc.getAttribute(ATTR_RESULT_MANAGER);
   }
   
   public ArcoDbConnectionPool getConnectionPool() {
      ServletContext sc = getServletContext();
      return (ArcoDbConnectionPool)sc.getAttribute(ATTR_CONNECTION_POOL);
   }
   
   public com.sun.grid.arco.ResultExportManager getResultExportManager() {
      ServletContext sc = getServletContext();
      return (com.sun.grid.arco.ResultExportManager)sc.getAttribute(ATTR_RESULT_EXPORT_MANAGER);
   }
   
   protected com.sun.grid.arco.model.Configuration getConfiguration() {
      return getConnectionPool().getConfig();
   }
   
   /**
    * Determine if the current user has write permission for ARCo
    * @return  true the current user has write permission
    */
   public boolean hasUserWritePermission() {
      
      
      HttpServletRequest req = RequestManager.getRequestContext().getRequest();
      
      HttpSession session = req.getSession();
      
      Boolean ret = (Boolean)req.getSession().getAttribute("WRITE_PERMISSON");
      
      if( ret == null ) {
         
         try {
            SSOTokenManager sSOTokenManager = SSOTokenManager.getInstance();
            SSOToken sSOToken = sSOTokenManager.createSSOToken(req);
            Subject subject = sSOToken.getSubject();
            
            ret = Boolean.FALSE;
            if( !subject.getPrincipals().isEmpty() ) {
               Principal princ = (Principal)subject.getPrincipals().iterator().next();
               
               String user = princ.getName();
               
               Configuration config = getConfiguration();
               
               List applUserList = config.getApplUser();
               
               Iterator iter = applUserList.iterator();
               
               String applUser = null;
               
               while( iter.hasNext() ) {
                  applUser = (String)iter.next();
                 //The xml allow new lines and othe whitespaces 
                  if( applUser.trim().equals(user.trim())) {
                     ret = Boolean.TRUE;
                     break;
                  }
               }
            }
         } catch( com.iplanet.sso.SSOException sose ) {
            SGELog.severe(sose, "Can''t determine user: {0}", sose.getMessage() );
         }
         session.setAttribute("WRITE_PERMISSON", ret );
      }
      
      return ret.booleanValue();
   }
   
   
   public com.sun.grid.arco.sql.SQLGenerator getSQLGenerator() {
      return getConnectionPool().getSQLGenerator();
   }
   
   protected void processRequest(String pageName, HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
      
      long start = System.currentTimeMillis();
      
      if( SGELog.isLoggable(Level.FINE)) {
         SGELog.fine("pageName={0} ----------------------", pageName );
         java.util.Enumeration en = request.getParameterNames();
         String param = null;
         ArrayList names = new ArrayList();
         
         while(en.hasMoreElements()) {
            names.add(en.nextElement());
         }
         Collections.sort(names);
         Iterator iter = names.iterator();
         String values [] = null;
         while(iter.hasNext()) {
            param = (String)iter.next();
            values = request.getParameterValues(param);
            for( int i = 0; i < values.length; i++ ) {
               SGELog.fine("param {0}["+i+"]={1}", param, request.getParameter(param));
            }
         }
      }
      super.processRequest(pageName, request, response);
      
      if( SGELog.isLoggable(Level.CONFIG )) {
         double diff = ((double)System.currentTimeMillis() - start) / 1000;
         SGELog.config("request for page " + pageName + " executed in " + diff + "s");
      }
   }
   
   protected void onPageSessionDeserializationException(RequestContext requestContext, com.iplanet.jato.view.ViewBean viewBean, Exception e) throws ServletException, IOException {
      SGELog.severe(e, "page session deserialization exception: {0}", e );
   }
   
}
//##############################################################################
