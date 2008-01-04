#!/bin/sh
#
#  (c) 2004 Sun Microsystems, Inc. Use is subject to license terms.
#
#  helper script for the Grid Engine dbWriter
#
#  Scriptname: dbwriter
#

if [ -n "$SGE_ROOT" -o -n "$SGE_CELL" ]; then
   echo "Your SGE_ROOT and SGE_CELL must be set!!!"
   exit 1
fi
$SGE_ROOT/SGE_CELL/common/sgedbwriter $@


