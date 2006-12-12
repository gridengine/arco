#!/bin/sh
#
#  (c) 2004 Sun Microsystems, Inc. Use is subject to license terms.
#
#  helper script for the Grid Engine dbWriter
#
#  Scriptname: dbwriter
#


usage() {
   echo "dbwriter.sh  [print <setting>] [-h]"
   echo ""
   echo "  If the print argument is given a dbwriter setting is printed"
   echo "  to stdout. The following settings are available:"
   echo ""
   echo "       pid_file   print the default pid file"
   echo "       log_file   print the default log file"
   echo "       spool_dir  print the default spool directory"
   echo ""
   echo "  Otherwise start the dbwriter. The content environment variable JVMARGS"
   echo "  is treated as options for the java virtual machine. If JAVA_HOME is set the"
   echo "  Java virtual machine at \$JAVA_HOME/bin/java is started"
   echo "  If the -h options is given this help text is printed"
   echo ""
}



# --------------------------------------------------------------------------
#  echo all options for the dbWriter
# --------------------------------------------------------------------------
echoOptions()
{
    echo "DBWRITER_CONTINOUS=$DBWRITER_CONTINOUS"              
    echo "DBWRITER_INTERVAL=$DBWRITER_INTERVAL"                
    echo "DBWRITER_DRIVER=$DBWRITER_DRIVER"                    
    echo "DBWRITER_URL=$DBWRITER_URL"                          
    echo "DBWRITER_USER=$DBWRITER_USER"                        
    echo "DBWRITER_USER_PW=$DBWRITER_USER_PW"                  
    echo "DBWRITER_REPORTING_FILE=$DBWRITER_REPORTING_FILE"    
    echo "DBWRITER_SHARE_LOG_FILE=$DBWRITER_SHARE_LOG_FILE"    
    echo "DBWRITER_STATISTIC_FILE=$DBWRITER_STATISTIC_FILE"    
    echo "DBWRITER_CALCULATION_FILE=$DBWRITER_CALCULATION_FILE"
    echo "DBWRITER_DELETION_FILE=$DBWRITER_DELETION_FILE"      
    echo "DBWRITER_DEBUG=$DBWRITER_DEBUG"
    echo "DBWRITER_SQL_THRESHOLD=$DBWRITER_SQL_THRESHOLD"
}

# --------------------------------------------------------------------------
# Start the dbwriter
# --------------------------------------------------------------------------
startDBWriter() {
   
   # --------------------------------------------------------------------------
   # Setup the classpath
   # Allow setting of an initial classpath, e.g. for running instrumented code
   # (code coverage).
   # --------------------------------------------------------------------------
   if [ "x$DBWRITER_CLASSPATH" != "x" ]; then
      CP=$DBWRITER_CLASSPATH
   fi

   LIBDIR=$SGE_ROOT/dbwriter/lib
   for i in $LIBDIR/*.jar
   do
     if [ "$CP" = "" ]; then
        CP="$i"
     else
        CP="$CP:$i"
     fi
   done
   
   if [ "$JVMARGS" = "" ]; then
      JVMARGS="-classpath $CP"
   else
      JVMARGS="$JVMARGS -classpath $CP"
   fi
   
   if [ "$JAVA_HOME" = "" ]; then
      JAVA=`which java`
   else 
      JAVA=$JAVA_HOME/bin/java
   fi
   
   DBWRITER_OPTIONS=""
   USE_DEFAULT_LOG_FILE=1
   USE_DEFAULT_PID_FILE=1
   while [ $# -gt 1 ]; do
      if [ "$1" = "-pid" -a $# -gt 1 ]; then
        shift
        USE_DEFAULT_PID_FILE=0
        PID_FILE=$1
        DBWRITER_OPTIONS="$DBWRITER_OPTIONS -pid \"$PID_FILE\""
      elif [ "$1" = "-logfile" -a  $# -gt 1 ]; then
         shift
         USE_DEFAULT_LOG_FILE=0
         LOG_FILE=$1
         DBWRITER_OPTIONS="$DBWRITER_OPTIONS -logfile \"$LOG_FILE\""
      else
        DBWRITER_OPTIONS="$DBWRITER_OPTIONS \"$1\""
      fi
      shift
   done
   
   if [ $USE_DEFAULT_PID_FILE -eq 1 ]; then
      DBWRITER_OPTIONS="$DBWRITER_OPTIONS -pid \"$PID_FILE\""
   fi
   if [ $USE_DEFAULT_LOG_FILE -eq 1 ]; then
      DBWRITER_OPTIONS="$DBWRITER_OPTIONS -logfile \"$LOG_FILE\""
   fi
   
   #echo "DBWRITER_OPTIONS=$DBWRITER_OPTIONS"
   echoOptions | eval $JAVA $JVMARGS com/sun/grid/reporting/dbwriter/ReportingDBWriter $DBWRITER_OPTIONS
   exit $?
}

# --------------------------------------------------------------------------
# Print a setting
# --------------------------------------------------------------------------
printSetting () {
   if [ $# -ne 1 ]; then
     usage
     exit 1
   fi
   
   case "$1" in
      "pid_file")  echo "$PID_FILE"; exit 0;;
      "log_file")  echo "$LOG_FILE"; exit 0;;
      "spool_dir") echo "$SPOOL_DIR"; exit 0;;
      "*")         echo "Unknown setting $1"; exit 1;;
   esac
}


# --------------------------------------------------------------------------
# Main
# --------------------------------------------------------------------------

case "$1" in
 "print") mode="print"; shift;;
 "-h")    usage; exit 0;;
 *)     mode="start";;
esac

if [ "$SGE_ROOT" = "" ]; then
   echo "Can not start dbwriter, SGE_ROOT not set"
   exit 1
fi

if [ "$SGE_CELL" = "" ]; then
   echo "Can not start dbwriter, SGE_CELL not set"
   exit 1
fi

DBWRITER_CONF=$SGE_ROOT/$SGE_CELL/common/dbwriter.conf

if [ ! -r $DBWRITER_CONF ]; then
   echo "Can not read dbwriter configuration file ($DBWRITER_CONF)"
   exit 1
fi
# --------------------------------------------------------------------------
# Source the dbwriter config
# --------------------------------------------------------------------------
. $DBWRITER_CONF
if [ $? -ne 0 ]; then
   echo "Can not source dbwriter configuration file ($DBWRITER_CONF)"
   exit 1
fi

if [ "$SPOOL_DIR" = "" ]; then
   echo "Sorry, no SPOOL_DIR defined. Please check the dbwriter configuration"
   echo "($DBWRITER_CONF)"
   exit 1
fi

PID_FILE=$SPOOL_DIR/dbwriter.pid
LOG_FILE=$SPOOL_DIR/dbwriter.log

case $mode in
  "start") 
     startDBWriter $*;;
  "print")
     printSetting $*;;
  *) echo "Unknown mode $mode"; exit 1;;
esac
  


