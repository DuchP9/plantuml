/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2023, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 *
 * If you like this project or if you find it useful, you can support us at:
 *
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 *
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 */
package net.sourceforge.plantuml.tim;

import net.sourceforge.plantuml.utils.LineLocation;
import net.sourceforge.plantuml.utils.StringLocated;

public class EaterDeclareReturnFunction extends Eater {

	private TFunctionImpl function;
	private final LineLocation location;
	private boolean finalFlag;

	public EaterDeclareReturnFunction(StringLocated s) {
		super(s.getTrimmed());
		this.location = s.getLocation();
	}

	@Override
	public void analyze(TContext context, TMemory memory) throws EaterException, EaterExceptionLocated {
		skipSpaces();
		checkAndEatChar("!");
		boolean unquoted = false;
		while (peekUnquoted() || peekFinal()) {
			if (peekUnquoted()) {
				checkAndEatChar("unquoted");
				skipSpaces();
				unquoted = true;
			} else if (peekFinal()) {
				checkAndEatChar("final");
				skipSpaces();
				finalFlag = true;
			}
		}
		checkAndEatChar("function");
		skipSpaces();
		function = eatDeclareReturnFunctionWithOptionalReturn(context, memory, unquoted, location);
	}

	private boolean peekUnquoted() {
		return peekChar() == 'u';
	}

	private boolean peekFinal() {
		return peekChar() == 'f' && peekCharN2() == 'i';
	}

	public TFunctionImpl getFunction() {
		return function;
	}

	public final boolean getFinalFlag() {
		return finalFlag;
	}

}
