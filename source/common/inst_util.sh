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
#  Portions of this code are Copyright 2011 Univa Inc.
#
########################################setupdb##################################
#___INFO__MARK_END__


DB_VERSION=10
DB_VERSION_NAME="6.2u1"
FILELIST_755="inst_dbwriter updatedb.sh"


# -------------------------------------------------------------------
# correctFilePermissions()
# check if the file permissions are already correct, if not it tries
# to correct them or fails
# $1 directory that we want to check relative to $2
# $2 directory, where is $1 located (SGE_ROOT)
# -------------------------------------------------------------------
correctFilePermissions()
{
   dir=$1
   sge_root=$2
   needVerify=0
   needVerifyFilePermissions $dir $sge_root
   if [ $needVerify -eq 1 ]; then
      old_adminuser="$ADMINUSER"
      #check who owns the dbwriter directory
      owner=`ls -la $sge_root | grep " ${dir}$" | awk '{print $3}'`
      if [ "$owner" != "$ADMINUSER" -a "$ADMINUSER" != default ]; then
         ADMINUSER=default
      fi
      #now check that we are not on a shared FS where root has no permissions
      if [ "$ADMINUSER" = default ]; then
         chmod 755 $sge_root/$dir > /dev/null 2>&1
      else
         $SGE_UTILBIN/adminrun $ADMINUSER chmod 755 $sge_root/$dir > /dev/null 2>&1
      fi
      if [ $? -ne 0 ]; then
         $INFOTEXT -n "\nCan't set file permissions for $sge_root/$dir directory!"
         if [ $euid = 0 ]; then
            $INFOTEXT "\nYou are probably on a shared file system where root has no permissions."
            $INFOTEXT "Start the installation as root on a host that shares this directory or"
            $INFOTEXT "change the owner of the %s directory to %s." "$dir" "$old_adminuser" 
         else
            $INFOTEXT "Unexpected owner %s of the %s directory." "$owner" "$sge_root/$dir"
            $INFOTEXT "Expected root (or %s)." "$old_adminuser"
         fi
         exit 1
      fi
      $INFOTEXT -n "   Correcting file permissions ... "
      verifyFilePermissions $SGE_ROOT/$dir
      $INFOTEXT "done"
      ADMINUSER=$old_adminuser
   fi
}
# -------------------------------------------------------------------
# needVerifyFilePermissions()
# check if the file permissions are already correct
# $1 filename
# $2 directory, where is $1 located
# -------------------------------------------------------------------
needVerifyFilePermissions()
{
   checkPermissions $1 $2
   FILESET=`ls $2/$1`
   for fil in $FILESET; do
      needVerifyGetFilePermissions "$fil" "$2/$1"
   done
}

# -------------------------------------------------------------------
# needVerifyGetFilePermissions()
# check if the file permissions to the files (umask 022)
# $1 filename
# $2 directory, where is $1 located
# ------------------------------------------------------------------
needVerifyGetFilePermissions()
{
   if [ -d "$2/$1" ]; then
      needVerifyFilePermissions $1 $2
   else
      checkPermissions $1 $2
   fi
}

# -------------------------------------------------------------------
# checkPermissions()
# check the file permissions to the files (umask 022)
# $1 filename
# $2 directory, where is $1 located
# ------------------------------------------------------------------
checkPermissions()
{
   cmd="ls -la $2/$1"
   perms="-rw-r--r--"
   if [ -d "$2/$1" ]; then
      perms="drwxr-xr-x"
      cmd="ls -la $2 | grep \ ${1}$"
   fi
   for f in $FILELIST_755; do
      if [ "$1" = "$f" ]; then
         perms="-rwxr-xr-x"
         break
      fi
   done
   cur_perms=`eval $cmd | awk '{print substr($1,0,10)}'`
   if [ "$cur_perms" != "$perms" ]; then
      $INFOTEXT "   found incorrect permissions %s for %s" "$cur_perms" "$2/$1"
      needVerify=1
   fi
}

# -------------------------------------------------------------------
# verifyFilePermissions()
# set the file permissions to the files (umask 022)
# $1 directory
# -------------------------------------------------------------------
verifyFilePermissions()
{
   ExecuteAsAdmin chmod 755 "$1"
   FILESET=`ls $1`
   for fil in $FILESET; do
      setFilePermissions "$fil" "$1"
   done
}

# -------------------------------------------------------------------
# verifyFilePermissions()
# set the file permissions to the files (umask 022)
# $1 filename
# $2 directory, where is $1 located
# ------------------------------------------------------------------
setFilePermissions()
{
   if [ -d "$2/$1" ]; then
      verifyFilePermissions "$2/$1"
   else
      for f in $FILELIST_755; do
         if [ "$1" = "$f" ]; then
            ExecuteAsAdmin chmod 755 "$2/$1"
            return
         fi
      done
      ExecuteAsAdmin chmod 644 "$2/$1"
   fi
}

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
            JAVA_HOME=$dummy; export JAVA_HOME
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

    printf "%d%02d%02d%03d\n" $major $minor $micro $patch
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
#     $2   ask the user for input parameters - 1 yes, 0 no
setupDB()
{
   DBWRITER_PWD="$1"
   ask_user=$2

   if [ $ask_user -eq 1 ]; then
      $CLEAR
      $INFOTEXT -u "\nSetup your database connection parameters"
   fi

   while :
   do
      if [ $ask_user -eq 1 ]; then
         if [ "$DB_USER" = "" ]; then
            DB_USER=arco_write      # default database username
         fi
         dummy=""
         $INFOTEXT -n \
                  "\nEnter your database type ( o = Oracle, p = PostgreSQL, m = MySQL ) [$dummy] >> "
         dbtype=`Enter $dummy`
         if [ "$dbtype" = 'p' ]; then
             queryPostgres $ask_user
         elif [ "$dbtype" = 'o' ]; then
             queryOracle $ask_user
         elif [ "$dbtype" = 'm' ]; then
             queryMysql $ask_user
         else
            $INFOTEXT "\nDatabase type must be specified!"
            continue
         fi
      else
         case "$DB_DRIVER" in
         "org.postgresql.Driver")
            queryPostgres $ask_user;;
         "oracle.jdbc.driver.OracleDriver")
            queryOracle $ask_user;;
         "com.mysql.jdbc.Driver")
            queryMysql $ask_user;;
         *)
            ask_user=1
            $INFOTEXT "Unknown database with driver $DB_DRIVER";
            $INFOTEXT -wait -n "Hit <RETURN> to continue >> ";
            continue;;
         esac
      fi

      for i in  $DBWRITER_PWD/lib/*.jar; do
         CP=$CP:$i
      done

      # query other required database parameters
      queryDBParams $ask_user

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
      ask_user=1
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

}

#
#  Query the database schema
#  Uses DB_DRIVER, DB_USER
#  set the variables DB_SCHEMA
#
queryDBSchema() {
   ask_schema=$1
   if [ "$DB_SCHEMA" = "" ]; then
      case "$DB_DRIVER" in
        "org.postgresql.Driver")
                DB_SCHEMA=public;;
        "oracle.jdbc.driver.OracleDriver")
                DB_SCHEMA=arco_write;;
        "com.mysql.jdbc.Driver")
               DB_SCHEMA="n/a";;
        *)
            $INFOTEXT "Unkown database with driver $DB_DRIVER";
            exit 1;;
      esac
      ask_schema=1
   fi
   if [ $ask_schema -eq 1 ]; then   
      if [ "$DB_SCHEMA" != "n/a" ]; then
         dummy=$DB_SCHEMA
         while true ; do
            $INFOTEXT -n "\nEnter the name of the database schema [$dummy] >> "
            DB_SCHEMA=`Enter $dummy`
            if [ "$DB_SCHEMA" = "" ]; then
               # repeat the setup
               $INFOTEXT "\nThe name of the schema must be specified."
            else
               break
            fi
         done
      fi
   fi

}

#############################################################################
# Query the parameters for a PostgreSQL db connection
#############################################################################
queryPostgres()
{
   ask_user=$1
   if [ "$DB_DRIVER" = "" ]; then
      DB_DRIVER="org.postgresql.Driver"
   fi
   if [ $ask_user -eq 1 ]; then
      queryDB postgresql 5432
   fi
   if [ "$DB_URL" = ""  -o  $ask_user -eq 1 ]; then
      DB_URL="jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
   fi
   # we support psql 8.0 and higher, where the tablespaces are available
   TABLESPACE_DEFAULT="pg_default"
   SYNONYMS="0"
   FOREIGN_KEY_SUFFIX="fkey"
}

#############################################################################
# Query the parameters for the Oracle db connection
#############################################################################
queryOracle()
{
   ask_user=$1
   if [ "$DB_DRIVER" = "" ]; then
      DB_DRIVER="oracle.jdbc.driver.OracleDriver"
   fi
   if [ $ask_user -eq 1 ]; then
      queryDB oracle 1521
   fi
   if [ "$DB_URL" = "" -o  $ask_user -eq 1 ]; then
      DB_URL="jdbc:oracle:thin:@$DB_HOST:$DB_PORT:$DB_NAME"
   fi
   TABLESPACE_DEFAULT="USERS"
   SYNONYMS="1"
   FOREIGN_KEY_SUFFIX="n/a"
}

#############################################################################
# Query the parameters for a MySQL db connection
#############################################################################
queryMysql()
{
   ask_user=$1
   if [ "$DB_DRIVER" = "" ]; then
      DB_DRIVER="com.mysql.jdbc.Driver"
   fi
   if [ $ask_user -eq 1 ]; then
      queryDB mysql 3306
   fi
   if [ "$DB_URL" = "" -o  $ask_user -eq 1 ]; then
      DB_URL="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME"
   fi
   # tablespaces in mysql are not available
   TABLESPACE_DEFAULT="n/a"
   SYNONYMS="0"
   # we have one foreign key per table, so the suffix contains _1, if we have more
   # keys this should be changed
   FOREIGN_KEY_SUFFIX="ibfk_1"
}

#
#  Query the database parameters
#  Parameter: $1 .. ask user input 1, do not ask 0
#
queryDBParams()
{
   ask_db_param=$1
   queryTablespace $ask_db_param
   queryDBSchema $ask_db_param
   queryReadUser $ask_db_param
}

#
#  Query tablespace names
#  Parameter: $1 .. ask user input 1, do not ask 0
#
queryTablespace()
{
   ask_tbl=$1
   if [ "$TABLESPACE" = "" ]; then
      TABLESPACE=$TABLESPACE_DEFAULT
      TABLESPACE_INDEX=$TABLESPACE_DEFAULT
      ask_tbl=1
   fi
   if [ $ask_tbl -eq 1 ]; then
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
         dummy=$TABLESPACE_INDEX
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
   fi
}

#
#  Query the database read user
#  Parameter: $1 .. ask user input 1, do not ask 0
#
queryReadUser()
{
   ask_read_user=$1
   if [ "$READ_USER" = "" ]; then
      READ_USER=arco_read
      ask_read_user=1
   fi
   if [ $ask_read_user -eq 1 ]; then
      $INFOTEXT "\nApplications should connect to the database as a user which has restricted"
      $INFOTEXT "access. The name of this database user is needed to grant him access to the sge tables"
      $INFOTEXT "and must be different from $DB_USER."
      dummy=$READ_USER
      while :
      do
         $INFOTEXT -n "\nEnter the name of this database user [$dummy] >> "
         READ_USER_TMP=`Enter $dummy`
         if [ "$READ_USER_TMP" = "$DB_USER" ]; then
            $INFOTEXT "The user must be different from $DB_USER."
         else
            break
         fi
      done
      READ_USER=$READ_USER_TMP
      if [ "$SYNONYMS" = "1" ]; then
         # ask for the password of read user and store it in the variable READ_USER_PW
         $INFOTEXT "\nThis user will also create the synonyms for the ARCo tables and views."
         queryUserPWD
         READ_USER_PW=$TMP_DB_PW
      fi
   fi
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
# Parameters:
#   $1 parameter to be passed to print_db_version
#      either -only-id which print just the version id
#      or     -only-name which prints just the version name
#
#  Uses the variables DB_DRIVER, DB_URL, DB_USER, DB_PW
#  and DB_SCHEMA
# -------------------------------------------------
echoPrintDBVersion() {
   echo "debug SEVERE"
   echo "connect $DB_DRIVER $DB_URL $DB_USER $DB_PW"
   echo "debug INFO"
   echo "install print_db_version $1 $DB_SCHEMA"
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
    db_version=`echoPrintDBVersion -only-id | sqlUtil 2> /dev/null`
    db_name=`echoPrintDBVersion -only-name | sqlUtil 2> /dev/null`

    case "$db_version" in
      "-1")     $INFOTEXT "no sge tables found";;
      [0-9]*) $INFOTEXT "found version $db_version $db_name";;
      *)        $INFOTEXT "error ($db_version)";
                return 1;;
    esac

    if [ $db_version -le $DB_VERSION -a "$db_name" != "$DB_VERSION_NAME" ]; then
       $INFOTEXT "New version of the database model is needed" 
    
      case "$DB_DRIVER" in
        "org.postgresql.Driver")
                DB_DEF=$1/database/postgres/dbdefinition.xml;;
        "oracle.jdbc.driver.OracleDriver")
                DB_DEF=$1/database/oracle/dbdefinition.xml;;
        "com.mysql.jdbc.Driver")
                DB_DEF=$1/database/mysql/dbdefinition.xml;;
        *)
            $INFOTEXT "Unkown database with driver $DB_DRIVER";
            exit 1;;
      esac

      if [ $mode -eq 1 ]; then
         installDB $DB_VERSION $DB_VERSION_NAME $DB_DEF
      else
         installDB -dry-run $DB_VERSION $DB_VERSION_NAME $DB_DEF
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
#     [-dry-run] <version> <version name> <xml file with dbmodel>
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
   if [ "$DB_SCHEMA" != "n/a" ]; then
      echo "set DB_SCHEMA $DB_SCHEMA"
   fi
   if [ "$FOREIGN_KEY_SUFFIX" != "n/a" ]; then
      echo "set FOREIGN_KEY_SUFFIX $FOREIGN_KEY_SUFFIX"
   fi

   echo "install $* $DB_SCHEMA"
   echo "exit"
}


# ----------------------------------------------------------------
#  Install or update the database model.
#  Parameters:
#     [-dry-run] <version ID> <version name> <xml file with dbmodel>
# ----------------------------------------------------------------
installDB() {
   
   dryrun=""
   while [ $# -gt 3 ]; do   
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
                   "\nShould the database model be upgraded to version $1 $2? (y/n) [y] >> "
         dummy=$?
      else
         $INFOTEXT -n -ask y n -def y \
                   "\nShould the database model version $1 $2 be installed? (y/n) [y] >> "
         dummy=$?
      fi
   else
      dummy=0
   fi
   
   if [ $dummy -eq 0 ]; then
      if [ $1 -gt 0 ]; then
         $INFOTEXT -n "Upgrade to database model version $1 $2... \n"
      else
         $INFOTEXT -n "Install database model version $1 $2... \n"
      fi
      
      if [ "$dryrun" != "" ]; then
         $INFOTEXT "\n"
      fi
   
      echoInstall $dryrun $1 $2 $3 | sqlUtil 2> /dev/null
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

#-------------------------------------------------------------------------
# Execute command as user $ADMINUSER
# if ADMINUSER = default then execute command unchanged
#
# uses binary "adminrun" form SGE distribution
#
# USES: variables "$verbose"    (if set to "true" print arguments)
#                  $ADMINUSER   (if set to "default" do not use "adminrun)
#                 "$SGE_UTILBIN"  (path to the binary in utilbin)
#
ExecAsAdmin()
{
   if [ $ADMINUSER = default ]; then
      $*
   else
      $SGE_UTILBIN/adminrun $ADMINUSER "$@"
   fi
}
