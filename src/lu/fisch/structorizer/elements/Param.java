/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lu.fisch.structorizer.elements;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Parameter record for subroutine diagrams, contains name, type, and default value
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2015-12-10      First Issue
 *      Kay Gürtzig     2019-03-07      Enh. #385 support for optional parameters
 *      Kay Gürtzig     2021-02-04      Bugfix #925 - we must be able to suppress "const" prefixes in type
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************///

/**
 * Parameter record for subroutine diagrams, contains name, type, and possibly a default value
 * @author robertfisch
 */
public class Param {
    protected String name;
    protected String type;
    // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
    protected String defaultValue;

    /**
     * Creates a Param object for a (possibly optional) parameter
     * @param name - the parameter name as declared
     * @param type - the (possibly prefixed) type description as declared
     * (a possible prefix might be "const")
     * @param defaultLiteral a default value description (usually some literal)
     * for an optional parameter, {@code null} for a mandatory parameter
     */
    public Param(String name, String type, String defaultLiteral) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultLiteral;
    }
    // END KGU#371 2019-03-07

    /**
     * Creates a Param object for a regular (mandatory) parameter
     * @param name - the parameter name as declared
     * @param type - the (possibly prefixed) type description as declared
     * (a possible prefix might be "const")
     * @see #Param(String, String, String)
     */
    public Param(String name, String type) {
        this.name = name;
        this.type = type;
        // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
        this.defaultValue = null;
        // END KGU#371 2019-03-07
    }

    /**
     * @return the declared parameter name
     * @see #getType(boolean)
     * @see #getDefault()
     */
    public String getName() {
        return name;
    }

    /**
     * Provides the type description of the parameter
     * @param withoutPrefix - if {@code true} then prefixes like "const" will be
     * removed such that the "true" type description is obtained
     * @return the type description of the parameter declaration
     * @see #getName()
     * @see #getDefault()
     */
    // START KGU#925 2021-02-04: Bugfix #925
    //public String getType() {
    public String getType(boolean withoutPrefix) {
        if (type != null) {
            String typeDescr = this.type;
            // FIXME This approach is order-sensitive in case of several prefixes
            for (String prefix: new String[] {"const "}) {
                if (typeDescr.startsWith(prefix)) {
                    typeDescr = typeDescr.substring(prefix.length());
                }
            }
            return typeDescr;
        }
    // END KGU#925 2021-02-04
        return type;
    }
    // END KGU#925 2021-02-04
    
    // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
    /**
     * @return {@code null} in case of a mandatory parameter or the default value
     * string (usually a literal) in case of an optional parameter
     * @see #getName()
     * @see #getType(boolean)
     */
    public String getDefault() {
        return this.defaultValue;
    }
    // END KGU#371 2019-03-07
    
}
