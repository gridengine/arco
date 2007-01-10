#!/bin/csh -fb
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
#  License at http://arco.sunsource.net/Gridengine_SISSL_license.html
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


set TAG = HEAD
set SOURCEDIR = "/tmp/arco"
set OUTFILE = "/tmp/arco-HEAD-src.tar.gz"
set EXCLUDEFILES = ""
set TAR = tar

#
# commandline parsing
#
while ($#argv >= 1)
   switch ("$argv[1]")
   case "-tag":
      set argv   = ($argv[2-])
      if ($#argv >= 1) then
         set TAG = $argv[1]
         set OUTFILE = "/tmp/arco-$TAG-src.tar.gz"
      else
         goto usage
         exit 1
      endif
      breaksw
   case "-o":
      set argv   = ($argv[2-])
      if ($#argv >= 1) then
         set OUTFILE = $argv[1]
      else
         goto usage
         exit 1
      endif
      breaksw
   case "-gtar":
      set TAR = gtar
      breaksw
   case "-w":
      set argv   = ($argv[2-])
      if ($#argv >= 1) then
         set SOURCEDIR = $argv[1]
      else
         goto usage
         exit 1
      endif
      breaksw
   case "-h":
   case "-help":
   case "--help":
      goto usage
      breaksw
   default:
      continue
      breaksw
   endsw
   set argv     = ($argv[2-])
end

#
# main
#

if ( ! -d $SOURCEDIR ) then
   mkdir -p $SOURCEDIR || exit 1
endif

if ( -f CVS/Root ) then
   setenv CVSROOT `cat CVS/Root`
endif

echo If the $CVSROOT is the wrong CVSROOT, press Ctrl-C

cd $SOURCEDIR || exit 1
rm -rf $SOURCEDIR/arco || exit 1

cvs -z9 -q co -r $TAG arco/source arco/Changelog
find arco -name Root -exec rm {} \;
foreach i ( $EXCLUDEFILES ) 
   find arco -name $i -exec rm {} \;
end

$TAR cvzf $OUTFILE arco
if ( $status == 0 ) then
   rm -rf $SOURCEDIR/arco
else
   echo tar failed. Leaving $SOURCEDIR/arco unchanged
endif

exit 0

#
# 
#
usage:
   echo "usage: <OPTIONS>"

   echo "OPTIONS are: "
   echo "-tag <tagname>   -> checkout tag instead of HEAD revision"
   echo "-o <file>        -> write to file <file> [default: $OUTFILE]"
   echo "-w <dir>         -> set checkout directory to <dir> [default: $SOURCEDIR]"
   echo "-gtar            -> use gtar as tar command"
   exit
