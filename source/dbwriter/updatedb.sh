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
   $INFOTEXT -n "\nEnter your SGE_ROOT [$dummy] >> "
   SGE_ROOT=`Enter $dummy`
   
   dummy="default"
   $INFOTEXT -n "\nEnter your SGE_CELL [$dummy] >> "
   SGE_CELL=`Enter $dummy`
   
   
   DBWRITER_CONF=$SGE_ROOT/$SGE_CELL/common/dbwriter.conf
   while [ 1 ]; do
      dummy=$DBWRITER_CONF
      $INFOTEXT -n "\nEnter the path to the dbwriter configuration file [$dummy]>> "
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
$INFOTEXT  -u "\nInstallation / Upgrade for the @@ARCO_NAME@@ database"
$INFOTEXT  "\n"

queryJavaHome "1.4.1"

queryDBWriterConfig

setupDB $DBWRITER_PWD $DBWRITER_CONF

DB_VERSION=8

$INFOTEXT -n -ask y n -def y \
          "\n Shall we only print all sql statements which will be executed during the upgrade? (y/n) [y] >> "

if [ $? -eq 0 ]; then
  updateDBVersion $DBWRITER_PWD 0
else
  updateDBVersion $DBWRITER_PWD
fi
