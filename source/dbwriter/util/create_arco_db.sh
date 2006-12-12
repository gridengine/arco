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
##########################################################################
#___INFO__MARK_END__

usage() {

   echo "create_arco_db <pg_data_path>"
   exit 1   
}

shutdown() {
   $PG_INST_DIR/bin/pg_ctl -D $PG_DATA -m fast stop
   if [ $? -ne 0 ]; then
     echo "shutdown of postmaster failed"
     exit 1
   fi
}


if [ $# -lt 1 ]; then
  usage
fi

PG_DATA=$1

if [ ! -w $PG_DATA ]; then
   echo "$PG_DATA is not readable"
   usage
fi

if [ ! -d $PG_DATA ]; then
   echo "$PG_DATA is not a directory"
   usage
fi

if [ "x$PG_INST_DIR" = "x" ]; then
   PG_INST_DIR=/vol2/tools/SW/postgresql-8.0.1/$ARCH
fi
RD_HOME=/vol2/tools/SW/readline-5.0/$ARCH

if [ "$LD_LIBRARY_PATH" != "" ]; then
  LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${PG_INST_DIR}/lib:${RD_HOME}/lib
else 
  LD_LIBRARY_PATH=${PG_INST_DIR}/lib:${RD_HOME}/lib
fi
export LD_LIBRARY_PATH

printf "run initdb: "
$PG_INST_DIR/bin/initdb -D $PG_DATA > /dev/null
if [ $? -ne 0 ]; then
    echo "failed"
    exit 1
fi
echo "OK"

printf "create %s: " $PG_DATA/pg_hba.conf

# create a pg_hba.conf 
echo "# \"local\" is for Unix domain socket connections only"      > $PG_DATA/pg_hba.conf
echo "local   all         all                               trust" >> $PG_DATA/pg_hba.conf
echo "# IPv4 local connections:"                                   >> $PG_DATA/pg_hba.conf
echo "host    all         all         127.0.0.1/32          trust" >> $PG_DATA/pg_hba.conf
echo "# all host in the subnet 129.157.141.0"                      >> $PG_DATA/pg_hba.conf
echo "host    all         all         129.157.141.0/24        md5" >> $PG_DATA/pg_hba.conf
echo "# IPv6 local connections:" >> $PG_DATA/pg_hba.conf
echo "host    all         all         ::1/128               trust" >> $PG_DATA/pg_hba.conf

echo "OK"

printf "start the postmaster: "
$PG_INST_DIR/bin/pg_ctl -D $PG_DATA -o "-i" -l  $PG_DATA/pg.log start > /dev/null

if [ $? -ne 0 ]; then
   echo "failed"
   exit 1
fi

sleep 3
echo "OK"

printf "create postgres database: "
$PG_INST_DIR/bin/createdb postgres > /dev/null
if [ $? -ne 0 ]; then
   echo "failed"
   shutdown
   exit 1
fi
echo "OK"

printf "create user arco_write: "
$PG_INST_DIR/bin/psql -c "CREATE USER arco_write PASSWORD 'arco_write' CREATEDB CREATEUSER;" postgres > /dev/null
if [ $? -ne 0 ]; then
   echo "failed"
   shutdown
   exit 1
fi
echo "OK"

printf "create user arco_read: "
$PG_INST_DIR/bin/psql -c "CREATE USER arco_read PASSWORD 'arco_read' NOCREATEDB NOCREATEUSER;" postgres > /dev/null
if [ $? -ne 0 ]; then
   echo "failed"
   shutdown
   exit 1
fi
echo "OK"

printf "create arco database: "
$PG_INST_DIR/bin/createdb -O arco_write arco > /dev/null
if [ $? -ne 0 ]; then
   echo "failed"
   shutdown
   exit 1
fi
echo "OK"

echo "ARCo database successfully initialized"






