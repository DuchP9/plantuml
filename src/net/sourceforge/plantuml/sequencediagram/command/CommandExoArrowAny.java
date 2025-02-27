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
 *
 */
package net.sourceforge.plantuml.sequencediagram.command;

import net.sourceforge.plantuml.StringUtils;
import net.sourceforge.plantuml.Url;
import net.sourceforge.plantuml.UrlBuilder;
import net.sourceforge.plantuml.UrlMode;
import net.sourceforge.plantuml.command.CommandExecutionResult;
import net.sourceforge.plantuml.command.SingleLineCommand2;
import net.sourceforge.plantuml.command.regex.IRegex;
import net.sourceforge.plantuml.command.regex.RegexResult;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.sequencediagram.LifeEventType;
import net.sourceforge.plantuml.sequencediagram.MessageExo;
import net.sourceforge.plantuml.sequencediagram.MessageExoType;
import net.sourceforge.plantuml.sequencediagram.Participant;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;
import net.sourceforge.plantuml.skin.ArrowBody;
import net.sourceforge.plantuml.skin.ArrowConfiguration;
import net.sourceforge.plantuml.skin.ArrowDecoration;
import net.sourceforge.plantuml.skin.ArrowHead;
import net.sourceforge.plantuml.skin.ArrowPart;
import net.sourceforge.plantuml.ugraphic.color.HColor;
import net.sourceforge.plantuml.ugraphic.color.NoSuchColorException;
import net.sourceforge.plantuml.utils.LineLocation;

abstract class CommandExoArrowAny extends SingleLineCommand2<SequenceDiagram> {

	protected static final String ARROW_SUPPCIRCLE1 = "ARROW_SUPPCIRCLE1";
	protected static final String ARROW_SUPPCIRCLE2 = "ARROW_SUPPCIRCLE2";

	public CommandExoArrowAny(IRegex pattern) {
		super(pattern);
	}

	@Override
	final protected CommandExecutionResult executeArg(SequenceDiagram diagram, LineLocation location, RegexResult arg)
			throws NoSuchColorException {
		final String body = arg.getLazzy("ARROW_BODYA", 0) + arg.getLazzy("ARROW_BODYB", 0);
		final String dressing = arg.getLazzy("ARROW_DRESSING", 0);
		final Participant p = diagram.getOrCreateParticipant(
				StringUtils.eventuallyRemoveStartingAndEndingDoubleQuote(arg.get("PARTICIPANT", 0)));

		final boolean sync = dressing.length() == 2;
		final boolean dotted = body.contains("--");

		final Display labels;
		if (arg.get("LABEL", 0) == null)
			labels = Display.create("");
		else
			labels = Display.getWithNewlines(arg.get("LABEL", 0));

		final boolean bothDirection = arg.get("ARROW_BOTHDRESSING", 0) != null;

		ArrowConfiguration config = bothDirection ? ArrowConfiguration.withDirectionBoth()
				: ArrowConfiguration.withDirectionNormal();
		if (dotted)
			config = config.withBody(ArrowBody.DOTTED);

		if (sync)
			config = config.withHead(ArrowHead.ASYNC);

		final MessageExoType messageExoType = getMessageExoType(arg);

		config = config.withPart(getArrowPart(dressing, messageExoType));
		config = CommandArrow.applyStyle(arg.getLazzy("ARROW_STYLE", 0), config);

		final String activationSpec = arg.get("ACTIVATION", 0);

		if (activationSpec != null && activationSpec.charAt(0) == '*')
			diagram.activate(p, LifeEventType.CREATE, null);

		if (messageExoType == MessageExoType.TO_RIGHT || messageExoType == MessageExoType.TO_LEFT) {
			if (containsSymbol(ARROW_SUPPCIRCLE1, arg, "o"))
				config = config.withDecoration1(ArrowDecoration.CIRCLE);

			if (containsSymbol(ARROW_SUPPCIRCLE1, arg, "x"))
				config = config.withHead1(ArrowHead.CROSSX);

			if (containsSymbol(ARROW_SUPPCIRCLE2, arg, "o"))
				config = config.withDecoration2(ArrowDecoration.CIRCLE);

			if (containsSymbol(ARROW_SUPPCIRCLE2, arg, "x"))
				config = config.withHead2(ArrowHead.CROSSX);

		} else {
			if (containsSymbol(ARROW_SUPPCIRCLE2, arg, "o"))
				config = config.withDecoration1(ArrowDecoration.CIRCLE);

			if (containsSymbol(ARROW_SUPPCIRCLE2, arg, "x"))
				config = config.withHead1(ArrowHead.CROSSX);

			if (containsSymbol(ARROW_SUPPCIRCLE1, arg, "o"))
				config = config.withDecoration2(ArrowDecoration.CIRCLE);

			if (containsSymbol(ARROW_SUPPCIRCLE1, arg, "x"))
				config = config.withHead2(ArrowHead.CROSSX);

		}

		final MessageExo msg = new MessageExo(diagram.getSkinParam().getCurrentStyleBuilder(), p, messageExoType,
				labels, config, diagram.getNextMessageNumber(), isShortArrow(arg));
		if (arg.get("URL", 0) != null) {
			final UrlBuilder urlBuilder = new UrlBuilder(diagram.getSkinParam().getValue("topurl"), UrlMode.STRICT);
			final Url urlLink = urlBuilder.getUrl(arg.get("URL", 0));
			msg.setUrl(urlLink);
		}

		final boolean parallel = arg.get("PARALLEL", 0) != null;
		if (parallel)
			msg.goParallel();

		msg.setAnchor(arg.get("ANCHOR", 1));
		msg.setPart1Anchor(arg.get("PART1ANCHOR", 1));
		msg.setPart2Anchor(arg.get("PART2ANCHOR", 1));

		final CommandExecutionResult status = diagram.addMessage(msg);
		if (status.isOk() == false)
			return status;

		final String s = arg.get("LIFECOLOR", 0);

		final HColor activationColor = s == null ? null
				: diagram.getSkinParam().getIHtmlColorSet().getColor(s);

		if (activationSpec != null) {
			switch (activationSpec.charAt(0)) {
			case '+':
				diagram.activate(p, LifeEventType.ACTIVATE, activationColor);
				break;
			case '-':
				diagram.activate(p, LifeEventType.DEACTIVATE, null);
				break;
			case '!':
				diagram.activate(p, LifeEventType.DESTROY, null);
				break;
			default:
				break;
			}
		} else if (diagram.isAutoactivate()
				&& (config.getHead() == ArrowHead.NORMAL || config.getHead() == ArrowHead.ASYNC)) {
			if (config.isDotted())
				diagram.activate(p, LifeEventType.DEACTIVATE, null);
			else
				diagram.activate(p, LifeEventType.ACTIVATE, activationColor);

		}

		return CommandExecutionResult.ok();
	}

	private ArrowPart getArrowPart(String dressing, MessageExoType messageExoType) {
		if (dressing.contains("/")) {
			if (messageExoType.getDirection() == 1)
				return ArrowPart.BOTTOM_PART;

			return ArrowPart.TOP_PART;
		}
		if (dressing.contains("\\")) {
			if (messageExoType.getDirection() == 1)
				return ArrowPart.TOP_PART;

			return ArrowPart.BOTTOM_PART;
		}
		return ArrowPart.FULL;
	}

	abstract MessageExoType getMessageExoType(RegexResult arg2);

	private boolean isShortArrow(RegexResult arg2) {
		final String s = arg2.get(ARROW_SUPPCIRCLE2, 0);
		if (s != null && s.contains("?"))
			return true;

		return false;
	}

	private boolean containsSymbol(String suppCircle, RegexResult arg2, String symbol) {
		final String s = arg2.get(suppCircle, 0);
		if (s != null && s.contains(symbol))
			return true;

		return false;
	}

}
