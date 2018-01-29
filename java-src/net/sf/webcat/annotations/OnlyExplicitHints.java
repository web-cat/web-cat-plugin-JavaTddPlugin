/*==========================================================================*\
 |  $Id: OnlyExplicitHints.java,v 1.2 2011/05/18 17:40:07 stedwar2 Exp $
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2006-2011 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU General Public License as published by
 |  the Free Software Foundation; either version 2 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU General Public License
 |  along with Web-CAT; if not, write to the Free Software
 |  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 |
 |  Project manager: Stephen Edwards <edwards@cs.vt.edu>
 |  Virginia Tech CS Dept, 114 McBryde Hall (0106), Blacksburg, VA 24061 USA
\*==========================================================================*/

package net.sf.webcat.annotations;

import java.lang.annotation.*;

//-------------------------------------------------------------------------
/**
 *  Marker annotation to indicate that a class or method does not use the
 *  default hinting mechanism and only uses explicitly applied hints.
 *
 *  @deprecated Use
 *  {@link student.testingsupport.annotations.OnlyExplicitHints} instead.
 *  @author Stephen Edwards
 *  @author  Last changed by $Author: stedwar2 $
 *  @version $Revision: 1.2 $, $Date: 2011/05/18 17:40:07 $
 */
@Deprecated
@Documented
@Inherited
@Retention( RetentionPolicy.RUNTIME )
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface OnlyExplicitHints
{
    // Marker annotations have no fields
}
