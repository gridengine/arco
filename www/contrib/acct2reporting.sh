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
##########################################################################
#___INFO__MARK_END__
#
#  This script converts the SGE5.3, SGE6.0 or SGE6.1 accounting file 
# into a SGE6.0 or SGE6.1 reporting file
#
# In order to convert SGE6.0 or SGE6.1 accounting file into a SGE6.2 reporting
# file, change the last line of the script to look like this:
#
# cat $ACCT_FILE | awk -F: '{ if( substr($0,0,1) == "#" ) print $0; else  printf ("%d:acct:%s:0:0\n",$9+1000,$0)}' 
##########################################################################

usage() {
  echo "acct2reporting <SGE 5.3 accouting file>"
  exit 1
}

if [ $# -ne 1 ]; then
  usage
fi

ACCT_FILE=$1

if [ ! -f $ACCT_FILE ]; then
    echo "accouting file $ACCT_FILE not found"
	 usage
fi

cat $ACCT_FILE | awk -F: '{ if( substr($0,0,1) == "#" ) print $0; else  printf ("%d:acct:%s\n",$9+1000,$0)}'

