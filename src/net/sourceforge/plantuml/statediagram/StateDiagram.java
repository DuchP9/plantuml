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
 * Contribution   :  Serge Wenger 
 *
 */
package net.sourceforge.plantuml.statediagram;

import java.util.Map;
import java.util.Objects;

import net.sourceforge.plantuml.UmlDiagramType;
import net.sourceforge.plantuml.baraye.EntityUtils;
import net.sourceforge.plantuml.baraye.IEntity;
import net.sourceforge.plantuml.baraye.IGroup;
import net.sourceforge.plantuml.classdiagram.AbstractEntityDiagram;
import net.sourceforge.plantuml.core.UmlSource;
import net.sourceforge.plantuml.cucadiagram.Code;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.GroupType;
import net.sourceforge.plantuml.cucadiagram.Ident;
import net.sourceforge.plantuml.cucadiagram.LeafType;
import net.sourceforge.plantuml.cucadiagram.Link;
import net.sourceforge.plantuml.cucadiagram.NamespaceStrategy;
import net.sourceforge.plantuml.graphic.USymbol;

public class StateDiagram extends AbstractEntityDiagram {

	private static final String CONCURRENT_PREFIX = "CONC";

	public StateDiagram(UmlSource source, Map<String, String> skinParam) {
		super(source, UmlDiagramType.STATE, skinParam);
		// setNamespaceSeparator(null);
	}

	public boolean checkConcurrentStateOk(Ident ident, Code code) {
		final boolean result = checkConcurrentStateOkInternal(code);
		// System.err.println("checkConcurrentStateOk " + code + " " + ident + " " +
		// result);
		return result;
	}

	private boolean checkConcurrentStateOkInternal(Code code) {
		if (leafExist(code) == false) {
			return true;
		}
		final IEntity existing = this.getLeaf(code);
		if (getCurrentGroup().getGroupType() == GroupType.CONCURRENT_STATE
				&& getCurrentGroup() != existing.getParentContainer()) {
			return false;
		}
		if (existing.getParentContainer().getGroupType() == GroupType.CONCURRENT_STATE
				&& getCurrentGroup() != existing.getParentContainer()) {
			return false;
		}
		return true;
	}

	@Override
	public IEntity getOrCreateLeaf(Ident ident, Code code, LeafType type, USymbol symbol) {
		if (checkConcurrentStateOk(Objects.requireNonNull(ident), code) == false) {
			throw new IllegalStateException("Concurrent State " + code);
		}
		if (type == null) {
			if (code.getName().startsWith("[*]"))
				throw new IllegalArgumentException();

			if (isGroup(code))
				return getGroup(code);

			return getOrCreateLeafDefault(ident, code, LeafType.STATE, null);
		}
		return getOrCreateLeafDefault(ident, code, type, symbol);
	}

	public IEntity getStart() {
		final IGroup g = getCurrentGroup();
		if (EntityUtils.groupRoot(g)) {
			final Ident ident = buildLeafIdent("*start");
			final Code code = buildCode("*start");
			return getOrCreateLeaf(ident, code, LeafType.CIRCLE_START, null);
		}
		final String idShort = "*start*" + g.getCodeGetName();
		final Ident ident = buildLeafIdent(idShort);
		final Code code = buildCode(idShort);
		return getOrCreateLeaf(ident, code, LeafType.CIRCLE_START, null);
	}

	public IEntity getEnd() {
		final IGroup p = getCurrentGroup();
		if (EntityUtils.groupRoot(p)) {
			final Ident ident = buildLeafIdent("*end");
			final Code code = buildCode("*end");
			return getOrCreateLeaf(ident, code, LeafType.CIRCLE_END, null);
		}
		final String idShort = "*end*" + p.getCodeGetName();
		final Ident ident = buildLeafIdent(idShort);
		final Code code = buildCode(idShort);
		return getOrCreateLeaf(ident, code, LeafType.CIRCLE_END, null);
	}

	public IEntity getHistorical() {
		final IGroup g = getCurrentGroup();
		if (EntityUtils.groupRoot(g)) {
			final Ident ident = buildLeafIdent("*historical");
			final Code code = buildCode("*historical");
			return getOrCreateLeaf(ident, code, LeafType.PSEUDO_STATE, null);
		}
		final String idShort = "*historical*" + g.getCodeGetName();
		final Ident ident = buildLeafIdent(idShort);
		final Code code = buildCode(idShort);
		return getOrCreateLeaf(ident, code, LeafType.PSEUDO_STATE, null);
	}

	public IEntity getHistorical(String idShort) {
		final Ident idNewLong = buildLeafIdent(idShort);
		final Code codeGroup = buildCode(idShort);
		gotoGroup(idNewLong, codeGroup, Display.getWithNewlines(codeGroup), GroupType.STATE, getRootGroup(),
				NamespaceStrategy.SINGLE);
		final IEntity g = getCurrentGroup();
		final String tmp = "*historical*" + g.getCodeGetName();
		final Ident ident = buildLeafIdent(tmp);
		final Code code = buildCode(tmp);
		final IEntity result = getOrCreateLeaf(ident, code, LeafType.PSEUDO_STATE, null);
		endGroup();
		return result;
	}

	public IEntity getDeepHistory() {
		final IGroup g = getCurrentGroup();
		if (EntityUtils.groupRoot(g)) {
			final Ident ident = buildLeafIdent("*deephistory");
			final Code code = buildCode("*deephistory");
			return getOrCreateLeaf(ident, code, LeafType.DEEP_HISTORY, null);
		}

		final String idShort = "*deephistory*" + g.getCodeGetName();
		final Ident ident = buildLeafIdent(idShort);
		final Code code = buildCode(idShort);
		return getOrCreateLeaf(ident, code, LeafType.DEEP_HISTORY, null);
	}

	public IEntity getDeepHistory(String idShort) {
		final Ident idNewLong = buildLeafIdent(idShort);
		final Code codeGroup = buildCode(idShort);
		gotoGroup(idNewLong, codeGroup, Display.getWithNewlines(codeGroup), GroupType.STATE, getRootGroup(),
				NamespaceStrategy.SINGLE);
		final IEntity g = getCurrentGroup();
		final String tmp = "*deephistory*" + g.getCodeGetName();
		final Ident ident = buildLeafIdent(tmp);
		final Code code = buildCode(tmp);
		final IEntity result = getOrCreateLeaf(ident, code, LeafType.DEEP_HISTORY, null);
		endGroup();
		return result;
	}

	public boolean concurrentState(char direction) {
		final IGroup cur = getCurrentGroup();
		// printlink("BEFORE");
		if (EntityUtils.groupRoot(cur) == false && cur.getGroupType() == GroupType.CONCURRENT_STATE) {
			super.endGroup();
		}
		getCurrentGroup().setConcurrentSeparator(direction);
		final String tmp1 = this.getUniqueSequence(CONCURRENT_PREFIX);
		final Ident ident1 = buildLeafIdent(tmp1);
		final Code code1 = buildCode(tmp1);
		gotoGroup(ident1, code1, Display.create(""), GroupType.CONCURRENT_STATE, getCurrentGroup(),
				NamespaceStrategy.SINGLE);
		final IGroup conc1 = getCurrentGroup();
		if (EntityUtils.groupRoot(cur) == false && cur.getGroupType() == GroupType.STATE) {
			cur.moveEntitiesTo(conc1);
			super.endGroup();
			final String tmp2 = this.getUniqueSequence(CONCURRENT_PREFIX);
			final Ident ident2 = buildLeafIdent(tmp2);
			final Code code2 = buildCode(tmp2);
			gotoGroup(ident2, code2, Display.create(""), GroupType.CONCURRENT_STATE, getCurrentGroup(),
					NamespaceStrategy.SINGLE);
		}
		// printlink("AFTER");
		return true;
	}

	// private void printlink(String comment) {
	// Log.println("COMMENT="+comment);
	// for (Link l : getLinks()) {
	// Log.println(l);
	// }
	// }

	@Override
	public boolean endGroup() {
		final IGroup cur = getCurrentGroup();
		if (EntityUtils.groupRoot(cur) == false && cur.getGroupType() == GroupType.CONCURRENT_STATE)
			super.endGroup();

		return super.endGroup();
	}

	private boolean hideEmptyDescription = false;

	@Override
	public final void setHideEmptyDescription(boolean hideEmptyDescription) {
		this.hideEmptyDescription = hideEmptyDescription;
	}

	public final boolean isHideEmptyDescriptionForState() {
		return hideEmptyDescription;
	}

	@Override
	public String checkFinalError() {
		for (Link link : this.getLinks()) {
			final IGroup parent1 = getGroupParentIfItIsConcurrentState(link.getEntity1());
			final IGroup parent2 = getGroupParentIfItIsConcurrentState(link.getEntity2());
			if (isCompatible(parent1, parent2) == false) {
				return "State within concurrent state cannot be linked out of this concurrent state (between "
						+ link.getEntity1().getCodeGetName() + " and " + link.getEntity2().getCodeGetName() + ")";
			}
		}
		return super.checkFinalError();
	}

	private static boolean isCompatible(IGroup parent1, IGroup parent2) {
		if (parent1 == null && parent2 == null) {
			return true;
		}
		if (parent1 != null ^ parent2 != null) {
			return false;
		}
		assert parent1 != null && parent2 != null;
		return parent1 == parent2;
	}

	private static IGroup getGroupParentIfItIsConcurrentState(IEntity ent) {
		IGroup parent = ent.getParentContainer();
		while (parent != null) {
			if (parent.getGroupType() == GroupType.CONCURRENT_STATE) {
				return parent;
			}
			parent = parent.getParentContainer();
		}
		return null;

	}

}
