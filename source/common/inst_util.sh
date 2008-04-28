#!/bin/sh
#
#___INFO__MARK_BEGIN__
##########################################################################
#
#  The Contents of this file are made available subject to the terms of
#  the Sun Industry Standards Source License Version 1.2
#
#  Sun Microsystems Inc., March, 2001
#
#
#  Sun Industry Standards Source License Version 1.2
#  =================================================
#  The contents of this file are subject to the Sun Industry Standards
#  Source License Version 1.2 (the "License"); You may not use this file
#  except in compliance with the License. You may obtain a copy of the
#  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
#
#  Software provided under this License is provided on an "AS IS" basis,
#  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
#  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
#  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
#  See the License for the specific provisions governing your rights and
#  obligations concerning the Software.
#
#  The Initial Developer of the Original Code is: Sun Microsystems, Inc.
#
#  Copyright: 2001 by Sun Microsystems, Inc.
#
#  All Rights Reserved.
#
########################################setupdb##################################
#___INFO__MARK_END__


DB_VERSION=7
# -------------------------------------------------------------------
# queryJavaHome
#  $1  contains the minimumn java version
# -------------------------------------------------------------------
queryJavaHome()
{
   $INFOTEXT -u "\nJava setup"

   MIN_JAVA_VERSION=$1
   NUM_MIN_JAVA_VERSION=`versionString2Num $MIN_JAVA_VERSION`

   $INFOTEXT "\nARCo needs at least java $MIN_JAVA_VERSION\n"
   while :
   do
      dummy=$JAVA_HOME
      $INFOTEXT -n "Enter the path to your java installation [$dummy] >> "
      dummy=`Enter $dummy`
      if [ "$dummy" = "" ]; then
         $INFOTEXT "Java must be set!"
         continue
      fi
      if [ -x "$dummy/bin/java" ]; then
         JAVA_VERSION=`$dummy/bin/java -version 2>&1 | head -1`
         JAVA_VERSION=`echo $JAVA_VERSION | awk '{print $3}' | sed -e "s/\"//g"`
         NUM_JAVA_VERSION=`versionString2Num $JAVA_VERSION`
         
         if [ $NUM_JAVA_VERSION -lt $NUM_MIN_JAVA_VERSION ]; then
            $INFOTEXT "Invalid java version ($JAVA_VERSION), ARCo needs $MIN_JAVA_VERSION or higher"
         else
            JAVA_HOME=$dummy
            break
         fi
      else
         $INFOTEXT "Can not execute $dummy/bin/java"
      fi   
   done
}

########################################################
#
# Version to convert a version string in X.Y.Z-* or
# X.Y.X_NN format to XYZNN format so can be treated as a
# number.
#
# $1 = version string
# Returns numerical version
#
########################################################
versionString2Num () {

    # Minor and micro default to 0 if not specified.
    major=`echo $1 | awk -F. '{print $1}'`
    minor=`echo $1 | awk -F. '{print $2}'`
    if [ ! -n "$minor" ]; then
        minor="0"
    fi
    micro=`echo $1 | awk -F. '{print $3}'`
    if [ ! -n "$micro" ]; then
        micro="0"
    fi

    # The micro version may further be extended to include a patch number.
    # This is typically of the form <micro>_NN, where NN is the 2-digit
    # patch number.  However it can also be of the form <micro>-XX, where
    # XX is some arbitrary non-digit sequence (eg., "rc").  This latter
    # form is typically used for internal-only release candidates or
    # development builds.
    #
    # For these internal builds, we drop the -XX and assume a patch number 
    # of "00".  Otherwise, we extract that patch number.
    #
    patch="00"
    dash=`echo $micro | grep "-"`
    if [ $? -eq 0 ]; then
   # Must be internal build, so drop the trailing variant.
   micro=`echo $micro | awk -F- '{print $1}'`
    fi

    underscore=`echo $micro | grep "_"`
    if [ $? -eq 0 ]; then
   # Extract the seperate micro and patch numbers, ignoring anything
   # after the 2-digit patch.
   patch=`echo $micro | awk -F_ '{print substr($2, 1, 2)}'`
   micro=`echo $micro | awk -F_ '{print $1}'`
    fi

    echo "${major}${minor}${micro}${patch}"

} # versionString2Num

########################################################
#
# queryUserPWD
# ask for the password and store it in TMP_DB_PW variable.
#
########################################################
queryUserPWD()
{
   STTY_ORGMODE=`stty -g`
   while :
   do
      $INFOTEXT -n "\nEnter the password of the database user >> "
      stty -echo
      read TMP_DB_PW
      stty "$STTY_ORGMODE"
      $INFOTEXT -n "\n"
      $INFOTEXT -n "Retype the password >> "
      stty -echo
      read TMP_DB_PW1
      stty "$STTY_ORGMODE"
      $INFOTEXT -n "\n"
      if [ "$TMP_DB_PW" = "$TMP_DB_PW1" ]; then
         break;
      else
        $INFOTEXT "password do not match"
      fi
   done
}

#
#  Parameters
#     $1   dbwriter directory
#     $2   dbwriter configuration file, if exists
setupDB()
{
   DBWRITER_PWD="$1"
   DBWRITER_CONF="$2"

   do_setup=1
   if [ -r "$DBWRITER_CONF" ]; then
      do_setup=0
      # source the dbwriter configuration
      . $DBWRITER_CONF
      # from previous version we might not have all required parameters, set the default values
      case "$DBWRITER_DRIVER" in
      "org.postgresql.Driver")
         queryPostgres "only_defaults";;
      "oracle.jdbc.driver.OracleDriver")
         queryOracle "only_defaults";;
      "com.mysql.jdbc.Driver")
         queryMysql "only_defaults";;
      *)
         do_setup=1
         $INFOTEXT "Unkown database with driver $DB_DRIVER";
         $INFOTEXT -wait -n "Hit <RETURN> to continue >> ";;
      esac
      if [ $do_setup -ne 1 ]; then
         # source the dbwriter configuration again to update the default values
         . $DBWRITER_CONF
         DB_PW=$DBWRITER_USER_PW
         DB_USER=$DBWRITER_USER
         DB_URL=$DBWRITER_URL
         DB_DRIVER=$DBWRITER_DRIVER
      fi
   fi

   if [ $do_setup -eq 1 ]; then
      $CLEAR
      $INFOTEXT -u "\nSetup your database connection parameters"
   fi

   while :
   do
      if [ $do_setup -eq 1 ]; then
         DB_USER=arco_write      # default database username
         dummy=""
         $INFOTEXT -n \
                  "\nEnter your database type ( o = Oracle, p = PostgreSQL, m = MySQL ) [$dummy] >> "
         dbtype=`Enter $dummy`
         if [ "$dbtype" = 'p' ]; then
             queryPostgres
         elif [ "$dbtype" = 'o' ]; then
             queryOracle
         elif [ "$dbtype" = 'm' ]; then
             queryMysql
         else
            $INFOTEXT "\nDatabase type must be specified!";
            continue
         fi
      fi

      for i in  $DBWRITER_PWD/lib/*.jar; do
         CP=$CP:$i
      done

      $CLEAR

      $INFOTEXT -u "\nDatabase connection test"

      searchJDBCDriverJar $DB_DRIVER $DBWRITER_PWD/lib

      CP="${CP}:$JDBC_JAR"

      testDB
      if [ $? -eq 0 ]; then
         break
      else
         $INFOTEXT -ask y n -def y \
                  -n "Do you want to repeat database connection setup? (y/n) [y] >> " 
         if [ $? -ne 0 ]; then
            exit 1
         fi
      fi
      do_setup=1
   done
}

queryDB() {
   
   DB_TYPE=$1
   DEFAULT_PORT=$2

   dummy=$DB_HOST
   $INFOTEXT -n "\nEnter the name of your $DB_TYPE database host [$dummy] >> "
   DB_HOST=`Enter $dummy`

   if [ "$DB_PORT" = "" ]; then
      dummy=$DEFAULT_PORT
   else
      dummy=$DB_PORT
   fi
   $INFOTEXT -n "\nEnter the port of your $DB_TYPE database [$dummy] >> "
   DB_PORT=`Enter $dummy`

   if [ "$DB_NAME" = "" ]; then
      dummy=arco
   else
      dummy=$DB_NAME
   fi
   $INFOTEXT -n "\nEnter the name of your $DB_TYPE database [$dummy] >> "
   DB_NAME=`Enter $dummy`

   if [ "$DB_USER" = "" ]; then
      dummy=arco_read
   else
      dummy=$DB_USER
   fi
   $INFOTEXT -n "\nEnter the name of the database user [$dummy] >> "
   DB_USER=`Enter $dummy`
   # ask for the password of write user and store it in the variable DB_PW
   queryUserPWD
   DB_PW=$TMP_DB_PW
   
   queryDBSchema
   
}
#
#  Query the database schema
#  Uses DB_DRIVER, DB_USER
#  set the variables DB_SCHEMA
#
queryDBSchema() {
   case "$DB_DRIVER" in
     "org.postgresql.Driver")
             DB_SCHEMA=public;;
     "oracle.jdbc.driver.OracleDriver")
             DB_SCHEMA=arco_write;;
     "com.mysql.jdbc.Driver")
            DB_SCHEMA=arco;;
     *)
         $INFOTEXT "Unkown database with driver $DB_DRIVER";
         exit 1;;
   esac
   dummy=$DB_SCHEMA
   $INFOTEXT -n "\nEnter the name of the database schema [$dummy] >> "
   DB_SCHEMA=`Enter $dummy`
}


#############################################################################
# Query the parameters for a postgreSQL db connection
#############################################################################
queryPostgres()
{
   DB_SCHEMA=public
   DB_DRIVER="org.postgresql.Driver"
   if [ "$1" != "only_defaults" ]; then
      queryDB postgresql 5432
   fi
   DB_URL="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
   # tablespaces in postgresql are not available for all supported versions
   TABLESPACE="n/a"
   SYNONYMS="0"
}

#############################################################################
# Query the parameters for the oracle db connection
#############################################################################
queryOracle()
{
   DB_SCHEMA=arco_write
   DB_DRIVER="oracle.jdbc.driver.OracleDriver"
   if [ "$1" != "only_defaults" ]; then
      queryDB oracle 1521
   fi
   DB_URL="jdbc:oracle:thin:@$DB_HOST:$DB_PORT:$DB_NAME"
   TABLESPACE="USERS"
   TABLESPACE_INDEX="USERS"
   SYNONYMS="1"
}

#############################################################################
# Query the parameters for a MySQL db connection
#############################################################################
queryMysql()
{
   DB_SCHEMA=arco
   DB_DRIVER="com.mysql.jdbc.Driver"
   if [ "$1" != "only_defaults" ]; then
      queryDB mysql 3306
   fi
   DB_URL="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME"
   # tablespaces in mysql are not available
   TABLESPACE="n/a"
   SYNONYMS="0"
}

# ----------------------------------------------------------------
#  echo the sqlUtil command for connecting to the database
#  to stdout
#  Uses the variables DB_DRIVER DB_URL DB_USER and DB_PW
# ----------------------------------------------------------------
echoConnect() {
   echo "debug SEVERE"
   echo "connect $DB_DRIVER $DB_URL $DB_USER $DB_PW"
   echo "exit"
}


# ----------------------------------------------------------------
#  Run the sqlUtil
# ----------------------------------------------------------------
sqlUtil() {
  #$INFOTEXT "\nClasspath for sqlUtil ---------------------------"
  #$INFOTEXT "$CP"
  #$INFOTEXT " finished classpath -------------------------------"
  $JAVA_HOME/bin/java -classpath  $CP com.sun.grid.util.SQLUtil
  return $?
}

searchJDBCDriverJar()
{
   while true ;  do
      $INFOTEXT "\nSearching for the jdbc driver $1 \nin directory $2 "
      JDBC_JAR=`$JAVA_HOME/bin/java -cp $CP com.sun.grid.util.ClassGrep -v $1 $2/*.jar`
      res=$?

      if [ $res -eq 0 ]; then
        # we only need the first jar
        for i in $JDBC_JAR; do
          JDBC_JAR=$i
          break
        done
        export JDBC_JAR
        $INFOTEXT "\n OK, jdbc driver found"
        return 0
      elif [ $res -eq 2 ]; then
         $INFOTEXT "\nError: jdbc driver $1"
         $INFOTEXT "       not found in any jar file of directory"
         $INFOTEXT "       $2\n"         
         $INFOTEXT "Copy a jdbc driver for your database into\n this directory!"
         $INFOTEXT -n "\nPress enter to continue >> " 
         Enter
      else
        $INFOTEXT "Fatal error while searching for jdbc driver"
        $INFOTEXT "Can not continue!!!"
        exit 1
      fi
   done
}

# ---------------------------------------------------------------
# testDB
# Try to connect to the database
#
# Parameters:
#
#     $1   base directory of the installation
#          if this parameter is set the database version will be 
#          queried and updated.
#
# return codes    0    => connection OK
#                 else => connection Failed
# ---------------------------------------------------------------
testDB() {

  $INFOTEXT -ask y n -def y -n \
            "\nShould the connection to the database be tested? (y/n) [y] >> "
  dummy=$?
  if [ $dummy -eq 0 ]; then
     $INFOTEXT -n "\nTest database connection to '$DB_URL' ... "
     echoConnect | sqlUtil 2> /dev/null
     dummy=$?
     if [ $dummy -eq 0 ]; then
       $INFOTEXT "OK"
     else
       $INFOTEXT "Failed ($dummy)"
     fi
  else
     $INFOTEXT "Skip the database connection test"
     dummy=0
  fi
  return $dummy 
}

# -------------------------------------------------
# echo the commands for the sql util to stdout
# which queries the version of the dbmodel
#
#  Uses the variables DB_DRIVER, DB_URL, DB_USER, DB_PW
#  and DB_SCHEMA
# -------------------------------------------------
echoPrintDBVersion() {
   echo "debug SEVERE"
   echo "connect $DB_DRIVER $DB_URL $DB_USER $DB_PW"
   echo "debug INFO"
   echo "install print_db_version -only-id $DB_SCHEMA"
   echo "exit"
}

# -------------------------------------------------
# echo the commands for the sql util to stdout
# which queries the version of the database server
#
#  Uses the variables DB_DRIVER, DB_URL, DB_USER, DB_PW
# -------------------------------------------------
echoPrintDatabaseServerVersion() {
   echo "debug SEVERE"
   echo "connect $DB_DRIVER $DB_URL $DB_USER $DB_PW"
   echo "debug INFO"
   echo "dbversion $1"
   echo "exit"
}

# ----------------------------------------------------------------
# query the version of the database model and do an upgrade.
# Paramters:
#    $1 base directory of the installation. In the subdirectory 
#       database/<database name> the file dbdefinition.xml is expected.
#    $2 ... 1 - real installation (default value)
#           0 - pretended installation (show only the sql statements)
#
#  Return:
#          0  if the version of the database model is OK or the
#             the database model has been successfully updated.
# ----------------------------------------------------------------
updateDBVersion() {
    if [ "$2" = "" ]; then
       mode=1
    else
       mode=$2
    fi
    $INFOTEXT -n "Query database version ... " 
    dummy=`echoPrintDBVersion | sqlUtil 2> /dev/null`

    case "$dummy" in
      "-1")     $INFOTEXT "no sge tables found";;
      [0-9]*) $INFOTEXT "found version $dummy";;
      *)        $INFOTEXT "error ($dummy)";
                return 1;;
    esac

    if [ $dummy -lt $DB_VERSION ]; then
       $INFOTEXT "New version of the database model is needed"
       
      case "$DB_DRIVER" in
        "org.postgresql.Driver")
                dummy=`echoPrintDatabaseServerVersion major | sqlUtil 2> /dev/null`
                case "$dummy" in
                   [0-9]*) ;;
                   *) $INFOTEXT "error ($dummy)";
                      return 1;;
                esac
                if [ $dummy -lt 8 ]; then
                   DB_DEF=$1/database/postgres/dbdefinition_7.xml
                else
                   TABLESPACE="pg_default"
                   TABLESPACE_INDEX="pg_default"
                   DB_DEF=$1/database/postgres/dbdefinition.xml
                fi;;
        "oracle.jdbc.driver.OracleDriver")
                DB_DEF=$1/database/oracle/dbdefinition.xml;;
        "com.mysql.jdbc.Driver")
                DB_DEF=$1/database/mysql/dbdefinition.xml;;
        *)
            $INFOTEXT "Unkown database with driver $DB_DRIVER";
            exit 1;;
      esac

      if [ $mode -eq 1 ]; then
         installDB $DB_VERSION $DB_DEF
      else
         installDB -dry-run $DB_VERSION $DB_DEF
      fi

      return $?
    else 
      return 0
    fi
}

# ----------------------------------------------------------------
#  echo the sqlUtil command for connecting to the database
#  and installing a dbmodel to stdout.
#  Uses the variables DB_DRIVER DB_URL DB_USER, DB_PW, DB_SCHEMA,
#  READ_USER
#
#  Parameters:
#     [-dry-run] <version> <xml file with dbmodel>
# ----------------------------------------------------------------
echoInstall() {
   echo "debug SEVERE"
   echo "connect $DB_DRIVER $DB_URL $DB_USER $DB_PW"
   # initiate the secondary connection of read user for creating synonyms
   if [ "$SYNONYMS" = "1" ]; then
      echo "set SYNONYMS 1"
      echo "connect $DB_DRIVER $DB_URL $READ_USER $READ_USER_PW"
      echo "set SYNONYMS 0"
   fi
   echo "debug INFO"
   if [ "$DB_USER" != "" ]; then
      echo "set DB_USER $DB_USER"
   fi
   if [ "$READ_USER" != "" ]; then
      echo "set READ_USER $READ_USER"
   fi
   if [ "$DB_HOST" != "" ]; then
      echo "set DB_HOST $DB_HOST"
   fi
   if [ "$DB_NAME" != "" ]; then
      echo "set DB_NAME $DB_NAME"
   fi
   if [ "$TABLESPACE" != "n/a" ]; then
      echo "set TABLESPACE $TABLESPACE"
      echo "set TABLESPACE_INDEX $TABLESPACE_INDEX"
   fi
   echo "install $* $DB_SCHEMA"
   echo "exit"
}


# ----------------------------------------------------------------
#  Install or update the database model.
#  Parameters:
#     [-dry-run] <version> <xml file with dbmodel>
# ----------------------------------------------------------------
installDB() {

   dryrun=""
   while [ $# -gt 2 ]; do   
      if [ "$1" = "-dry-run" ]; then
         dryrun="-dry-run"
         shift
      else
         $INFOTEXT "Invalid options $1"
         exit 1
      fi
   done

   if [ "$dryrun" = "" ]; then
      if [ $1 -gt 0 ]; then
         $INFOTEXT -n -ask y n -def y \
                   "\nShould the database model be upgraded to version $1? (y/n) [y] >> "
         dummy=$?
      else
         $INFOTEXT -n -ask y n -def y \
                   "\nShould the database model version $1 be installed? (y/n) [y] >> "
         dummy=$?
      fi
   else
      dummy=0
   fi

   if [ $dummy -eq 0 ]; then
      # if the tablespaces are available, ask user to define them   
      if [ "$TABLESPACE" != "n/a" ]; then
         dummy=$TABLESPACE
         $INFOTEXT "\nThe $DB_USER must have permissions to create objects in the specified tablespace."
         while true ; do
            $INFOTEXT -n "\nEnter the name of TABLESPACE for tables [$dummy] >> "
            TABLESPACE=`Enter $dummy`
            if [ "$TABLESPACE" = "" ]; then
               # repeat the setup
               $INFOTEXT "\nThe name of the tablespace must be specified."
            else
               break
            fi
         done
         dummy=$TABLESPACE
         while true ; do
            $INFOTEXT -n "\nEnter the name of TABLESPACE for indexes [$dummy] >> "
            TABLESPACE_INDEX=`Enter $dummy`
            if [ "$TABLESPACE" = "" ]; then
               # repeat the setup
               $INFOTEXT "\nThe name of the tablespace must be specified."
            else
               break
            fi
         done
      fi

      $INFOTEXT "\nThe ARCo web application connects to the database"
      $INFOTEXT "with a user which has restricted access."
      $INFOTEXT "The name of this database user is needed to grant"
      $INFOTEXT "him access to the sge tables."
      if [ "$SYNONYMS" = "1" ]; then
         $INFOTEXT "This user will create the synonyms for the ARCo"
         $INFOTEXT "tables and views, so the user's password is needed."
      fi
      dummy=$READ_USER
      $INFOTEXT -n "\nEnter the name of this database user [$dummy] >> "
      READ_USER=`Enter $dummy`
      if [ "$SYNONYMS" = "1" ]; then
         # ask for the password of read user and store it in the variable READ_USER_PW
         queryUserPWD
         READ_USER_PW=$TMP_DB_PW
      fi

      if [ $1 -gt 0 ]; then
         $INFOTEXT -n "Upgrade to database model version $1 ... "
      else
         $INFOTEXT -n "Install database model version $1 ... "
      fi
      
      if [ "$dryrun" != "" ]; then
         $INFOTEXT "\n"
      fi
   
      echoInstall $dryrun $1 $2 | sqlUtil 2> /dev/null
      dummy=$?
      if [ $dummy -eq 0 ]; then
        $INFOTEXT "OK"        
      else
        $INFOTEXT "Failed ($dummy)"
      fi
   else
      $INFOTEXT "Installation aborted"
      dummy=1
   fi
  return $dummy 
}
