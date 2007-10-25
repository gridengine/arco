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

import com.sun.grid.reporting.dbwriter.event.CommitEvent;
import com.sun.grid.reporting.dbwriter.event.ParserEvent;
import java.sql.*;
import java.io.*;
import java.util.logging.*;
import java.util.*;
import com.sun.grid.reporting.dbwriter.file.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.logging.SGEFormatter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import com.sun.grid.reporting.dbwriter.db.*;

import com.sun.grid.reporting.dbwriter.model.*;


public class ReportingDBWriter extends Thread {
   
   /** join timeout for the shutdown (20 seconds). */
   public static final long JOIN_TIMEOUT         = 20 * 1000;
   
   /** The required version of the database model */
   public static final Integer REQUIRED_DB_MODEL_VERSION = new Integer(4);
   
   /** Current version of the database model */
   public static final Integer CURRENT_DB_MODEL_VERSION = new Integer(4);
   
   /** prefix for all environment variables */
   public final static String ENV_PRE              = "DBWRITER_";
   
   // db connection parameters
   public final static String ENV_USER             = ENV_PRE + "USER";
   public final static String ENV_USER_PW          = ENV_PRE + "USER_PW";
   public final static String ENV_URL              = ENV_PRE + "URL";
   public final static String ENV_DRIVER           = ENV_PRE + "DRIVER";
   
   // Files
   public final static String ENV_ACCOUNTING_FILE  = ENV_PRE + "ACCOUNTING_FILE";
   public final static String ENV_CALC_FILE        = ENV_PRE + "CALCULATION_FILE";
   public final static String ENV_REPORTING_FILE   = ENV_PRE + "REPORTING_FILE";
   public final static String ENV_SHARE_LOG_FILE   = ENV_PRE + "SHARE_LOG_FILE";
   public final static String ENV_STATISTIC_FILE   = ENV_PRE + "STATISTIC_FILE";
   public static final String ENV_PID_FILE         = ENV_PRE + "PID_FILE";
   
   // misc
   public final static String ENV_INTERVAL         = ENV_PRE + "INTERVAL";
   public final static String ENV_CONTINOUS        = ENV_PRE + "CONTINOUS";
   public final static String ENV_DEBUG            = ENV_PRE + "DEBUG";
   
   public static final String ENV_SQL_THRESHOLD   = ENV_PRE + "SQL_THRESHOLD";
   
   /**
    *  Name of the thread which imports the values from the files.
    */
   public static final String REPORTING_THREAD_NAME = "dbwriter";
   
   /**
    *  Name of the thread which calculates executes derived value rules and
    *  deletion rules.
    */
   public static final String DERIVED_THREAD_NAME   = "derived";
   
   /**
    *  Name of the thread which executes the vacuum analyze
    */
   public static final String VACUUM_THREAD_NAME    = "vacuum";
   
   public static final String STATISTIC_THREAD_NAME = "statistic";
   
   /** the properties*/
   private static Properties props;
   private final static String RESOURCEBUNDLE_NAME = "com.sun.grid.reporting.dbwriter.Resources";
   
   private static ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCEBUNDLE_NAME);
   
   // ------------ Members ----------------------------------------------------------------
   private int pid = -1;
   private File   pidFile         = null;
   private boolean pidFileWritten = false;
   
   private String driver = null;
   private String url    = null;
   private String logFile = null;
   private String debugLevel = null;
   private String accountingFile  = null;
   private String statisticsFile  = null;
   private String sharelogFile    = null;
   private String reportingFile   = null;
   private String calculationFile = null;
   private String userName        = System.getProperty("user.name");
   private String userPW          = "";
   private boolean continous      = false;
   private boolean vacuum        = true;
   
   public static final String DEFAULT_VACUUM_SCHEDULE = "+1 0 11 0";
   /**
    *  The schedule for the next vaccum default (every day at 00:11:00)
    */
   private TimeSchedule vacuumSchedule = new TimeSchedule(DEFAULT_VACUUM_SCHEDULE);
   
   private int interval           = 60;
   private int sqlExecThreshold   = 0;
   
   private Database database = new Database();
   
   private FileParser[] readers;
   
   private StoredRecordManager jobManager = null;
   private RecordManager jobLogManager = null;
   private StoredRecordManager queueManager = null;
   private StoredRecordManager hostManager = null;
   private StoredRecordManager departmentManager = null;
   private StoredRecordManager projectManager = null;
   private StoredRecordManager userManager = null;
   private StoredRecordManager groupManager = null;
   private StoredRecordManager newARManager = null;
   private StatisticManager statisticManager = null;
   private RecordManager sharelogManager = null;
   private RecordManager arAttrManager = null;
   private GeneralManager generalManager = null;
   
   private ValueRecordManager queueValueManager = null;
   private ValueRecordManager hostValueManager = null;
   private ValueRecordManager departmentValueManager = null;
   private ValueRecordManager projectValueManager = null;
   private ValueRecordManager userValueManager = null;
   private ValueRecordManager groupValueManager = null;
   private ValueRecordManager statisticValueManager = null;
   
   
   private Logger logger;
   private Handler handler;
   private com.sun.grid.logging.SGEFormatter formatter;
   
   private DerivedValueThread derivedValueThread = new DerivedValueThread();
   private VacuumAnalyzeThread vacuumAnalyzeThread = new VacuumAnalyzeThread();
   private StatisticThread statisticThread = new StatisticThread();
   
   private ThreadGroup threadGroup;
   
   /**
    *  Create a new ReportingDBWriter
    *  @param tg  all threads which are created by the dbwriter will be
    *             members of this thread group
    */
   public ReportingDBWriter(ThreadGroup tg) {
      super(tg, REPORTING_THREAD_NAME);
      this.threadGroup = tg;
   }
   
   /** Creates a new instance of ReportingDBWriter */
   public ReportingDBWriter() {
      this( new ThreadGroup("dbwriter") );
   }
   
   /**
    *   get the thread group of all threads which are started
    *   by the dbwriter.
    *   @return  the thread group of the dbwriter (returns not
    *            <code>null</code> even if the dbwriter thread
    *            has died
    *   @see     java.lang.Thread#getThreadGroup
    */
   public ThreadGroup getDbWriterThreadGroup() {
      return this.threadGroup;
   }
   
   
   /**
    * the set debug level for the dbwriter
    * @param level the debug level
    * @throws IllegalArgumentException if the level is invalid
    */
   void setDebugLevel(String level) {
      setDebugLevel(Level.parse(level));
   }
   
   public void setDebugLevel( Level level ) {
      logger.setLevel(level);
      handler.setLevel( level );
   }
   
   public Level getDebugLevel() {
      return logger.getLevel();
   }
   
   private void parseCommandLine(String argv[]) {
      // parse commandline
      for (int i = 0; i < argv.length; i++) {
         try {
            if( argv[i].equals( "-logfile" ) ) {
               logFile = argv[++i];
            } else if (argv[i].compareTo("-accounting") == 0) {
               accountingFile = argv[++i];
            } else if (argv[i].compareTo("-calculation") == 0) {
               calculationFile = argv[++i];
            } else if (argv[i].compareTo("-continous") == 0) {
               continous = true;
            } else if (argv[i].compareTo("-debug") == 0) {
               debugLevel = argv[++i];
            } else if (argv[i].compareTo("-driver") == 0) {
               driver = argv[++i];
            } else if (argv[i].compareTo("-pid") == 0 ) {
               pidFile = new File(argv[++i]);
            } else if (argv[i].compareTo("-interval") == 0) {
               interval = Integer.parseInt(argv[++i]);
            } else if (argv[i].compareTo("-user") == 0) {
               userName = argv[++i];
            } else if (argv[i].compareTo("-reporting") == 0) {
               reportingFile = argv[++i];
            } else if (argv[i].compareTo("-sharelog") == 0) {
               sharelogFile = argv[++i];
            } else if (argv[i].compareTo("-statistics") == 0) {
               statisticsFile = argv[++i];
            } else if (argv[i].compareTo("-url") == 0) {
               url = argv[++i];
            } else if (argv[i].compareTo("-sqlThreshold") == 0) {
               sqlExecThreshold = Integer.parseInt(argv[++i]) * 1000;
            } else if (argv[i].compareTo("-vs") == 0 ) {
               try {
                  setVacuumSchedule(argv[++i]);
               } catch(Exception e) {
                  usage("invalid vacuumSchedule '" + argv[i] + "'", true);
               }
            } else if (argv[i].compareTo("-help") == 0) {
               usage(null, true);
            } else if ( argv[i].compareTo("-props") == 0) {
               // ignore this
               i++;
            } else {
               usage("unknown option " + argv[i], true);
            }
         } catch (ArrayIndexOutOfBoundsException e) {
            usage("option " + argv[i-1] + " requires an argument", true);
         }
      }
      
      // now check if all params have been passed
      if (driver == null) {
         usage("option -driver has to be specified", true);
      }
      
      if (url == null) {
         usage("option -url has to be specified", true);
      }
   }
   
   /**
    *   Check the parameters which was set by environment variables or
    *   through application parameters
    */
   private void checkParams() {
      if (accountingFile == null &&
            statisticsFile == null &&
            sharelogFile   == null &&
            reportingFile  == null) {
         usage("any input file has to be specified", true);
      }
      
      if (reportingFile != null &&
            (accountingFile != null || statisticsFile != null)) {
         usage("don't specify both 6.0 and 5.3 files", true);
      }
      
      if( userName == null ) {
         usage( "no db user specified", true );
      }
      if( userPW == null ) {
         usage( "no password for the db user specified", true );
      }
      
      if( driver == null ) {
         usage( "no db driver specified", true );
      }
      
      if( url == null ) {
         usage( "no db url specified", true );
      }
      
      
   }
   
   
   void initLogging() {
      if( debugLevel != null ) {
         initLogging(debugLevel);
      } else {
         initLogging( Level.INFO.toString() );
      }
   }
   
   /**
    *  init the logging system of the dbwriter
    *  @param level the log level
    */
   void initLogging(String level) {
      
      
      logger = Logger.getAnonymousLogger(RESOURCEBUNDLE_NAME);
      
      if( logFile == null ) {
         handler = new ConsoleHandler();
      } else {
         try {
            handler = new FileHandler(logFile, true);
         } catch( IOException ioe ) {
            System.err.println("Can't create log file " + logFile );
            System.err.println(ioe.getMessage());
            System.exit(1);
         }
      }
      
      formatter = new SGEFormatter( "dbwriter", true );
      handler.setFormatter( formatter );
      logger.addHandler(handler);
      
      setDebugLevel(level);
      logger.setUseParentHandlers( false );
      
      SGELog.init(logger);
      SGELog.info("ReportingDBWriter.start", Version.PRODUCT, Version.APPLICATION, Version.VERSION );
   }
   
   public void setLogWithStackTrace(boolean withStacktrace) {
      formatter.setWithStackTrace(withStacktrace);
   }
   
   public boolean getLogWithStackTrace() {
      return formatter.getWithStackTrace();
   }
   
   /**
    *  Close the logging. After calling this method the handler will
    *  not write any log message into the log file.
    *  the <code>writeDirectLog</code> message can be used to write log messages
    */
   public void closeLogging() {
      if( handler != null ) {
         try {
            handler.flush();
            handler.close();
         } catch( SecurityException se ) {
         }
      }
   }
   
   /**
    * @param args the command line arguments
    * @throws ReportingException in the case of invalid configuration parameters
    */
   public void initialize(String argv[]) throws ReportingException {
      
      long time = System.currentTimeMillis();
      
      
      // first read options from stdin
      getOptionFromStdin();
      
      // parse the comand line
      parseCommandLine(argv);
      
      checkParams();
      
      // First add a shutdown hook
      Runtime.getRuntime().addShutdownHook(new ShutdownHandler(this));
      
      initLogging();
      
      
   }
   
   /**
    *    Initialize all members
    */
   void initialize() throws ReportingException {
      
      if( sqlExecThreshold > 0 ) {
         SGELog.info("sql execute threshold is " + sqlExecThreshold/1000 + " seconds" );
      }
      SGELog.info("Connection to db " + url);
      database.init(driver, url, userName, userPW, sqlExecThreshold);
      
      if(!database.test()) {
         throw new ReportingException("ReportingDBWriter.dbtestFailed", url);
      }
      
      Integer dbModelVersion = new Integer(database.getDBModelVersion());
      SGELog.info("ReportingDBWriter.dbModel",  dbModelVersion);
      
      if(dbModelVersion.intValue() < REQUIRED_DB_MODEL_VERSION.intValue()) {
         throw new ReportingException("ReportingDBWriter.invalidDatabaseModel", dbModelVersion, REQUIRED_DB_MODEL_VERSION );
      }
      if(dbModelVersion.intValue() < CURRENT_DB_MODEL_VERSION.intValue()) {
         SGELog.warning("Reporting.dbModelUpdate", dbModelVersion, CURRENT_DB_MODEL_VERSION);
      }
      
      jobLogManager = new JobLogManager(database);
      
      jobManager = new JobManager(database, jobLogManager);
      newARManager = new AdvanceReservationManager(database);
      
      queueValueManager = new QueueValueManager(database);
      hostValueManager = new HostValueManager(database);
      departmentValueManager = new DepartmentValueManager(database);
      projectValueManager = new ProjectValueManager(database);
      userValueManager = new UserValueManager(database);
      groupValueManager = new GroupValueManager(database);
      statisticValueManager = new StatisticValueManager(database);
      
      queueManager = new QueueManager(database, queueValueManager);
      hostManager = new HostManager(database, hostValueManager);
      departmentManager = new DepartmentManager(database, departmentValueManager);
      projectManager = new ProjectManager(database, projectValueManager);
      userManager = new UserManager(database, userValueManager);
      groupManager = new GroupManager(database, groupValueManager);
      statisticManager = new StatisticManager(database, statisticValueManager);
      
      queueValueManager.setParentManager(queueManager);
      hostValueManager.setParentManager(hostManager);
      departmentValueManager.setParentManager(departmentManager);
      projectValueManager.setParentManager(projectManager);
      userValueManager.setParentManager(userManager);
      groupValueManager.setParentManager(groupManager);
      statisticValueManager.setParentManager(statisticManager);
      
      sharelogManager = new ShareLogManager(database);
      
      readers = new FileParser[4];
      
      if (accountingFile != null) {
         AccountingFileParser accountingFileReader = new AccountingFileParser(accountingFile, ":");
         accountingFileReader.addNewObjectListener(jobManager);
         accountingFileReader.addNewObjectListener(queueManager);
         accountingFileReader.addNewObjectListener(hostManager);
         accountingFileReader.addNewObjectListener(projectManager);
         accountingFileReader.addNewObjectListener(userManager);
         accountingFileReader.addNewObjectListener(departmentManager);
         accountingFileReader.addNewObjectListener(groupManager);
         accountingFileReader.addNewObjectListener(statisticManager);
         readers[0] = accountingFileReader;
      }
      
      if (statisticsFile != null) {
         StatisticsFileParser statisticsFileReader = new StatisticsFileParser(statisticsFile, ":");
         statisticsFileReader.addNewObjectListener(queueManager);
         statisticsFileReader.addNewObjectListener(hostManager);
         statisticsFileReader.addNewObjectListener(statisticManager);
         readers[1] = statisticsFileReader;
      }
      
      if (sharelogFile != null) {
         ShareLogFileParser sharelogFileReader   = new ShareLogFileParser(sharelogFile, ":");
         sharelogFileReader.addNewObjectListener(projectManager);
         sharelogFileReader.addNewObjectListener(userManager);
         sharelogFileReader.addNewObjectListener(sharelogManager);
         sharelogFileReader.addNewObjectListener(statisticManager);
         readers[2] = sharelogFileReader;
      }
      
      if (reportingFile != null) {
         GeneralManager generalManager = new GeneralManager(database);
         generalManager.addNewObjectListener(jobManager, "acct");
         generalManager.addNewObjectListener(queueManager, "acct");
         generalManager.addNewObjectListener(hostManager, "acct");
         generalManager.addNewObjectListener(projectManager, "acct");
         generalManager.addNewObjectListener(departmentManager, "acct");
         generalManager.addNewObjectListener(userManager, "acct");
         generalManager.addNewObjectListener(groupManager, "acct");
         
         generalManager.addNewObjectListener(hostManager, "host");
         generalManager.addNewObjectListener(hostManager, "host_consumable");
         
         generalManager.addNewObjectListener(queueManager, "queue");
         generalManager.addNewObjectListener(queueManager, "queue_consumable");
         
         generalManager.addNewObjectListener(jobManager, "new_job");
         generalManager.addNewObjectListener(jobManager, "job_log");
         
         generalManager.addNewObjectListener(projectManager, "sharelog");
         generalManager.addNewObjectListener(userManager, "sharelog");
         generalManager.addNewObjectListener(sharelogManager, "sharelog");
         generalManager.addNewObjectListener(statisticManager, "statistic");
         
         generalManager.addNewObjectListener(newARManager, "new_ar");
         generalManager.addNewObjectListener(newARManager, "ar_attr");
         generalManager.addNewObjectListener(newARManager, "ar_log");
         generalManager.addNewObjectListener(newARManager, "ar_acct");
         
         
         ReportingFileParser reportingFileReader = new ReportingFileParser(reportingFile, ":");
         reportingFileReader.addNewObjectListener(generalManager);
         reportingFileReader.addNewObjectListener(derivedValueThread);
         readers[3] = reportingFileReader;
      }
   }
   
   private static void usage(String message, boolean exit) {
      if (message != null) {
         System.err.println(message);
         System.err.println();
      }
      
      System.out.println(resourceBundle.getString("ReportingDBWriter.usage"));
      
      if (exit) {
         System.exit(1);
      }
   }
   
   public StoredRecordManager getDerivedValueManager(String name) {
      StoredRecordManager manager = null;
      
      if (name.compareTo("host") == 0) {
         manager = hostManager;
      } else if (name.compareTo("queue") == 0) {
         manager = queueManager;
      } else if (name.compareTo("project") == 0) {
         manager = projectManager;
      } else if (name.compareTo("department") == 0) {
         manager = departmentManager;
      } else if (name.compareTo("user") == 0) {
         manager = userManager;
      } else if (name.compareTo("group") == 0) {
         manager = groupManager;
      } else if (name.compareTo("statistic") == 0) {
         manager = statisticManager;
      } else {
         SGELog.warning( "ReportingDBWriter.invalidObjectClass", name );
      }
      
      return manager;
   }
   
   public RecordManager getDeleteManager(String name) {
      RecordManager manager = null;
      
      if (name.compareTo("host_values") == 0) {
         manager = hostValueManager;
      } else if (name.compareTo("queue_values") == 0) {
         manager = queueValueManager;
      } else if (name.compareTo("project_values") == 0) {
         manager = projectValueManager;
      } else if (name.compareTo("department_values") == 0) {
         manager = departmentValueManager;
      } else if (name.compareTo("user_values") == 0) {
         manager = userValueManager;
      } else if (name.compareTo("group_values") == 0) {
         manager = groupValueManager;
      } else if (name.compareTo("job") == 0) {
         manager = jobManager;
      } else if (name.compareTo("job_log") == 0) {
         manager = jobLogManager;
      } else if (name.compareTo("share_log") == 0) {
         manager = sharelogManager;
      } else if (name.compareTo("statistic_values") == 0) {
         manager = statisticValueManager;
      } else if (name.compareTo("ar_values") == 0) {
         manager = newARManager;
      } else {
         SGELog.warning( "ReportingDBWriter.invalidObjectClass", name );
      }
      
      return manager;
   }
   
   /** configuration object (content of the calculation file. */
   private DbWriterConfig config = null;
   /* timestamp of the last read attempt if the calculation file. */
   private long configTimestamp;
   
   /**
    * Get the configuration of the dervied value rules and the deletion
    * rules.
    * The configuration is cached. If the timestamp of the calculation file
    * has changed the configuration will be read. If the new configuartion
    * can't be read the last configuration will be used. A log message
    * will be written.
    * @throws com.sun.grid.reporting.dbwriter.ReportingException
    * @return the configuration object or <code>null</code> of the path
    *         the the calculation file is not defined.
    */
   public DbWriterConfig getDbWriterConfig() throws ReportingException {
      
      if( calculationFile == null ) {
         return null;
      }
      File calcFile = new File( calculationFile );
      long ts = calcFile.lastModified();
      DbWriterConfig oldConfig = null;
      if( ts > configTimestamp ) {
         SGELog.info("ReportingDBWriter.calcFileChanged", calculationFile);
         oldConfig = config;
         config = null;
      }
      
      if( config == null ) {
         synchronized( this ) {
            if( config == null ) {
               try {
                  JAXBContext ctx = JAXBContext.newInstance( "com.sun.grid.reporting.dbwriter.model" );
                  Unmarshaller u = ctx.createUnmarshaller();
                  config = (DbWriterConfig)u.unmarshal( calcFile );
                  configTimestamp = ts;
               } catch( JAXBException je ) {
                  ReportingException re = new ReportingException( "ReportingDBWriter.calcFileError"
                        , new Object[] { calculationFile, je.getMessage() } );
                  if( oldConfig == null ) {
                     // If no configuration is available, the dbwriter can
                     // not run. Throw an exception
                     throw re;
                  } else {
                     // Use the old configuration.
                     config = oldConfig;
                     // Update the timestamp
                     configTimestamp = ts;
                     re.log();
                  }
               }
            }
         }
      }
      return config;
   }
   
   
   public void calculateDerivedValues( java.sql.Connection connection,
         long timestampOfLastRowData  ) throws ReportingException {
      DbWriterConfig conf  = getDbWriterConfig();
      if( conf != null ) {
         DeriveRuleType rule = null;
         Iterator iter = conf.getDerive().iterator();
         
         StoredRecordManager manager = null;
         while( iter.hasNext() && !isProcessingStopped() ) {
            rule = (DeriveRuleType)iter.next();
            manager = getDerivedValueManager( rule.getObject() );
            // feed manager with rule
            if (manager != null) {
               manager.calculateDerivedValues( timestampOfLastRowData, rule, connection );
            } else {
               SGELog.warning( "No derived value rule for object {0} found", rule.getObject() );
            }
         }
      }
   }
   
   /**
    *   The statistic thread calculates the statistics defined in the statistic
    *   rules
    */
   class StatisticThread extends Thread {
      
      public StatisticThread() {
         super(ReportingDBWriter.this.getThreadGroup(), STATISTIC_THREAD_NAME );
      }
      
      public void run() {
         SGELog.entering(getClass(),"run");
         try {
            while( !ReportingDBWriter.this.isProcessingStopped() ) {
               
               Timestamp nextTimestamp = new Timestamp(System.currentTimeMillis() + 60 * 60 * 1000);
               
               DbWriterConfig conf  = getDbWriterConfig();
               if( conf != null ) {
                  
                  Connection connection = database.getConnection();
                  try {
                     Iterator iter = conf.getStatistic().iterator();
                     while(iter.hasNext()) {
                        StatisticRuleType rule = (StatisticRuleType)iter.next();
                        Timestamp nextCalc = statisticManager.getNextCalculation(rule, connection);
                        if(System.currentTimeMillis() > nextCalc.getTime()) {
                           try {
                              statisticManager.calcucateStatistic(rule, connection);
                              database.commit(connection, CommitEvent.INSERT);
                           } catch(ReportingException e) {
                              SGELog.severe(e, "ReportingDBWriter.errorInStatisticRule", rule.getVariable(), e.getLocalizedMessage());
                           }
                        } else if(nextTimestamp.getTime() > nextCalc.getTime()) {
                           nextTimestamp = nextCalc;
                        }
                     }
                  } finally {
                     database.release(connection);
                  }
               }
               
               if( nextTimestamp.getTime() > System.currentTimeMillis() ) {
                  SGELog.info("ReportingDBWriter.nextStatistic", nextTimestamp );
                  sleep( nextTimestamp.getTime() - System.currentTimeMillis()  );
               }
            }
         } catch( InterruptedException ire ) {
            // finish execution
         } catch( Throwable ex ) {
            SGELog.severe( ex, "Unknown error: {0}", ex.toString() );
         } finally {
            SGELog.exiting(getClass(),"run");
         }
      }
   }
   
   
   /**
    *  This thread executes the vacuum analyze
    */
   class VacuumAnalyzeThread extends Thread {
      
      public VacuumAnalyzeThread() {
         super( ReportingDBWriter.this.getThreadGroup(), VACUUM_THREAD_NAME );
      }
      
      
      public void run() {
         SGELog.entering(getClass(),"run");
         try {
            
            if ( vacuum && database.getType() == Database.TYPE_POSTGRES) {
               
               long startTime = System.currentTimeMillis();
               java.util.Date nextRun = null;
               
               while( !ReportingDBWriter.this.isProcessingStopped() ) {
                  
                  nextRun = vacuumSchedule.getNextTime(startTime);
                  SGELog.info("ReportingDBWriter.nextVacuum", nextRun);
                  
                  long sleepTime = nextRun.getTime() - System.currentTimeMillis();
                  if(sleepTime > 0 ) {
                     sleep(sleepTime);
                  }
                  startTime = System.currentTimeMillis();
                  
                  java.sql.Connection connection = database.getConnection();
                  
                  try {
                     //The VACUUM ANALYZE cannot be performed in the transaction,
                     //hence get the connection with autoCommit(true). If an error
                     //occurs don't do anything just write a log. If there are
                     //consistent problems it is up to the DBA to check the logs
                     //and see what is wrong with the DB. The database.release()
                     //assures that only connection with autoCommit(false) are
                     // returned to the pool.
                     connection.setAutoCommit(true);
                     SGELog.info("ReportingDBWriter.vacuumStarted");
                     database.execute("VACUUM ANALYZE", connection );
                     connection.setAutoCommit(false);
                  } catch( ReportingException re ) {
                     re.log();
                  }  catch(SQLException sql) {
                     SGELog.info("ReportingDBWriter.setAutoCommitFailed");
                  } finally {
                     database.release( connection );
                  }
                  
                  long duration = System.currentTimeMillis() - startTime;
                  
                  if (SGELog.isLoggable(Level.INFO)) {
                     long minutes = duration / (1000*60);
                     SGELog.info("ReportingDBWriter.vacuumDuration",
                           new Integer( (int)(minutes / 60)),
                           new Integer( (int)(minutes % 60)) );
                  }
               }
            }
         } catch (InterruptedException ire) {
            // Ignore
         } catch (Throwable ex) {
            SGELog.severe( ex, "Unknown error: {0}", ex.toString() );
         } finally {
            SGELog.exiting(getClass(),"run");
         }
         
      }
   }
   
   /**
    *  This thread calculates the derived values and data expired
    *  data from the database
    */
   class DerivedValueThread extends Thread implements com.sun.grid.reporting.dbwriter.event.ParserListener {
      
      /** timestamp in mills of the last imported raw data. */
      private long timestampOfLastRowData;
      
      /** the object synchronizes the drived value thread
       *  with the main thread. */
      private Object syncObject = new Object();
      
      public DerivedValueThread() {
         super( ReportingDBWriter.this.getThreadGroup(), DERIVED_THREAD_NAME );
      }
      
      public void run() {
         SGELog.entering(getClass(),"run");
         java.sql.Connection connection = null;
         try {
            
            Timestamp nextTimestamp = null;
            Timestamp lastCalcTimestamp = new Timestamp(0);
            
            // Wait until a row data are imported
            synchronized(syncObject) {
               while(timestampOfLastRowData == 0 && !ReportingDBWriter.this.isProcessingStopped()) {
                  syncObject.wait();
               }
            }
            
            while( !ReportingDBWriter.this.isProcessingStopped() ) {
               
               synchronized( syncObject ) {
                  while( !ReportingDBWriter.this.isProcessingStopped() ) {
                     nextTimestamp = StoredRecordManager.getDerivedTimeEnd("hour",timestampOfLastRowData);
                     SGELog.fine( "derive value timestamp is {0} (last {1})", nextTimestamp, lastCalcTimestamp);
                     if( nextTimestamp.getTime() > lastCalcTimestamp.getTime() ) {
                        break;
                     }
                     SGELog.finest("derived value thread is waiting for next event");
                     syncObject.wait();
                     SGELog.finest("derived value wakeup from event");
                  }
               }
               lastCalcTimestamp = nextTimestamp;
               
               if (calculationFile != null) {
                  connection = database.getConnection();
                  
                  long startTime = System.currentTimeMillis();
                  try {
                     calculateDerivedValues( connection, nextTimestamp.getTime() );
                     
                     long duration = (System.currentTimeMillis()- startTime) / 1000;
                     if (SGELog.isLoggable(Level.INFO)) {
                        long minutes = duration / 60;
                        SGELog.info("ReportingDBWriter.derivedDuration",
                              new Integer( (int)(minutes / 60)),
                              new Integer( (int)(minutes % 60)) );
                     }
                     try {
                        ParserEvent evt =
                              StatisticManager.createStatisticEvent(
                              this, ReportingSource.DBWRITER_STATISTIC,
                              System.currentTimeMillis(), "dbwriter",
                              "derived_value_time", duration);
                        
                        statisticManager.newLineParsed(evt, connection);
                        database.commit(connection, CommitEvent.INSERT);
                     } catch(ReportingException re) {
                        SGELog.warning(re, "ReportDBWriter.statisticDBError");
                        database.rollback(connection);
                     }
                     
                  } catch( ReportingException re ) {
                     // rollback has already been executed in calculateDerivedValues
                     re.log();
                  } finally {
                     database.release( connection );
                  }
                  
                  startTime = System.currentTimeMillis();
                  
                  try {
                     deleteData(nextTimestamp.getTime());
                     
                     long duration = (System.currentTimeMillis()- startTime) / 1000;
                     if (SGELog.isLoggable(Level.INFO)) {
                        long minutes = duration / 60;
                        SGELog.info("ReportingDBWriter.deleteDuration",
                              new Integer( (int)(minutes / 60)),
                              new Integer( (int)(minutes % 60)) );
                     }
                     try {
                        connection = database.getConnection();
                        ParserEvent evt =
                              StatisticManager.createStatisticEvent(
                              this, ReportingSource.DBWRITER_STATISTIC,
                              System.currentTimeMillis(), "dbwriter",
                              "deletion_time", duration);
                        
                        statisticManager.newLineParsed(evt, connection);
                        database.commit(connection, CommitEvent.INSERT);
                     } catch(ReportingException re) {
                        SGELog.warning(re, "ReportDBWriter.statisticDBError");
                        database.rollback(connection);
                     }
                  } catch( ReportingException re ) {
                     // rollback has already been executed in deleteData
                     re.log();
                  } finally {
                     database.release( connection );
                  }
               }
               
               java.util.Date nextCalculationTime = new java.util.Date(nextTimestamp.getTime() + 71 * 60 * 1000);
               if( nextCalculationTime.getTime() > System.currentTimeMillis() ) {
                  // start next calculation one hour later.
                  // wait additional 15 minutes to be sure that all values of the last hour are in the
                  // database - getDerivedValues uses a delay of 10 minutes
                  SGELog.info("ReportingDBWriter.nextTask", nextCalculationTime );
                  sleep( nextCalculationTime.getTime() - System.currentTimeMillis()  );
               }
            }
         } catch( InterruptedException ire ) {
            // finish execution
         } catch( Throwable ex ) {
            SGELog.severe( ex, "Unknown error: {0}", ex.toString() );
         } finally {
            SGELog.config("Derived Value Thread is finished");
            SGELog.exiting(getClass(),"run");
         }
      }
      
      public void newLineParsed(ParserEvent e, Connection connection) throws ReportingException {
         String timeFieldName = "time";
         
         Object obj  = e.data.get( timeFieldName );
         // We do not want to change the timeStamp if the ReportingSource is
         // DBWRITER_STATISTIC, because that prevents the TestDelete to run
         // successfuly. I.E. without this check the deleteData never gets
         // executed, because the timeStamp from TestDelete and
         // DBWRITER_STATISTIC collide.
         if(e.reportingSource != ReportingSource.DBWRITER_STATISTIC) {
            if( obj instanceof DateField ) {
               DateField dateField = (DateField)obj;
               synchronized( syncObject ) {
                  timestampOfLastRowData = dateField.getValue().getTime();
                  syncObject.notify();
               }
               SGELog.fine("new object received, timestampOfLastRowData is {0}",
                     dateField.getValue());
            } else if ( obj == null ) {
               ReportingException re = new ReportingException("DerivedValueThread.timeFieldNotFound",
                     timeFieldName );
               throw re;
            } else {
               ReportingException re = new ReportingException("DerivedValueThread.invalidTimeField",
                     timeFieldName );
               throw re;
            }
         }
      }
   }
   
   public void deleteData(long timestampOfLastRowData) throws ReportingException {
      DbWriterConfig conf  = getDbWriterConfig();
      
      if (conf != null) {
         DeletionRuleType rule = null;
         RecordManager manager = null;
         boolean execute = false;
         
         Iterator iter = conf.getDelete().iterator();
         Timestamp ts = null;
         int ruleNumber = 0;
         while (iter.hasNext() && !isProcessingStopped() ) {
            ruleNumber++;
            
            rule = (DeletionRuleType)iter.next();
            manager = getDeleteManager(rule.getScope());
            if (manager == null) {
               ReportingException re = new ReportingException("ReportingDBWriter.deleteManagerNotFound",
                     new Object[] {rule.getScope()});
               throw re;
            }
            //check the validity of delete rules make sure they contain all the properties
            execute = validateDeleteRules(rule, ruleNumber);
            //validation was succesful
            if (execute){
               int dbType = Database.getType();
               
               Timestamp timeEnd = manager.getDeleteTimeEnd(timestampOfLastRowData, rule.getTimeRange(),
                     rule.getTimeAmount());
               // first get the Select Statements that will be the base for the delete. Each delete rule can in fact
               // produce statements for multiple tables
               String [] deleteRule = ((DeleteManager)manager).getDeleteRuleSQL(timeEnd, rule.getSubScope());
               try {
                  if(dbType == Database.TYPE_MYSQL) {
                     processMySQLDeletes(deleteRule, manager);
                  } else {
                     processDeletes(deleteRule, manager);
                  }
               } catch (ReportingException ex) {
                  throw ex;
               }
               
            }
         }
      }
   }
   
   // Validates if the delete rule contains all the parameters
   private boolean validateDeleteRules(DeletionRuleType rule, int ruleNumber) throws ReportingException {
      boolean executeIt = true;
      if (!rule.isSetScope()) {
         SGELog.warning("ReportingDBWriter.missingAttributeInDeletionRule", new Integer(ruleNumber), "scope");
         executeIt = false;
      }
      if (!rule.isSetTimeRange()) {
         SGELog.warning("ReportingDBWriter.missingAttributeInDeletionRule", new Integer(ruleNumber), "time_range");
         executeIt = false;
      }
      if (!rule.isSetTimeAmount()) {
         SGELog.warning("ReportingDBWriter.missingAttributeInDeletionRule", new Integer(ruleNumber), "time_amount");
         executeIt = false;
      }
      
      return executeIt;
   }
   
   /**
    * Since some of the DeleteManager(s) (JobManager) can contain nested
    * deleted rules, i.e. the delete rules for their child tables, we have to
    * itterate through the whole deleteRules[] before checking the updateCount.
    * The updateCount returned from the last executed deleteRule in the array
    * will always be the be the updateCount from the parent table.
    */
   private void processDeletes(String [] deleteRules, RecordManager manager)
   throws ReportingException {
      java.sql.Connection conn = null;
      int updateCount = -1;
      
      try {
         conn = database.getConnection();
         while (updateCount != 0) {
            for (int i = 0; i < deleteRules.length; i++) {
               updateCount = database.executeUpdate(deleteRules[i], conn);
               database.commit(conn, CommitEvent.DELETE);
            }
         }
      } catch (ReportingException ex) {
         database.rollback(conn);
         throw ex;
      } finally {
         database.release(conn);
      }
   }
   
   /**
    * see DeleteManager interface for info why we have to use different algorithm
    * for deleting from MySQL Database
    * WARNING: We cannot use this algorithm for Oracle and Postgres because
    * their drivers don't implement the getTableName(int column) method.
    *
    * There is a way around it in Postgres:
    *
    *     result = stmt.getResultSet();
    *     ResultSetMetaData data = result.getMetaData();
    *     String table = ((PGResultSetMetaData)data).getBaseTableName(1));
    *
    * TODO: If we find a way in Oracle to get table name we should consolidate
    *       this so we use only one algorithm to delete dat for all database
    */
   private void processMySQLDeletes(String [] deleteRules, RecordManager manager)
   throws ReportingException {
      java.sql.Connection conn = null;
      Statement stmt = null;
      int updateCount = -1;
      ResultSet result = null;
      
      try {
         try {
            conn = database.getConnection();
            while (updateCount != 0) {
               for (int i = 0; i < deleteRules.length; i++) {
                  stmt = database.executeQuery(deleteRules[i], conn);
                  result = stmt.getResultSet();
                  
                  ResultSetMetaData meta = result.getMetaData();
                  
                  StringBuffer bf = getDeleteStatement(meta.getTableName(1), meta.getColumnName(1));
                  while(result.next()) {
                     bf.append(result.getInt(1));
                     bf.append(",");
                  }
                  //there is no id 0 but we have "," after the last id so we have to
                  //append some number, also we need to execute the delete even
                  //if there was nothing in resultSet so we would get updateCount = 0
                  bf.append("0)");
                  updateCount = database.executeUpdate(bf.toString(), conn);
                  database.commit(conn, CommitEvent.DELETE);
               }
            }
         } finally {
            if (stmt != null) {
               stmt.close();
            }
            database.release(conn);
         }
      } catch (ReportingException ex) {
         database.rollback(conn);
         ex.printStackTrace();
         throw ex;
      } catch (SQLException sql) {
         //happens if there is a problem retrieving MetaData
         sql.printStackTrace();
         ReportingException re = new ReportingException("ReportingDBWriter.sqlError");
         re.initCause(sql);
         throw re;
      }
      
   }
   
   private StringBuffer getDeleteStatement(String table, String field) {
      
      StringBuffer bf = new StringBuffer("DELETE FROM ");
      bf.append(table);
      bf.append(" WHERE ");
      bf.append(field);
      bf.append(" IN (");
      return bf;
   }
   
   private FileParser currentReader;
   
   private void processFile( FileParser reader, Database database )
   throws ReportingException {
      currentReader = reader;
      try {
         currentReader.processFile( database );
      } finally {
         currentReader = null;
      }
   }
   
   public void mainLoop() throws ReportingException {
      
      boolean done = false;
      
      java.util.Date nextCalculationTime = new java.util.Date();
      long currentTime = 0L;
      long nextMillis = 0L;
      do {
         currentTime = System.currentTimeMillis();
         // calculate time of next loop
         nextMillis = System.currentTimeMillis() + interval * 1000;
         
         if( isProcessingStopped() ) {
            break;
         }
         
         // check database and reopen if necessary
         if( !database.test() ) {
            if (continous) {
               try {
                  sleep(5000);
                  continue;
               } catch( InterruptedException ire ) {
                  break;
               }
            }
         }
         
         for( int i = 0; i < readers.length && !isProcessingStopped(); i++ ) {
            if( readers[i] != null ) {
               processFile( readers[i], database );
            }
         }
         
         
         if( isProcessingStopped() ) {
            break;
         }
         
         if (continous) {
            long currentMillis = System.currentTimeMillis();
            if (nextMillis > currentMillis ) {
               long sleepMillis = nextMillis - currentMillis;
               SGELog.config( "ReportingDBWriter.sleep", new Long(sleepMillis) );
               try {
                  sleep(sleepMillis);
               } catch( InterruptedException ire ) {
                  break;
               }
            } else {
               SGELog.warning("ReportingDBWriter.intervalExpired");
            }
            
            if( isProcessingStopped() ) {
               break;
            }
            
         } else {
            done = true;
         }
         
      } while ( !done );
   }
   
   
   private int getPid() throws ReportingException {
      
      if( pid < 0 ) {
         try {
            System.loadLibrary("juti");
            pid = new com.sun.grid.util.SGEUtil().getPID();
         } catch(SecurityException se) {
            ReportingException re = new ReportingException("ReportingDBWriter.getpid.securityError",
                  new Object[] { se.getMessage() } );
            re.initCause(se);
            throw re;
         } catch(UnsatisfiedLinkError ule) {
            ReportingException re = new ReportingException("ReportingDBWriter.getpid.linkError",
                  new Object[] { ule.getMessage() });
            re.initCause(ule);
            throw re;
         }
      }
      return pid;
   }
   
   private void writePidFile() throws ReportingException {
      
      if( pidFile == null ) {
         throw new ReportingException("ReportingDBWriter.noPidFile" );
      }
      if( pidFile.exists() ) {
         throw new ReportingException("ReportingDBWriter.pidFileExists", pidFile);
      }
      int pid = getPid();
      try {
         FileWriter wr = new FileWriter(pidFile);
         try {
            PrintWriter pw = new PrintWriter(wr);
            pw.println(pid);
            pw.flush();
         } finally {
            wr.close();
         }
         pidFileWritten = true;
         if( SGELog.isLoggable(Level.FINE)) {
            SGELog.fine( "ReportingDBWriter.pidFileWritten", new Integer(pid), pidFile );
         }
      } catch( IOException ioe ) {
         ReportingException re = new ReportingException("ReportingDBWriter.pidFileIOError", pidFile, ioe.getMessage());
         re.initCause(ioe);
         pidFile.delete();
         throw re;
      }
   }
   
   /**
    *  This method is used, to write log message directly into
    *  the log file if the logging is not available (e.g. at shutdown)
    *
    *  @param lr  the logging record which is used
    */
   private void writeDirectLog(java.util.logging.LogRecord lr) {
      String msg = handler.getFormatter().format(lr);
      closeLogging();
      if( logFile != null ) {
         try {
            synchronized(logFile) {
               FileWriter fw = new FileWriter(this.logFile, true);
               PrintWriter pw = new PrintWriter(fw);
               pw.println(msg);
               pw.flush();
               pw.close();
            }
         } catch (IOException ioe) {
            ioe.printStackTrace();
         }
      } else {
         System.err.println(msg);
      }
   }
   
   /**
    *  the run method calls the mainLoop and catches all uncaught exceptions
    *  @see #mainLoop
    */
   public void run() {
      SGELog.entering(getClass(), "run");
      isProcessingStopped = false;
      try {
         long time = System.currentTimeMillis();
         
         writePidFile();
         
         initialize();
         
         if( SGELog.isLoggable(Level.FINE)) {
            double diff = (System.currentTimeMillis() - time) / 1000;
            SGELog.fine("ReportingDBWriter.initialized", new Double(diff));
         }
         
         // After all is initialized we can startup the threads
         derivedValueThread.start();
         vacuumAnalyzeThread.start();
         statisticThread.start();
         
         mainLoop();
      } catch( ReportingException re ) {
         
         java.util.logging.LogRecord lr =
               new java.util.logging.LogRecord(Level.SEVERE,re.getMessage());
         lr.setParameters(re.getParams());
         lr.setSourceClassName(getClass().getName());
         lr.setSourceMethodName("run");
         lr.setThrown(re);
         lr.setMillis(System.currentTimeMillis());
         lr.setResourceBundle(resourceBundle);
         writeDirectLog(lr);
      }  catch( Throwable ex ) {
         if( SGELog.isLoggable(Level.SEVERE)) {
            // we can not use the logger to write the log message
            // because the log thread has already been stopped
            java.util.logging.LogRecord lr =
                  new java.util.logging.LogRecord(Level.SEVERE,"Uncaught exception: {0}");
            lr.setThrown(ex);
            lr.setParameters(new Object[] { ex.getMessage() } );
            lr.setSourceClassName(getClass().getName());
            lr.setSourceMethodName("run");
            lr.setMillis(System.currentTimeMillis());
            lr.setResourceBundle(resourceBundle);
            writeDirectLog(lr);
         }
      } finally {
         if( SGELog.isLoggable(Level.INFO ) ) {
            // we can not use the logger to write the log message
            // because the log thread has already been stopped
            java.util.logging.LogRecord lr =
                  new java.util.logging.LogRecord(Level.INFO,"dbwriter stopped");
            lr.setSourceClassName(getClass().getName());
            lr.setSourceMethodName("run");
            lr.setMillis(System.currentTimeMillis());
            lr.setResourceBundle(resourceBundle);
            writeDirectLog(lr);
         }
         isProcessingStopped = true;
         derivedValueThread.interrupt();
         vacuumAnalyzeThread.interrupt();
         statisticThread.interrupt();
         SGELog.exiting(getClass(), "run");
      }
   }
   
   private boolean isProcessingStopped;
   
   public void stopProcessing() {
      isProcessingStopped = true;
      super.interrupt();
      derivedValueThread.interrupt();
      vacuumAnalyzeThread.interrupt();
      statisticThread.interrupt();
      if( database != null ) {
         database.closeAll();
      }
      
      if( readers != null ) {
         for( int i = 0; i < readers.length; i++ ) {
            if( readers[i] != null ) {
               readers[i].stop();
            }
         }
      }
      if( pidFile != null && pidFileWritten ) {
         if( !pidFile.delete() ) {
            java.util.logging.LogRecord lr =
                  new java.util.logging.LogRecord(Level.SEVERE,"ReportingDBWriter.pidFileDeleteError");
            lr.setParameters(new Object[] { pidFile } );
            lr.setSourceClassName(getClass().getName());
            lr.setSourceMethodName("stopProcessing");
            lr.setMillis(System.currentTimeMillis());
            lr.setResourceBundle(resourceBundle);
            writeDirectLog(lr);
         }
      }
      
      try {
         join(JOIN_TIMEOUT);
      } catch( InterruptedException ire ) {
      }
   }
   
   public boolean isProcessingStopped() {
      return isProcessingStopped;
   }
   
   public void start() {
      super.start();
   }
   
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      boolean ret;
      
      ReportingDBWriter writer = new ReportingDBWriter();
      try {
         
         writer.initialize(args);
         writer.start();
         writer.join();
      } catch( ReportingException re ) {
         re.log();
      } catch( InterruptedException ire ) {
         // ignore
      }
   }
   
   // ----------------- Set methods --------------------------------------------
   
   public void setCalculationFile( String calculationFile ) {
      this.calculationFile = calculationFile;
      this.config = null;
   }
   
   public void setReportingFile( String reportingFile ) {
      this.reportingFile = reportingFile;
   }
   
   public void setJDBCDriver( String jdbcDriver ) {
      this.driver = jdbcDriver;
   }
   
   public void setJDBCUrl( String jdbcUrl ) {
      this.url = jdbcUrl;
   }
   
   public void setJDBCUser( String jdbcUser ) {
      this.userName = jdbcUser;
   }
   
   public void setJDBCPassword( String jdbcPassword ) {
      this.userPW = jdbcPassword;
   }
   
   /**
    *  Set the waiting time between to import cylces
    *  @param  interval   waiting time in seconds
    */
   public void setInterval( int interval ) {
      this.interval = interval;
   }
   
   public void setContinous( boolean continous ) {
      this.continous = continous;
   }
   
   public boolean isContinous() {
      return continous;
   }
   
   public void setPidFile(File pidFile) {
      this.pidFile = pidFile;
   }
   public File getPidFile() {
      return pidFile;
   }
   
   Database getDatabase() {
      return database;
   }
   
   // --------------------------------------------------------------------------------------------------
   
   /**
    *  Read options from stdin
    */
   private void getOptionFromStdin() {
      String name = null;
      String value = null;
      HashMap options = new HashMap();
      try {
         BufferedReader in = new BufferedReader( new InputStreamReader(System.in) );
         
         String line = null;
         int    index = 0;
         
         while( (line = in.readLine()) != null ) {
            index = line.indexOf( '=' );
            if( index > 0 ) {
               name = line.substring( 0, index ).trim();
               value = line.substring( index+1, line.length() ).trim();
               
               if( value.length() > 0 ) {
                  options.put( name, value );
               }
               
            }
         }
         
      } catch( IOException ioe ) {
         SGELog.warning( ioe, "ReportingDBWriter.stdinIOError", ioe.getMessage() );
      }
      
      // First set the debug option
      value = (String)options.remove( ENV_DEBUG );
      if( value != null ) {
         debugLevel = value;
      }
      
      Iterator iter = options.keySet().iterator();
      
      while( iter.hasNext() ) {
         name = (String)iter.next();
         value = (String)options.get( name );
         if ( ENV_ACCOUNTING_FILE.equals( name ) ) {
            accountingFile = value;
         } else if ( ENV_CALC_FILE.equals( name ) ) {
            calculationFile = value;
         } else if ( ENV_CONTINOUS.equals( name ) ) {
            continous = Boolean.valueOf( value ).booleanValue();
         } else if ( ENV_DRIVER.equals( name ) ) {
            driver = value;
         } else if ( ENV_INTERVAL.equals( name ) ) {
            try {
               interval = Integer.parseInt( value );
            } catch( NumberFormatException nfe ) {
               SGELog.warning( "ReportingDBWriter.numericalOptionExpected", ENV_INTERVAL, value );
            }
         } else if (ENV_USER.equals( name ) ) {
            userName = value;
         } else if (ENV_USER_PW.equals( name ) ) {
            userPW = value;
         } else if (ENV_REPORTING_FILE.equals( name ) ) {
            reportingFile = value;
         } else if( ENV_SHARE_LOG_FILE.equals( name ) ) {
            sharelogFile = value;
         } else if (ENV_STATISTIC_FILE.equals( name ) ) {
            statisticsFile = value;
         } else if (ENV_URL.equals( name ) ) {
            url = value;
         } else if (ENV_SQL_THRESHOLD.equals(name)) {
            try {
               sqlExecThreshold = Integer.parseInt(value) * 1000;
            } catch( NumberFormatException nfe ) {
               SGELog.warning( "ReportingDBWriter.numericalOptionExpected", ENV_SQL_THRESHOLD, value );
            }
         } else {
            SGELog.warning( "ReportDBWriter.unknownOption", name );
         }
      }
   }
   
   /**
    *  Set the schedule interval for the vacuum analyze
    *  @param vacuumSchedule "off" if no vaccum analyze should be executed
    *                        else a valid interval specifcation for a <code>TimeSchedule</code>
    *  @throws IllegalArgumentException of the parameter vacuumSchedule has invalid values
    *  @see    TimeSchedule
    */
   public void setVacuumSchedule(String vacuumSchedule) {
      if("off".equalsIgnoreCase(vacuumSchedule)) {
         this.vacuum = false;
      } else {
         this.vacuum = true;
         this.vacuumSchedule = new TimeSchedule(vacuumSchedule);
      }
   }
}
