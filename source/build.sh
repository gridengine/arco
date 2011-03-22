#!/bin/sh

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
#  Portions of this code are Copyright 2011 Univa Inc.
#
##########################################################################
#___INFO__MARK_END__


usage() {
   echo "build.sh [-emma] <target>"
   echo " <target>    dist, clean or test"
}

if [ $# -lt 1 ]; then
   usage
   exit 1
fi

ANT_HOME=/tools/PKG/apache-ant-1.8.2

#
#  PATH to the java development kit version >= 1.5
#
if [ "$JAVA_HOME" = "" ]; then
   echo "please set the JAVA_HOME to your Java installation"
   exit 1
fi

if [ -f ./build.private ]; then
   . ./build.private
fi

if [ "$ANT_OPTS" = "" ]; then
   ANT_OPTS=-Djava.endorsed.dirs=$WSDP_HOME/jaxp/lib/endorsed
else
  ANT_OPTS="-Djava.endorsed.dirs=$WSDP_HOME/jaxp/lib/endorsed $ANT_OPTS"
fi

export ANT_HOME ANT_OPTS JAVA_HOME

modules="dbwriter"

EMMA=0
if [ $1 = "-emma" ]; then
   EMMA=1
   shift
fi


if [ $# -eq 1 ]; then
  target=$1
else
  echo "Illegal number of arguments"
  usage
  exit 1
fi

for i in $modules; do
   if [ -d $i ]; then
      orgpwd=`pwd`
      cd $i
      if [ $i = "dbwriter" -a $EMMA -eq 1 ]; then
         $ANT_HOME/bin/ant emma $target
      else
         $ANT_HOME/bin/ant $target
      fi
      res=$?
      if [ $res -ne 0 ]; then
         exit $res
      fi
      cd $orgpwd
	else
	  echo "Unknown module $i"
	fi
done

