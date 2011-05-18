/*==========================================================================*\
 |  $Id$
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
 *  Annotation used to provide explicit hint text for a test case method
 *  or class.
 *
 *  @deprecated Use
 *  {@link student.testingsupport.annotations.Hint} instead.
 *  @author Stephen Edwards
 *  @author  Last changed by $Author$
 *  @version $Revision$, $Date$
 */
@Deprecated
@Documented
@Inherited
@Retention( RetentionPolicy.RUNTIME )
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Hint
{
    /** The hint text to use. */
    String value();
}
