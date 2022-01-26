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

/**
 * Allgemeine Umsetzung einer FIFO-Struktur zur Datenspeicherung.
 *
 * @param <typ>
 *            Klasse die im Fifo verwaltet werden soll.
 */

public interface FiFo<typ> {

	/**
	 * F�gt ein Element zum Fifo hinzu.
	 *
	 * @param element
	 *            neues Element
	 *
	 * @throws FullException
	 */

	public void add(typ element) throws FullException;

	/**
	 * Entfernt das zeitlich �lteste Element aus dem FIFO.
	 *
	 * @return zeitlich �lteste Element
	 *
	 * @throws EmptyException
	 *             Das FIFO ware leer.
	 */

	public typ remove() throws EmptyException;

	/**
	 * Gibt "true" zur�ck, wenn das FIFO voll ist.
	 *
	 * @return Ist FIFO voll?
	 */

	public boolean isFull();

	/**
	 * Gibt "true" zur�ck, wenn das FIFO leer ist.
	 *
	 * @return Ist FIFO leer?
	 */

	public boolean isEmpty();

	/** Klasse f�r die Erzeugung einer Exception, wenn FIFO voll ist. */

	public static class FullException extends Exception {
	}

	/** Klasse f�r die Erzeugung einer Exception, wenn FIFO leer ist. */

	public static class EmptyException extends Exception {
	}

}
