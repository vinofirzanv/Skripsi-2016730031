/*
 * Copyright (c) 2011., Virtenio GmbH
 * All rights reserved.
 *
 * Commercial software license.
 * Only for test and evaluation purposes.
 * Use in commercial products prohibited.
 * No distribution without permission by Virtenio.
 * Ask Virtenio for other type of license at info@virtenio.de
 *
 * Kommerzielle Softwarelizenz.
 * Nur zum Test und Evaluierung zu verwenden.
 * Der Einsatz in kommerziellen Produkten ist verboten.
 * Ein Vertrieb oder eine Ver�ffentlichung in jeglicher Form ist nicht ohne Zustimmung von Virtenio erlaubt.
 * F�r andere Formen der Lizenz nehmen Sie bitte Kontakt mit info@virtenio.de auf.
 */

package com.virtenio.preon32.examples.common.utils;

/** Datenspeicher, der seine Informationen zyklisch ausgibt. */

public class SourceCyclic {

	private final int[] values;
	private int blockP;

	/**
	 * Datenspeicher, der seine Informationen zyklisch ausgibt.
	 *
	 * @param values
	 *            Die Zahlenwerte, die zyklisch ausgegeben werden sollen.
	 */
	public SourceCyclic(int[] values) {
		this.values = values;
		this.blockP = -1;
	}

	/**
	 * Fordert die n�chste Information des Datenspeichers an.
	 *
	 * @return N�chste Information im Speicher
	 */
	public int nextValue() {
		blockP = (blockP + 1) % values.length;
		return values[blockP];
	}

	/**
	 * Gibt die Gr��e des Datenspeichers an.
	 *
	 * @return Gr��e des Datenspeichers
	 */
	public int getSize() {
		return values.length;
	}

}
