/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.mailer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/****************************************************************************
 *
 * MailAuthenticator - a password authentication class used to access a mail
 *                     server that needs authentication
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 ***************************************************************************/
public class MailAuthenticator extends Authenticator {
    /**
     * The users username used for basic authentication
     */
    protected String m_user = null;
    /**
     * The users password used for basic authentication
     */
    protected String m_password = null;

    /**
     * constructor calls the super class constructor
     *
     */
    public MailAuthenticator(String user, String password) {
		super();
		m_user = user;
		m_password = password;
    }

    /**
     * constructor calls the super class constructor
     *
     */
    public MailAuthenticator() {
	super();
    }

    /**
     * overrides the getPasswordAuthentication method in the super class
     * it creates a dialog box to collect the users name and password for a
     * connection to the mail server
     *
     * @return - PasswordAuthentication object containing the users name and
     *            password
     *
     */
    protected PasswordAuthentication getPasswordAuthentication() {

	if (m_user != null && m_password != null) {
		return new PasswordAuthentication(m_user, m_password);
	}

	final Dialog dialog = new Dialog(new Frame("Login"),
				     "Please Login to the mail server", true);
	dialog.setBounds(200, 200, 200, 100);
	dialog.setLayout(new GridLayout (0, 1));
	Label label = new Label(getRequestingPrompt());
	dialog.add (label);
	TextField username = new TextField();
	username.setBackground (Color.white);
	dialog.add (username);
	TextField password = new TextField();
	password.setEchoChar ('*');
	password.setBackground (Color.white);
	dialog.add (password);
	Button button = new Button ("OK");
	dialog.add (button);
	button.addActionListener (new ActionListener() {
		public void actionPerformed (ActionEvent e) {
		    dialog.dispose();
		}
	    });
	dialog.pack();
	dialog.setVisible(true);
	return new PasswordAuthentication(username.getText(),
					          password.getText());
    }
}
