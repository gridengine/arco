//<!--
//
// ident	"@(#)stylesheet.js 1.4 03/07/29 SMI"
//
// Copyright 2002-2003 by Sun Microsystems, Inc. All rights reserved.
// Use is subject to license terms.
//
// This Javascript code will add a stylesheet according to the variables
// set via the browserVersion.js Javascript file.
//
    if (is_ie5up) {
        // IE 5.x or above.
        document.write("<link href='/com_sun_web_ui/css/css_ie5win.css' type='text/css' rel='stylesheet' />")
    } else if (is_nav4 && is_win) {
        // Netscape 4 Windows.
        document.write("<link href='/com_sun_web_ui/css/css_ns4win.css' type='text/css' rel='stylesheet' />")
    } else if (is_nav4 && is_sun) {
        // Netscape 4 Solaris.
        document.write("<link href='/com_sun_web_ui/css/css_ns4sol.css' type='text/css' rel='stylesheet' />")
    } else {
        // All others
        document.write("<link href='/com_sun_web_ui/css/css_ns6up.css' type='text/css' rel='stylesheet' />")
    }
//-->
