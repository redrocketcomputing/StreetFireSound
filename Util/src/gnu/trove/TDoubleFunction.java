///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////

package gnu.trove;

/**
 * Interface for functions that accept and return one double primitive.
 *
 * Created: Mon Nov  5 22:19:36 2001
 *
 * @author Eric D. Friedman
 * @version $Id: TDoubleFunction.java,v 1.1 2005/02/22 03:54:48 stephen Exp $
 */

public interface TDoubleFunction {
    /**
     * Execute this function with <tt>value</tt>
     *
     * @param value an <code>double</code> input
     * @return an <code>double</code> result
     */
    public double execute(double value);
}// TDoubleFunction