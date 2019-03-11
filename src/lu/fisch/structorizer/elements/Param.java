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

/**
 * Parameter record for subroutine diagrams, contains name, type, and possibly a default value
 * @author robertfisch
 */
public class Param {
    protected String name;
    protected String type;
    // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
    protected String defaultValue;

    public Param(String name, String type, String defaultLiteral) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultLiteral;
    }
    // END KGU#371 2019-03-07

    public Param(String name, String type) {
        this.name = name;
        this.type = type;
        // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
        this.defaultValue = null;
        // END KGU#371 2019-03-07
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
    
    // START KGU#371 2019-03-07: Enh. #385 - allow default values for parameters
    public String getDefault() {
        return this.defaultValue;
    }
    // END KGU#371 2019-03-07
    
}
