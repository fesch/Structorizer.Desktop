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

package lu.fisch.graphics;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    Class to represent a drawing canvas.
 *						Aims to work like a "TCanvas" in Delphi
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author          Date            Description
 *      ------          ----            -----------
 *      Bob Fisch       2007-12-10      First Issue
 *      Kay Gürtzig     2016-07-27      Issue #208: New public method fillRoundRect()
 *      Kay Gürtzig     2016-10-13      Enh. #270: Method hatchedRect() added to overlay a hatched pattern
 *      Kay Gürtzig     2017-05-16      Enh. #389: New methods for polygons, API changes
 *      Kay Gürtzig     2017-05-17      Issue #405: API enhancement for rotated drawing
 *      Kay Gürtzig     2021-01-02      Enh. #905: New field `flags' (with related methods) and new method getColor
 *
 ******************************************************************************************************
 *
 *      Comment:		/
 *
 ******************************************************************************************************/


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Class to represent a drawing canvas. Aims to work like a "TCanvas" in Delphi
 * @author Bob Fisch
 */
public class Canvas  {
	
	protected Graphics2D canvas = null;
	private int x;
	private int y;
	// START KGU#906 2021-01-02: Enh. #905
	private int flags = 0;
	// END KGU#906 2021-01-02
	
	/**
	 * Creates a new Canvas based on the provided {@link Graphics2D} object
	 * @param _canvas - the underlying {@code java.awt.Graphics2D} object
	 */
	public Canvas(Graphics2D _canvas)
	{
		canvas = _canvas;
		if(canvas != null) {
			canvas.setRenderingHint(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON
					);
		}
	}
	
	// START KGU#906 2021-01-02: Enh. #905
	/**
	 * Sets the flag identical to the given binary flag combination {@code _flags}
	 * @param _flags - a binary combination of custom flags
	 * @see #setFlag(int)
	 */
	public void setFlags(int _flags)
	{
		flags = _flags;
	}
	
	/**
	 * Sets the custom flag with the given number (0 .. 31) in addition to the
	 * currently held flags
	 * @param _flagNo - number of the custom flag to be switched on
	 * @see #setFlags(int)
	 */
	public void setFlag(int _flagNo)
	{
		flags |= 1 << _flagNo;
	}
	
	public boolean isSetFlag(int _flagNo)
	{
		return (flags & (1 << _flagNo)) != 0;
	}
	// END KGU#906 2021-01-02
	
	/**
	 * Draws the given image {@code _img} a position {@code (_x, _y)}.
	 * @param _img - the {@link Image} to be drawn
	 * @param _x - left x coordinate
	 * @param _y - upper y coordinate
	 */
	public void draw(Image _img, int _x, int _y)
	{
		canvas.drawImage(_img,_x,_y,null);
	}
	
	/**
	 * Retrieves the {@link FontMetrics} for the given {@link Font} {@code _font}
	 * @param _font - the interesting font
	 * @return the associated font metrics
	 */
	public FontMetrics getFontMetrics(Font _font)
	{
		return canvas.getFontMetrics(_font);
	}
	
	/**
	 * Forecasts the width of the given string {@code _string} on drawing with the
	 * current font rendering context
	 * @param _string - the string to be measured
	 * @return the integral width of the drawn string in pixels
	 */
	public int stringWidth(String _string)
	{
		Rectangle2D bounds = canvas.getFont().getStringBounds(_string, canvas.getFontRenderContext());
		return Double.valueOf(bounds.getWidth()).intValue();
	}

	/**
	 * Forecasts the height of the given string {@code _string} on drawing with the
	 * current font rendering context
	 * @param _string - the string to be measured
	 * @return the integral height of the drawn string in pixels
	 */
	public int stringHeight(String _string)
	{
		Rectangle2D bounds = canvas.getFont().getStringBounds(_string,canvas.getFontRenderContext());
		return Double.valueOf(bounds.getHeight()).intValue();
	}
	
	/**
	 * @return the current {@link Font}
	 */
	public Font getFont()
	{
		return canvas.getFont();
	}
	
	/**
	 * Sets the given {@link Font} {@code _font} as new font
	 * @param _font - the new font to be used
	 */
	public void setFont(Font _font)
	{
		canvas.setFont(_font);
	}
	
	// START KGU#906 2021-01-02: Enh. #905
	/**
	 * @return the current pen color
	 * @see #setColor(Color)
	 */
	public Color getColor()
	{
		return canvas.getColor();
	}
	// END KGU#906 2021-01-02
	
	/**
	 * Sets the given {@link Color} {@code _color} as new pen color
	 * @param _color - the color to be used
	 * @see #getColor()
	 * @see #setBackground(Color)
	 */
	public void setColor(Color _color)
	{
		canvas.setColor(_color);
	}
	
	/**
	 * Sets the given {@link Color} {@code _color} as new background color
	 * @param _color - the background color to be used
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	public void setBackground(Color _color)
	{
		canvas.setBackground(_color);
	}
	
	/**
	 * Draws the rectangle {@code _rect} given as {@link Rect} 
	 * @param _rect - the rectangle to be drawn
	 * @see #fillRect(Rect)
	 * @see #roundRect(Rect, int)
	 */
	public void drawRect(Rect _rect)
	{
		canvas.drawRect(_rect.left, _rect.top, _rect.right-_rect.left, _rect.bottom-_rect.top);
	}	
	
	/**
	 * Draws the rectangle {@code _rect} given as {@link Rect} with rounded corners
	 * @param _rect - the rectangle to be drawn
	 * @param _cornerRadius - the radius of the corner arcs
	 * @see #drawRect(Rect)
	 * @see #drawPoly(Polygon)
	 * @see #fillRoundRect(Rect, int)
	 */
	public void roundRect(Rect _rect, int _cornerRadius)
	{
		canvas.drawRoundRect(_rect.left, _rect.top, _rect.right-_rect.left, _rect.bottom-_rect.top, 2*_cornerRadius, 2*_cornerRadius);
	}	
	
	// START KGU#376 2017-05-16: Enh. #389
	/**
	 * Draws the given {@link Polygon} {@code _poly}
	 * @param _poly - the polygon to be drawn
	 * @see #fillPoly(Polygon)
	 * @see #drawRect(Rect)
	 */
	public void drawPoly(Polygon _poly)
	{
		canvas.drawPolygon(_poly);
	}
	// END KGU#376 2017-05-16
	
	/**
	 * Fills the given {@link Rect} {@code _rect} with the current pen color
	 * @param _rect - the rectangle to be filled
	 * @see #drawRect(Rect)
	 * @see #hatchRect(Rect, int, int)
	 */
	public void fillRect(Rect _rect)
	{
		canvas.fillRect(_rect.left, _rect.top, _rect.right-_rect.left, _rect.bottom-_rect.top);
	}
	
	// Start KGU#277 2016-10-13: Enh. #270
	/**
	 * Fills the given {@link Rect} {@code _rect} with a dark gray hatch pattern
	 * with a line gradient {@code _deltaY/_deltaX}
	 * @param _rect - the rectangle to be hatched
	 * @param _deltaX - width of the hatch line cell
	 * @param _deltaY - height of the hatch line cell
	 * @see #hatchRect(Rect, int, int, Color)
	 * @see #fillRect(Rect)
	 * @see #drawRect(Rect)
	 */
	public void hatchRect(Rect _rect, int _deltaX, int _deltaY)
	{
		hatchRect(_rect, _deltaX, _deltaY, Color.DARK_GRAY);
	}
	
	/**
	 * Fills the given {@link Rect} {@code _rect} with a hatch pattern of color
	 * {@code _color} with a line gradient {@code _deltaY/_deltaX}.
	 * @param _rect - the rectangle to be hatched
	 * @param _deltaX - width of the hatch line cell
	 * @param _deltaY - height of the hatch line cell
	 * @param _color - the pattern color to be used
	 * @see #hatchRect(Rect, int, int)
	 * @see #fillRect(Rect)
	 * @see #drawRect(Rect)
	 */
	public void hatchRect(Rect _rect, int _deltaX, int _deltaY, Color _color)
	{
		BufferedImage bufferedImage =
		        new BufferedImage(_deltaX, _deltaY, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = bufferedImage.createGraphics();
		g2.setColor(_color);
		g2.drawLine(_deltaX, 0, 0, _deltaY);

		// paint with the texturing brush
		Paint oldPaint = canvas.getPaint();
		canvas.setPaint(new java.awt.TexturePaint(bufferedImage, new Rectangle(0,0,_deltaX,_deltaY)));
		canvas.fill(_rect.getRectangle());
		canvas.setPaint(oldPaint);
	}
	// END KGU#277 2016-10-13
	
	// START KGU#221 2016-07-27: Enhancement for bugfix #208, KGU 2017-05-16: Signature changed
	/**
	 * Draws a filled area with the shape of a rectangle with rounded corners
	 * @param _rect - the base rectangle to be filled with th current pen color
	 * @param _cornerRadius - the radius of the corner arcs
	 * @see #roundRect(Rect, int)
	 */
	public void fillRoundRect(Rect _rect, int _cornerRadius)
	{
		canvas.fillRoundRect(_rect.left, _rect.top, _rect.right-_rect.left, _rect.bottom-_rect.top, 2*_cornerRadius, 2*_cornerRadius);
	}
	// END KGU#221 2016-07-27

	// START KGU#357 2017-06-16: Enhancement for issue #389
	/**
	 * Fills the given {@link Polygon} {@code _poly} with the current pen color
	 * @param _poly - the polygon to be filled
	 * @see #drawPoly(Polygon)
	 */
	public void fillPoly(Polygon _poly)
	{
		canvas.fillPolygon(_poly);
	}
	// END KGU#357 2017-06-16

	/**
	 * Draws the given string {@code _text} horizontally with the current font
	 * and color, starting at position {@code (_x, _y)}
	 * @param _x
	 * @param _y
	 * @param _text
	 */
	public void writeOut(int _x, int _y, String _text)
	{
		String display = new String(_text);

		// START KGU#377 2017-03-30: Bugfix - already done in better quality
//		display = BString.replace(display, "<--","<-");
//		display = BString.replace(display, "<-","\u2190");
		// END KGU#377 2017-03-30
		canvas.drawString(display, _x, _y);
	}
	
	/**
	 * Sets the start coordinate for a following {@link #lineTo(int, int)}
	 * @param _x
	 * @param _y
	 */
	public void moveTo(int _x, int _y)
	{
		x = _x;
		y = _y;
	}
	
	/**
	 * Draws a line from the current coordinate (to be set with {@link #moveTo(int, int)}
	 * before the first use of this method!)
	 * @param _x - horizontal coordinate of the line end point
	 * @param _y - vertical coordinate of the line end point
	 * @see #moveTo(int, int)
	 */
	public void lineTo(int _x, int _y)
	{
		canvas.drawLine(x, y, _x, _y);
		moveTo(_x, _y);
	}
	
	// START KGU#401 2017-05-18: Issue #405
	/**
	 * Rotates the canvas by 90 degrees around {@code (_xRot, _yRot)} in order to
	 * draw something counter-clock-wise rotated
	 * @param _xRot - the rotation center (X value)
	 * @param _yRot - the rotation center (Y value)
	 * @return the former transform (allowing to restore it after the drawings)
	 * @see #setTransform(AffineTransform)
	 */
	public AffineTransform rotateLeftAround(int _xRot, int _yRot)
	{
		AffineTransform oldTransf = canvas.getTransform();
		canvas.rotate(-Math.PI/2.0, _xRot, _yRot);
		return oldTransf;
	}
	
	/**
	 * Allows restoring a transform obtained by {@link #rotateLeftAround(int, int)} 
	 * @param _transform - an {@code AffineTransform} previously cached
	 * @see #rotateLeftAround(int, int)
	 */
	public void setTransform(AffineTransform _transform)
	{
		canvas.setTransform(_transform);
	}
	// END KGU#401 2017-05-18
	
}
