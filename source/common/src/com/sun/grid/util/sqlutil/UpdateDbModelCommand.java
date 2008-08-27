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
package com.sun.grid.util.sqlutil;

import com.sun.grid.logging.SGELog;
import com.sun.grid.util.SQLUtil;
import com.sun.grid.util.dbmodel.DBModel;
import com.sun.grid.util.dbmodel.ObjectFactory;
import com.sun.grid.util.dbmodel.SQLItem;
import com.sun.grid.util.dbmodel.Version;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * This command updates the dbmodel according to
 * its database definition.
 */
public class UpdateDbModelCommand extends Command {

   /** name of the dbmodel package. */
   public static final String DBMODEL_PACKAGE =
         "com.sun.grid.util.dbmodel";
   /**  sql query for the highest version is the
    *   version table. */
   public static final String SQL_VERSION_STMT =
         "select v_id, v_version, v_time " + "from sge_version " + "where v_id = (select max(v_id) from sge_version)";
   /**  sql query for the availability test of the table
    *   sge_version. */
   public static final String SQL_TEST_VERSION_STMT =
         "select count(*) from sge_version";
   /** the jaxb context for the dbmodel classes. */
   private JAXBContext jc;
   /** the factory for instances of the dbmodel classes. */
   private ObjectFactory objFactory;

   /** Creates a new instance of UpdateDbModelCommand.
    *  @param  aSqlUtil  the SQLUtil which instantiates this command
    *  @param  name      the name of the command
    */
   public UpdateDbModelCommand(final SQLUtil aSqlUtil, final String name) {
      super(aSqlUtil, name);

      try {
         jc = JAXBContext.newInstance("com.sun.grid.util.dbmodel");
         objFactory = new ObjectFactory();
      } catch (JAXBException jaxbe) {
         IllegalStateException ilse =
               new IllegalStateException("Can't create JAXBContext");
         ilse.initCause(jaxbe);
         throw ilse;
      }

   }

   /**
    * the the definition of the dbmodel from a file.
    * @param file the file
    * @throws IOException    on any I/O Error
    * @throws JAXBException  if a dbmodel class could not be
    *                        instantiated
    * @return  the dbmodel
    */
   private DBModel readModel(final File file)
         throws IOException, JAXBException {
      Unmarshaller un = jc.createUnmarshaller();
      DBModel ret = (DBModel) un.unmarshal(file);
      return ret;
   }

   /**
    * test wether sge_version table is available.
    * @throws SQLException  if the sql statement could not
    *                       be created
    * @return  true if the tabe is available
    */
   private boolean isVersionTableAvailable() throws SQLException {
      SGELog.entering(getClass(), "isVersionTableAvailable");
      Connection conn = getConnection();

      Statement stmt = conn.createStatement();
      try {
         SGELog.fine("excute {0}", SQL_TEST_VERSION_STMT);
         ResultSet rs = stmt.executeQuery(SQL_TEST_VERSION_STMT);
         rs.close();

         SGELog.exiting(getClass(), "isVersionTableAvailable", Boolean.TRUE);
         return true;
      } catch (SQLException sqle) {
         SGELog.exiting(getClass(), "isVersionTableAvailable", Boolean.FALSE);
         return false;
      } finally {
         stmt.close();
      }
   }
   /** index of the table name column is the meta data. */
   public static final int NAME_COLUMN = 3;

   /**
    * get a list of all tables in the database which starts
    * with "sge_".
    * @param  schema  the name of the database schema
    * @throws SQLException on any SQL error (e.g. broken connection)
    * @return  list of tables
    */
   private List getSGETables(final String schema) throws SQLException {
      SGELog.entering(getClass(), "getSGETables");
      Connection conn = getConnection();

      DatabaseMetaData dbMeta = conn.getMetaData();

      String[] tablesTypes = new String[]{
         "TABLE"
      };

      SGELog.fine("Searching sge tables in schema {0}", schema);

      ResultSet rs = dbMeta.getTables(null, schema, null, tablesTypes);

      try {
         List ret = new ArrayList();
         String str = null;
         while (rs.next()) {
            str = rs.getString(NAME_COLUMN);
            if (str.toLowerCase().startsWith("sge_")) {
               SGELog.fine("found table {0}", str);
               ret.add(str);
            }
         }
         SGELog.exiting(getClass(), "getSGETables", ret);
         return ret;
      } finally {
         rs.close();
      }
   }

   /**
    *   Get the highest version of the database.
    *   @return  the version object
    *   @throws SQLException on any sql error
    */
   private Version getDatabaseVersion() throws SQLException {

      Connection conn = getConnection();
      Statement stmt = conn.createStatement();
      try {
         ResultSet rs = stmt.executeQuery(SQL_VERSION_STMT);
         try {
            Version ret = objFactory.createVersion();
            if (rs.next()) {
               ret.setId(rs.getInt("v_id"));
               ret.setName(rs.getString("v_version"));
            } else {
               ret.setId(0);
               ret.setName("unknown");
            }
            return ret;
         } catch (JAXBException jaxbe) {
            throw new IllegalStateException("Can not create " + "instance of version");
         } finally {
            rs.close();
         }
      } finally {
         stmt.close();
      }
   }

   /**
    *   Get the version of the database model in the database.
    *
    *   @param   schema  name of hte database schema
    *   @return  the version object which describes the current
    *            version of the database. The id of this object
    *            is
    *               -1   if no sge tables was found in the database
    *               0    if the sge_version table was not found or the
    *                    sge_version table is empty
    *               else the highest version id in the table sge_version
    *   @throws SQLException on any sql error
    */
   Version getExistingVersion(final String schema) throws SQLException {
      Version ret = null;
      if (isVersionTableAvailable()) {
         ret = this.getDatabaseVersion();
      } else {
         List tables = this.getSGETables(schema);
         if (SGELog.isLoggable(Level.FINE)) {
            Iterator iter = tables.iterator();
            while (iter.hasNext()) {
               SGELog.fine("Found table {0}", iter.next());
            }
         }
         try {
            if (tables.isEmpty()) {
               SGELog.fine("Found no sge tables version id is -1");
               ret = objFactory.createVersion();
               ret.setId(-1);
               ret.setName("No version installed");
            } else {
               SGELog.fine("Found the sge tables, version id is 0");
               ret = objFactory.createVersion();
               ret.setId(0);
               ret.setName("Initial version");
            }
         } catch (JAXBException jaxbe) {
            throw new IllegalStateException("Can not create " + "instanceof version");
         }
      }
      return ret;
   }

   /**
    *  execute this command.
    *  @param args   the arguments for this command
    *  @return  0  command successfully executed
    *           1  an error occured
    */
   public final int run(final String args) {
      if (args == null) {
         SGELog.severe("no arguments found");
         return 1;
      }
      if (getConnection() == null) {
         SGELog.severe("Can not execute this command, not connected");
         return 1;
      }

      StringTokenizer st = new StringTokenizer(args, " ");

      String[] argv = new String[st.countTokens()];
      int i = 0;
      while (st.hasMoreTokens()) {
         argv[i++] = st.nextToken();
      }

      if (argv[0].equals("print_db_version")) {

         String schema = null;
         boolean onlyId = false;
         switch (argv.length) {
            case 2:
               schema = argv[1];
               break;
            case 3:
               if (argv[1].equals("-only-id")) {
                  onlyId = true;
               } else {
                  SGELog.severe("Unknown option {0}", argv[1]);
                  return 1;
               }
               schema = argv[2];
               break;
            default:
               SGELog.severe("Invalid argument count");
               SGELog.info(usage());
               return 1;
         }
         try {
            Version dbVersion = getExistingVersion(schema);
            if (onlyId) {
               SGELog.info(Integer.toString(dbVersion.getId()));
            } else {
               SGELog.info("version {0} (id={1})",
                     dbVersion.getName(),
                     new Integer(dbVersion.getId()));
            }
            return 0;
         } catch (SQLException sqle) {
            SGELog.severe(sqle, "sql error: {0}", sqle.getMessage());
            return 1;
         }
      } else {
         return install(argv);
      }
   }

   /**
    * install a new version of the database.
    *
    * @param argv must be a string array(3)
    *             argv[0] contains the version id
    *             argv[1] contains the file with the db defintion
    *             argv[2] contains the schema name
    *
    * @return  0  the database model has been succesfully updated to
    *             the version
    *          1  an error occured
    */
   private int install(final String[] argv) {

      boolean tryrun = false;
      int argIndex = 0;
      int optionCount = argv.length - 3;
      Connection conn = getConnection();
      Connection conn2 = getConnection2();
      Statement stmt = null;
      Statement stmt2 = null;

      while (argIndex < optionCount) {
         if (argv[argIndex].equals("-dry-run")) {
            argIndex++;
            tryrun = true;
            SGELog.warning("Run in dry mode, no action on the database will be executed");
         } else {
            SGELog.severe("Unknown option " + argv[argIndex]);
            SGELog.info(usage());
            return 1;
         }
      }

      if (argIndex > argv.length + 3) {
         SGELog.severe("Invalid number of arguments");
         SGELog.info(usage());
         return 1;
      }

      Integer versionId = null;
      try {
         versionId = new Integer(argv[argIndex]);
         argIndex++;
      } catch (NumberFormatException nfe) {
         SGELog.severe("version must be an integer (''{0}'')", argv[argIndex]);
         SGELog.info(usage());
         return 1;
      }
      File file = new File(argv[argIndex++]);
      if (!file.exists()) {
         SGELog.severe("database model file {0} not found", file);
         SGELog.info(usage());
         return 1;
      }
      SGELog.fine("file is {0}", file);

      String schema = argv[argIndex++];
      try {
         DBModel model = readModel(file);
         Version dbVersion = getExistingVersion(schema);

         if (dbVersion.getId() >= versionId.intValue()) {
            SGELog.info("version with id {0} is already installed", versionId);
            return 0;
         }

         if (!tryrun) {
            try {
               SGELog.finest("switch off auto commit mode");
               conn.setAutoCommit(false);
               /* The secondary connection is used to create synonyms for Oracle db.
                * This connection is initiated before calling install function.
                * If the connection is set, we set the autocommit to false.
                */
               if (conn2 != null) {
                  conn2.setAutoCommit(false);
               }
            } catch (SQLException sqle) {
               SGELog.severe(sqle, "Can not switch off the auto commit mode");
               return 1;
            }
         }

         List versionList = model.getVersion();
         Version tmpVersion = null;
         Version instVersion = null;
         Iterator iter = null;
         SQLItem item = null;
         stmt = conn.createStatement();
         /* The secondary connection is used to create synonyms for Oracle db.
          * This connection is initiated before calling install function.
          * If the connection is set, we prepare the statement stmt2.
          */
         if (conn2 != null) {
            stmt2 = conn2.createStatement();
         }

         for (int i = dbVersion.getId() + 1;
               i <= versionId.intValue(); i++) {

            iter = versionList.iterator();
            instVersion = null;
            while (iter.hasNext()) {
               tmpVersion = (Version) iter.next();
               if (tmpVersion.getId() == i) {
                  instVersion = tmpVersion;
                  break;
               }
            }
            if (instVersion == null) {
               SGELog.severe("Version with id {0} is not defined in {1}",
                     new Integer(i), file);
               return 1;
            }
            SGELog.info("Install version {0} (id={1}) -------",
                  instVersion.getName(),
                  new Integer(instVersion.getId()));

            iter = instVersion.getItem().iterator();

            while (iter.hasNext()) {
               item = (SQLItem) iter.next();
               String descr = getSQLUtil().replaceVariables(item.getDescription());
               String sql = getSQLUtil().replaceVariables(item.getSql());

               if (tryrun) {
                  SGELog.info(sql.trim() + ";");
               } else {
                  SGELog.info(descr);
                  SGELog.fine("execute {0}", sql);
                  /* Check which statement use for executing sql command.
                   * For the synonyms we use the secondary connection.
                   */
                  if (!item.isSetSynonym() || !item.isSynonym()) {
                     stmt.execute(sql);
                  } else {
                     stmt2.execute(sql);
                  }
               }
            }

            if (tryrun) {
               SGELog.info("COMMIT;");
            } else {
               SGELog.info("commiting changes");
               conn.commit();
               if (conn2 != null) {
                  conn2.commit();
               }
               SGELog.info("Version {0} (id={1}) successfully installed",
                     instVersion.getName(),
                     new Integer(instVersion.getId()));
            }
         }
         return 0;
      } catch (IOException ioe) {
         SGELog.severe(ioe, "I/O Error while reading file {0}: {1}",
               file, ioe.getMessage());
         return 1;
      } catch (JAXBException jaxbe) {
         SGELog.severe(jaxbe, "Can not unmarshal file {0}: {1}",
               file, jaxbe.toString());
         return 1;
      } catch (SQLException sqle) {
         SGELog.severe(sqle, "SQL error: {0}", sqle.getMessage());
         try {
            conn.rollback();
            if (conn2 != null) {
               conn2.rollback();
            }
         } catch (SQLException sqle1) {
            SGELog.severe(sqle1, "Can not rollback: {0}", sqle1.getMessage());
         }
         return 1;
      } finally {
         try {
            stmt.close();
            if (stmt2 != null) {
               stmt2.close();
            }
            SGELog.finest("switch on auto commit mode");
            conn.setAutoCommit(true);
            if (conn2 != null) {
               conn2.setAutoCommit(true);
            }
         } catch (SQLException sqle) {
            SGELog.warning(sqle,"Error in SQL Statement");
            return 1;
         }
      }

   }

   /**
    * get the usage message for this command.
    * @return the usage message
    */
   public final String usage() {
      return getName() + "(print_db_version -only-id |" + "<version> [-dry-run ]<dbmodel file>) <schema>";
   }
}
