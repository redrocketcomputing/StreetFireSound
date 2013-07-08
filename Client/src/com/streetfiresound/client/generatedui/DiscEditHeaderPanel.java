/*
 * Copyright (C) 2004 by StreetFire Sound Labs
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id $
 */
package com.streetfiresound.client.generatedui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import com.streetfiresound.client.UISettings;
import com.streetfiresound.client.DiscEditPanel;
import com.streetfiresound.client.StreetFirePanel;
import javax.swing.SwingConstants;

/**
 */
public class DiscEditHeaderPanel extends StreetFirePanel
{
	private JPanel jPanel = null;
	private JTextField Artist = null;
	private JLabel jLabel = null;
	private JPanel jPanel1 = null;  //  @jve:decl-index=0:visual-constraint="253,244"
	private JLabel jLabel1 = null;  //  @jve:decl-index=0:visual-constraint="262,244"
	private JTextField Title = null;
	private JPanel jPanel2 = null;
	private JLabel jLabel2 = null;
	private JTextField Genre = null;
	private JPanel jPanel4 = null;
	/**
	 * This is the default constructor
	 */
	public DiscEditHeaderPanel() {
		//super(new BorderLayout());
		initialize();
	}
	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private  void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(300,200);
		this.add(getJPanel4(), java.awt.BorderLayout.CENTER);
	}
	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jLabel = new JLabel();
			jPanel = new StreetFirePanel();
			jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
			jLabel.setText("Artist ");
      jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel.setPreferredSize(new java.awt.Dimension(DiscEditPanel.COL1_WIDTH, 1));
			jPanel.setBorder(UISettings.INSET_PANEL_BORDER);
			jPanel.add(jLabel, null);
			jPanel.add(getArtist(), null);
		}
		return jPanel;
	}
	/**
	 * This method initializes jTextField
	 *
	 * @return javax.swing.JTextField
	 */
	public JTextField getArtist() {
		if (Artist == null) {
			Artist = new JTextField();
		}
		return Artist;
	}
	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new StreetFirePanel();
			jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.X_AXIS));
			jPanel1.add(getJLabel1(), null);
			jPanel1.add(getTitle(), null);
			jPanel1.setBorder(UISettings.INSET_PANEL_BORDER);
		}
		return jPanel1;
	}
	/**
	 * This method initializes jLabel1
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("Title ");
      jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel1.setPreferredSize(new java.awt.Dimension(DiscEditPanel.COL1_WIDTH, 1));
		}
		return jLabel1;
	}
	/**
	 * This method initializes jTextField1
	 *
	 * @return javax.swing.JTextField
	 */
	public JTextField getTitle() {
		if (Title == null) {
			Title = new JTextField();
		}
		return Title;
	}
	/**
	 * This method initializes jPanel2
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jLabel2 = new JLabel();
			jPanel2 = new StreetFirePanel();
			jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.X_AXIS));
			jLabel2.setText("Genre ");
      jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel2.setPreferredSize(new java.awt.Dimension(DiscEditPanel.COL1_WIDTH, 1));
			jPanel2.add(jLabel2, null);
			jPanel2.add(getGenre(), null);
			jPanel2.setBorder(UISettings.INSET_PANEL_BORDER);
		}
		return jPanel2;
	}
	/**
	 * This method initializes jTextField2
	 *
	 * @return javax.swing.JTextField
	 */
	public JTextField getGenre() {
		if (Genre == null) {
			Genre = new JTextField();
		}
		return Genre;
	}

	/**
	 * This method initializes jPanel4
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel4() {
		if (jPanel4 == null) {
			jPanel4 = new StreetFirePanel();
			jPanel4.setLayout(new BoxLayout(jPanel4, BoxLayout.Y_AXIS));
			jPanel4.add(getJPanel1(), null);
			jPanel4.add(getJPanel(), null);
			jPanel4.add(getJPanel2(), null);
		}
		return jPanel4;
	}
         }  //  @jve:decl-index=0:visual-constraint="17,4"
