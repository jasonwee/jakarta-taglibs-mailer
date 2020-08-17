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

import java.util.ArrayList;
import java.util.Date;
import java.util.ListIterator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

/**
 * SendTag - JSP tag <b>Send</b> is used to send the created email.
 *
 * <tag>
 *        <name>send</name>
 *	  <tagclass>org.apache.taglibs.mailer.SendTag</tagclass>
 *	  <bodycontent>JSP</bodycontent>
 *	  <info>Send the email</info>
 * </tag>
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 */

public class SendTag extends BodyTagSupport {

    ArrayList error = null;  // error messages made accessable to user

    /**
     * implementation of method from the Tag interface that tells the JSP what
     * to do upon encountering the start tag for this tag set
     *
     * @return integer value telling the JSP engine to evaluate the rest of the
     *         jsp page
     *
     * @throws JspException  thrown when error occurs in processing the body of
     *                       this method
     *
     */
    public int doStartTag() throws JspException {

        error = null;
	int i = 0;  // counter for list of extra header name/value pairs
	MimeMessage message;  // message object that contains this message
// Added by Jayson Falkner - 5/8/2001 --------------------------
	MimeMultipart multipart; // multipart for this message
// End of added.
	ListIterator iterate;  // iterator for the list of attachments
	String to = null;  // the to address for this e-mail message
	String cc = null;  // the cc address for this e-mail message
	String bcc = null;  // the bcc address for this e-mail message

	// parent tag must be a MailTag, gives access to methods in parent
	MailTag myparent = (MailTag)findAncestorWithClass(this, MailTag.class);

	if (myparent == null)
	    throw new JspException("send tag not nested within mail tag");

	// get message from parent tag
	message = myparent.getMessage();

	// get the list of name and values for the headers to be added
	ArrayList name = myparent.getHeaderName();
	ArrayList value = myparent.getHeaderValue();

	try {
	    // set extra headers if any
	    if (name.size() > 0)
		for (i = 0; i < name.size(); i++)
		    message.addHeader((String)name.get(i),
					       (String)value.get(i));
	} catch (MessagingException me) {
	    throw new JspException("Header " + name.get(i).toString()
			  + " was not able to be set");
	}

	// get the to address(es)
	if ((to = myparent.getTo()) != null) {

	    try {
		// set the to address for this message
		// catch any errors in the format of the addresses
	        message.setRecipients(Message.RecipientType.TO,
				      InternetAddress.parse(to));
	    } catch (AddressException ae) {
		// get the address that the error occured with
		String ref = ae.getRef();

		// check for more than one address
		if (ref.indexOf(',') != -1) {
		    // position of the start of the error inducing address
		    int pos = ref.substring(0, ae.getPos()).indexOf(',') + 1;
		    // extract the error inducing address
		    ref = ref.substring(pos, ref.indexOf(','));
		}

		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

	        String errorinput = "The to address " + ref + " is not in"
			      + " the proper format.";

		error.add(errorinput);
	    } catch (MessagingException me) {
		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

		// exception occurs when any of the addresses cannot be
		// properly set in the message
                String errorinput = "Messaging Exception: To address/es could"
			      + " not be set in the message." + me.getMessage();

		error.add(errorinput);
	    }
	} else {
	    // if no to address has been given through an error
	    String errorinput = "A to address must be supplied.";

	    // check for existence of error if it does not exist create it
	    if (error == null)
		error = new ArrayList();

	    error.add(errorinput);
	}

	// set the Reply-to address if it hax been supplied
	if (myparent.getReplyTo() != null) {

	    try {
		message.setReplyTo(InternetAddress.parse(myparent.getReplyTo()));
	    } catch (AddressException ae) {
		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

		// exception occurs when the cc address cannot be parsed
		String errorinput = "The Reply-To address was incorrectly set";

		error.add(errorinput);
	    } catch (MessagingException me) {
		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

		// exception occurs when any of the addresses cannot be
		// properly set in the message
                String errorinput = "Messaging Exception: Reply-To address/es"
		        + " could not be set in the message." + me.getMessage();

		error.add(errorinput);
	    }
	}

	try {

	    // get from address from the parent tag
	    String from = myparent.getFrom();

	    // set from address for this message
	    // check for user entered from address
	    if ((from == null) || (from.length() < 2)) {

		// check to see if from is set at the level of the Session
		if (myparent.getSessionObj().getProperty("mail.from") != null)

	            message.setFrom(new InternetAddress(
	    		   myparent.getSessionObj().getProperty("mail.from")));
	    }
	    else
		message.setFrom(new InternetAddress(from));

	} catch (MessagingException me) {
	    // check for existence of error if it does not exist create it
	    if (error == null)
		error = new ArrayList();

	    // add exception to the list of errors in the e-mail
	    String errorinput = "The from address was not set or is not in"
		    + " the proper format for an email address.";

	    error.add(errorinput);
	}

	// check for and set cc addresses
	if ((cc = myparent.getCc()) != null) {

	    try {
                message.setRecipients(Message.RecipientType.CC,
					   InternetAddress.parse(cc));
	    } catch (AddressException ae) {
		// get the address that the error occured with
		String ref = ae.getRef();

		// check for more than one address
		if (ref.indexOf(',') != -1) {
		    // position of the start of the error inducing address
		    int pos = ref.substring(0, ae.getPos()).indexOf(',') + 1;
		    // extract the error inducing address
		    ref = ref.substring(pos, ref.indexOf(','));
		}

		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

		// exception occurs when the to address cannot be parsed
		String errorinput = "The cc address " + ref + " is not in"
			      + " the proper format.";

		error.add(errorinput);
	    } catch (MessagingException me) {
		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

		// exception occurs when any of the addresses cannot be
		// properly set in the message
                String errorinput = "Messaging Exception: Some cc address/es"
			                + " could not be set in the message."
			                + me.getMessage();

		error.add(errorinput);
	    }
	}

	// check for and set bcc addresses
	if ((bcc = myparent.getBcc()) != null) {

	    try {
                message.setRecipients(Message.RecipientType.BCC,
					  InternetAddress.parse(bcc));
	    } catch (AddressException ae) {
		// get the address that the error occured with
		String ref = ae.getRef();

		// check for more than one address
		if (ref.indexOf(',') != -1) {
		    // position of the start of the error inducing address
		    int pos = ref.substring(0, ae.getPos()).indexOf(',') + 1;
		    // extract the error inducing address
		    ref = ref.substring(pos, ref.indexOf(','));
		}

		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

		// exception occurs when the to address cannot be parsed
		String errorinput = "The bcc address " + ref + " is not in"
			      + " the proper format.";

		error.add(errorinput);
	    } catch (MessagingException me) {
		// check for existence of error if it does not exist create it
		if (error == null)
		    error = new ArrayList();

		// exception occurs when any of the addresses cannot be
		// properly set in the message
                String errorinput = "Messaging Exception: Some bcc address/es"
			                + " could not be set in the message."
			                + me.getMessage();

		error.add(errorinput);
	    }
	}

	try {
	    // set the subject in the message
	    message.setSubject(myparent.getSubject());

	    // add the sent date time to the message
	    message.setSentDate(new Date());
	} catch (MessagingException me) {
	    // error occured while adding one of the above to the message
	}

	// check if there are attachments
	if (myparent.getAttachments()) {
	    // create a multipart object and set the message as the first
	    // part then add the attachments
	    multipart = new MimeMultipart();

	    try {

		// create a mimebodypart for the body of the e-mail message
		MimeBodyPart mbp = new MimeBodyPart();

		// set the content in the bodypart
		mbp.setContent(myparent.getBody(), getContentType(myparent));

		// add the message as the first bodypart in the multipart object
		multipart.addBodyPart(mbp);

		// get the list of attachments
		iterate = myparent.getBodyParts().listIterator();

		// loop through the list of attachments and add them to the
		// multipart object
		while (iterate.hasNext()) {
		    multipart.addBodyPart((MimeBodyPart) iterate.next());
		}

		// add the multipart object with the attachments to the message
		message.setContent(multipart);

	    } catch (MessagingException me) {
		// error occured while adding the message to the multipart
		// content
		throw new JspException("An error occured while trying to add" +
				       "the attachments to the e-mail, please"
				       +" to send the e-mail again.");
	    }
	} else {
	    try {
		// set the message with a mimetype according to type set by user
		message.setContent(myparent.getBody(), getContentType(myparent));
	    } catch (MessagingException me) {
		// this error is not very likely to occur
		throw new JspException("The message could not be set in " +
				   "the e-mail, please back up and try again.");
	    }
	}
	// check if errors have occured in creating the message
	if (error != null)
	    // taglibs 1.1
	    return EVAL_BODY_TAG;
	    // taglibs 1.2
	    //return EVAL_BODY_BUFFERED;
	else {
	    // create the thread to mail the messge
	    Mail mail = new Mail(message, pageContext.getServletContext(), to);

	    mail.start();  // send the mail

	    return SKIP_BODY;
	}
    }

    /**
     *  implementation of the method from the tag interface that tells the JSP
     *  page what to do after the body of this tag
     *
     *  @throws JspException  thrown when an error occurs while processing the
     *                        body of this method
     *
     *  @return - int telling the tag handler whether or not to evaluate the
     *            rest of the JSP
     *
     */
    public int doEndTag() throws JspException {

	// an error has occured write error page out
	if (error != null) {
	    //  get error page from body
	    try {
	        if (bodyContent != null)
		    bodyContent.writeOut(bodyContent.getEnclosingWriter());
	    } catch(java.io.IOException e) {
	         throw new JspException("IO Error: " + e.getMessage());
	    }
	    error = null;  // reset error
	}
	return EVAL_PAGE;
    }

    /**
     * get the error message from the final caught JspException
     *
     * @return - Error message telling where problem occured
     *
     */
    public ArrayList getError() {
        return error;
    }

    /**
     * get the content type, possibly including character set
     *
     * @return - complete encoded content type
     */
    public String getContentType(MailTag parent) {
        String type = parent.getType();
        String charset = parent.getCharset();

        if (charset == null) {
            return type;
        }

        return type + ";charset=" + charset;
    }
}

 /**
  *  Thread to actually send the mail.  It could conceivably take some time
  *  for the mail to be sent.  This thread allows for that so that the user will
  *  not be bogged down waiting for the mail to be sent before they can use
  *  their webbrowser again.  Notification will also be sent to the from address
  *  of the message of mail that could not be delivered, and which address it
  *  could not be delivered to.  If the mail does not contain a from address
  *  mail will be sent to user.from property.
  */
class Mail extends Thread {

    private MimeMessage message = null;  // the message to be sent
    // used to get the servlet context for logging
    private jakarta.servlet.ServletContext sc = null;
    String mailto;  // list of to address this message is being sent to

    Mail (MimeMessage mail, jakarta.servlet.ServletContext servletcontext,
	  String to) {
	message = mail;
	sc = servletcontext;
	mailto = to;
    }

    public void run() {

	try {
	    // send the message
	    Transport.send(message);

	} catch (MessagingException me) {
	    // exception occurs when the e-mail cannot be sent to anyone of the
	    // to addresses in the message depending on the mail properties set
	    // this may or may not cause transmission of the message to end.
	    // This error will be logged and a message will be sent to the from
	    // address explaining that the message could not be sent.
	    // Since the JSP has already finished executing this exception will
	    // do nothing visible, however the errors should be dealt with by
	    // the SMTP host if it is configured correctly
	    sc.log("Could not send the e-mail sent to " + mailto + ":  " +
		   me.getMessage());
	}
    }
}
