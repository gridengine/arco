#!/bin/sh
#
#  This script updates the database for dbwriter and ARCo reporting
#
#  Scriptname: updatedb
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
##########################################################################
#___INFO__MARK_END__

umask 022

DBWRITER_PWD=`pwd`
cd ..
. ./util/arch_variables
. ./util/install_modules/inst_common.sh
. $DBWRITER_PWD/inst_util.sh

BasicSettings
SetUpInfoText

JARS="jax-qname.jar jaxb-api.jar jaxb-impl.jar jaxb-libs.jar \
 namespace.jar relaxngDatatype.jar xsdlib.jar"

CP=$DBWRITER_PWD/lib/arco_common.jar:$DBWRITER_PWD/lib/dbwriter.jar
for i in $JARS; do
  CP="$CP:$DBWRITER_PWD/lib/$i"
done


queryDBWriterConfig() {
   dummy=`pwd`
   $INFOTEXT -n "\nPlease enter your SGE_ROOT [$dummy] >> "
   SGE_ROOT=`Enter $dummy`
   
   dummy="default"
   $INFOTEXT -n "\nPlease enter your SGE_CELL [$dummy] >> "
   SGE_CELL=`Enter $dummy`
   
   
   DBWRITER_CONF=$SGE_ROOT/$SGE_CELL/common/dbwriter.conf
   while [ 1 ]; do
      dummy=$DBWRITER_CONF
      $INFOTEXT -n "\nPlease enter the path to the dbwriter configuration file [$dummy]>> "
      DBWRITER_CONF=`Enter $dummy`
      
      if [ -r $DBWRITER_CONF ]; then
          break
      else 
         $INFOTEXT "Error: configuration file for dbwriter not found"
      fi 
   done
}

## Main

$INFOTEXT  "\n"
$INFOTEXT  -u "Installation / Upgrade for the @@ARCO_NAME@@ database"
$INFOTEXT  "\n"

queryJavaHome "1.4.1"

$INFOTEXT -n -ask y n -def y \
          "\nDo you have a installation of the dbwriter? (y/n) [y] >> "

if [ $? -eq 0 ]; then
   queryDBWriterConfig
   # source the dbwriter configuration
   . $DBWRITER_CONF
   
   DB_PW=$DBWRITER_USER_PW
   DB_USER=$DBWRITER_USER
   DB_URL=$DBWRITER_URL
   DB_DRIVER=$DBWRITER_DRIVER
   DB_SCHEMA=$DB_SCHEMA

   
   searchJDBCDriverJar $DB_DRIVER $DBWRITER_PWD/lib
   
   CP="${CP}:$JDBC_JAR"
   
   testDB
   
else 
   setupDB arco_write $DBWRITER_PWD/lib
fi

# database parameters a correct try update

case "$DB_DRIVER" in
  "org.postgresql.Driver")
          DB_DEF=$DBWRITER_PWD/database/postgres/dbdefinition.xml;;
  "oracle.jdbc.driver.OracleDriver")
          DB_DEF=$DBWRITER_PWD/database/oracle/dbdefinition.xml;;
  "com.mysql.jdbc.Driver")
          DB_DEF=$DBWRITER_PWD/database/mysql/dbdefinition.xml;;
  *)
      $INFOTEXT "Unkown database with driver $DB_DRIVER";
      exit 1;;
esac

DB_VERSION=8

$INFOTEXT -n -ask y n -def n \
          "\n Shall we only print all sql statements which will be executed during the upgrade? (y/n) [n] >> "

if [ $? -eq 0 ]; then
  installDB -dry-run $DB_VERSION $DB_DEF
else 
  installDB $DB_VERSION $DB_DEF
fi







