/*___INFO__MARK_BEGIN__*/
/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the Sun Industry Standards Source License Version 1.2
 *
 *  Sun Microsystems Inc., March, 2001
 *
 *
 *  Sun Industry Standards Source License Version 1.2
 *  =================================================
 *  The contents of this file are subject to the Sun Industry Standards
 *  Source License Version 1.2 (the "License"); You may not use this file
 *  except in compliance with the License. You may obtain a copy of the
 *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
 *
 *  Software provided under this License is provided on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
 *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 *  See the License for the specific provisions governing your rights and
 *  obligations concerning the Software.
 *
 *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
 *
 *   Copyright: 2001 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/

DROP TABLE SGE_JOB_USAGE    CASCADE CONSTRAINTS;
DROP TABLE SGE_JOB_LOG      CASCADE CONSTRAINTS;
DROP TABLE SGE_JOB_REQUEST  CASCADE CONSTRAINTS;
DROP TABLE SGE_JOB          CASCADE CONSTRAINTS;
DROP TABLE SGE_QUEUE_VALUES CASCADE CONSTRAINTS;
DROP TABLE SGE_QUEUE        CASCADE CONSTRAINTS;
DROP TABLE SGE_HOST_VALUES CASCADE CONSTRAINTS;
DROP TABLE SGE_HOST        CASCADE CONSTRAINTS;
DROP TABLE SGE_DEPARTMENT_VALUES CASCADE CONSTRAINTS;
DROP TABLE SGE_DEPARTMENT        CASCADE CONSTRAINTS;
DROP TABLE SGE_PROJECT_VALUES CASCADE CONSTRAINTS;
DROP TABLE SGE_PROJECT        CASCADE CONSTRAINTS;
DROP TABLE SGE_USER_VALUES CASCADE CONSTRAINTS;
DROP TABLE SGE_USER        CASCADE CONSTRAINTS;
DROP TABLE SGE_GROUP_VALUES CASCADE CONSTRAINTS;
DROP TABLE SGE_GROUP        CASCADE CONSTRAINTS;
DROP TABLE SGE_SHARE_LOG        CASCADE CONSTRAINTS;

DROP USER ARCO_READ

COMMIT;
EXIT;
