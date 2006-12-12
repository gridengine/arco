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
package com.sun.grid.arco.chart;

/**
 * Description: Enumeration of chart types<br>
 */
public final class ChartTypes {
    private String name;
    private int xl, yl, tl;
    private boolean hideLegendAllowed;
    
    protected static java.util.List registeredChartTypes;

    private ChartTypes(String name, int xl, int yl, int tl, boolean hideLegendAllowed) {
        this.name = name;
        this.xl = xl;
        this.yl = yl;
        this.tl = tl;
        this.hideLegendAllowed = hideLegendAllowed;
        if (registeredChartTypes == null) {
            registeredChartTypes = new java.util.ArrayList();
        }
        registeredChartTypes.add(this);
    }

    private ChartTypes(String name, int xl, int yl, int tl) {
        this(name, xl, yl, tl, true);
    }
    
    
    public int getMinimumAttributeCount() {
        int retVal = 0;
        for (int i=0; i<getChartInfo().length;i++) {
            if (getChartInfo()[i] > 0) {
                retVal++;
            }
        }
        return retVal;
    }

    /**
     * Returns the maximum amount of attributes this chart type needs.
     * Index 0 holds the amount for the x axis, index 1 holds the amount
     * for the y axis and index 2 holds the amount for the type attributes.
     */
    public int[] getChartInfo() {
        return new int[]{xl, yl, tl};
    }

    /**
     *returns the name of the chart
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return name;
    }
    
    public boolean equals( Object obj ) {
       return obj instanceof ChartTypes &&
              name.equals( ((ChartTypes)obj).name );
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    /** Getter for property hideLegendAllowed.
     * @return Value of property hideLegendAllowed.
     *
     */
    public boolean isHideLegendAllowed() {
        return hideLegendAllowed;
    }
    
    public static java.util.List getAvailableChartTypes() {
        return registeredChartTypes;
    }

    public static ChartTypes getChatTypeByName(String name) {
        for (int i=0; i<registeredChartTypes.size(); i++) {
            ChartTypes ct = (ChartTypes)registeredChartTypes.get(i);
            if (ct.getName().equals(name)) {
                return ct;
            }
        }
        return null;
    }
    
    /** definitions for a Bar-Chart
     */
    public static final ChartTypes BAR_CHART = new ChartTypes("Bar chart", 4, 1, 0);
    /** definitions for a Pie-Chart
     */
    public static final ChartTypes PIE_CHART = new ChartTypes("Pie chart", 1, 1, 0);
    /** definitions for a Line-Chart
     */
    public static final ChartTypes LINE_CHART = new ChartTypes("Line chart", 1, 1, 1);
    /** definitions for a stacked-line-Chart
     */
    public static final ChartTypes STACKED_LINE_CHART = new ChartTypes("Stacked line chart", 1, 1, 1);

    /** final value for access to the value for the x-Axis
     */
    public static final int X_AXIS = 0;
    /** final value for access to the value for the y-Axis
     */
    public static final int Y_AXIS = 1;
    /** final value for access to the value for the type
     */
    public static final int TYPE = 2;

}
