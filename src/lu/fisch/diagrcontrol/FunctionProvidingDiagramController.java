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
package lu.fisch.diagrcontrol;

/******************************************************************************************************
 *
 *      Author:         Kay Gürtzig
 *
 *      Description:    Inteface providing an API for retrieving and executing DiagramController functions.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Kay Gürtzig     2017.06.29      First Issue (introduced for enhancement #424)
 *      Kay Gürtzig     2017.10.28      Integrated in DiagramController and completely redesigned
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      Superfluous now
 *
 ******************************************************************************************************///

/**
 * Interface allowing a DiagramController to provide functions returning a result. Since
 * function calls may be nested, it's important to provide a map of provided functions
 * such that relevant calls may be identified within a complex expression and then be
 * pre-interpreted.
 * @author Kay Gürtzig
 */
public interface FunctionProvidingDiagramController extends DiagramController {

}
