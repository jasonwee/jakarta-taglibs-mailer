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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimePartDataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

/**
 * MailTag - JSP tag <b>Mail</b> is used to construct an email message.
 *
 * <tag>
 *      <name>mail</name>
 *	<tagclass>org.apache.taglibs.MailTag</tagclass>
 *	<bodycontent>JSP</bodycontent>
 *      <info>Construct an email message</info>
 *
 *      <attribute>
 *     	        <name>to</name>
 *		<required>false</required>
 *      	<rtexprval>false</rtexprval>
 *      </attribute>
 *      <attribute>
 *      	<name>from</name>
 *		<required>false</required>
 *		<rtexprval>false</rtexprval>
 *      </attribute>
 *      <attribute>
 *	       	<name>cc</name>
 *	       	<required>false</required>
 *	       	<rtexprval>false</rtexprval>
 *      </attribute>
 *      <attribute>
 *             	<name>bcc</name>
 *	       	<required>false</required>
 *	       	<rtexprval>false</rtexprval>
 *    	</attribute>
 *     	<attribute>
 *	       	<name>subject</name>
 *	       	<required>false</required>
 *	       	<rtexprval>false</rtexprval>
 *     	</attribute>
 *      <attribute>
 *              <name>user</name>
 *              <required>false</required>
 *              <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 *      <attribute>
 *              <name>password</name>
 *              <required>false</required>
 *              <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 *      <attribute>
 *              <name>server</name>
 *              <required>false</required>
 *              <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 *      <attribute>
 *              <name>port</name>
 *              <required>false</required>
 *              <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 *      <attribute>
 *              <name>session</name>
 *              <required>false</required>
 *              <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 *      <attribute>
 *              <name>mimeMessage</name>
 *              <required>false</required>
 *              <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 *      <attribute>
 *              <name>authenticate</name>
 *              <required>false</required>
 *              <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 * </tag>
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 */

public class MailTag extends BodyTagSupport {

    /**
     * The address type string constants
     */
    protected final static String TO_ADDRESS_STRING = "to";
    protected final static String CC_ADDRESS_STRING = "cc";
    protected final static String BCC_ADDRESS_STRING = "bcc";

    /**
     * The address type int constants
     */
    protected final static int TO_ADDRESS = 1;
    protected final static int CC_ADDRESS = 2;
    protected final static int BCC_ADDRESS = 3;

    /**
     * the address to which the mail is to be sent
     */
    private String to = null;
    private StringBuffer addTO = new StringBuffer();

    /**
     * the address to whom the recipient can reply to
     */
    private String from = null;
    private String reset_from = null;

    /**
     * the carbon copy list addresses that the message will be sent to
     */
    private String cc = null;
    private StringBuffer addCC = new StringBuffer();

    /**
     * the blind carbon copy list of addresses to recieve the message
     */
    private String bcc = null;
    private StringBuffer addBCC = new StringBuffer();

    /**
     * the subject of the message
     */
    private String subject = "";
    private String reset_subject;

    /**
     * the body of the email message
     */
    private String body = null;

    /**
     * or provide the server here for a new session
     */
    private String server = "localhost";
    private String reset_server = null;

    /**
     * provide the server port here for a new session
     */
    private String port = "25";
    private String reset_port = null;

    /**
     * can be "text" or "html" (otherwise text is default)
     */
    private String type = "text/plain";

    /**
     * character set (default is unspecified)
     */
    private String charset = null;

    /**
     * jndi name for Session object
     */
    private String session = null;

    /**
     * jndi named MimePartDataSource object
     */
    private String mimemessage = null;

    /**
     * user to login to smtp server
     */
    private String user = null;

    /**
     * simple text password to authenticate on smtp server
     */
    private String password = null;

    /**
     * The actual session object
     */
    private Session sessionobj = null;

    /**
     * the reply to address
     */
    private String replyto = null;
    private String reset_replyto = null;

    /**
     * list of names extra headers to add
     */
    private ArrayList name = new ArrayList(10);

    /**
     * list of values of extra headers to add
     */
    private ArrayList value = new ArrayList(10);

    /**
     * list of attachments stored as mimebodyparts
     */
    private ArrayList bodyparts = new ArrayList(10);

    /**
     * flag that lets the send tag know if attachments are being sent
     */
    private boolean attachments = false;

    /**
     * flag determines if an error has occured
     */
    private boolean error = false;

    /**
     * flag that determines if the session for sending mail should have an
     * authenticator so that user name and password can be entered if it is
     * required for the mail server
     */
    private boolean authentication = false;

    /**
     * implementation of method from the Tag interface that tells the JSP what
     * to do upon encountering the start tag for this tag set
     *
     * @return EVAL_BODY_TAG - integer value telling the JSP engine to evaluate
     *                         the body of this tag
     *
     * @throws JspException  thrown when error occurs in processing the body of
     *                       this method
     *
     */
    public int doStartTag() throws JspException {
        // Reset dynamic email addresses
        addTO.setLength(0);
        if (to != null) {
            addTO.append(to);
        }
        addCC.setLength(0);
        if (cc != null) {
            addCC.append(cc);
        }
        addBCC.setLength(0);
        if (bcc != null) {
            addBCC.append(bcc);
        }
        reset_from = null;
        reset_replyto = null;
        reset_subject = null;
        reset_server = null;
        reset_port = null;
        name.clear();
        value.clear();
        bodyparts.clear();
        attachments = false;
        sessionobj = null;

	// taglibs 1.1
        return EVAL_BODY_TAG;  // evaluate the body of this tag
	// taglibs 1.2
        // return EVAL_BODY_BUFFERED;  // evaluate the body of this tag
    }

    /**
     * implementation of method from the Tag interface that tells the JSP what
     * to do upon encountering the end tag for this tag set
     *
     * @return - integer value telling the JSP engine whether or not to evaluate
     *                         the body of this tag
     *
     * @throws JspException  thrown when error occurs in processing the body of
     *                       this method
     *
     */
    public int doEndTag() throws JspException {

	// check to see if bodycontent is just blank space
	if (bodyContent.getString().trim().length() == 0)
	    return EVAL_PAGE;
	else {
	    // write out to bodycontent of parent tag
	    try {
		pageContext.getOut().write(bodyContent.getString());
            } catch (IOException ie) {
	        pageContext.getServletContext().log("Mailer taglib: Error tag"
					      + ": unable to write out to jsp.");
	    }
	    return SKIP_PAGE;
	}
    }

    /**
     * get the whole email message
     *
     * @return message - the entire email message
     *
     */
    public MimeMessage getMessage() throws JspException {
        MimeMessage message;
        if (mimemessage != null) {
            try {
                // get initial context from which to lookup messagedatasource
                Context ctx = new InitialContext();

                // create mail message using preexisting jndi named message
                MimePartDataSource mds =
                                 (MimePartDataSource)ctx.lookup(mimemessage);
                sessionobj = mds.getMessageContext().getSession();
                message = new MimeMessage(sessionobj);
            } catch (NamingException ne) {
                throw new JspException("Naming Exception " +
                                       ne.getExplanation());
            }
        } else if (session != null) {
            try {
                // get initial context from which to lookup session
                Context ctx = new InitialContext();

                // create the mail message using the preconfigured jndi
                // named session
                sessionobj = (Session)ctx.lookup(session);
                message = new MimeMessage(sessionobj);
            } catch (NamingException ne) {
                throw new JspException("Naming Exception " +
                                           ne.getExplanation());
            }
        } else {
            // if configuring one of the above objects the following list
            // are a good set to use to take care of error handling if some
            // addresses are not valid

            // set up the smtp session that will send the message
            Properties props = new Properties();
            // set host to server
            if (reset_server != null) {
                props.put("mail.smtp.host", reset_server);
            } else {
                props.put("mail.smtp.host", server);
            }
            // set port to server
            if (reset_port != null) {
                props.put("mail.smtp.port", reset_port);
            } else {
                props.put("mail.smtp.port", port);
            }
            // set properties to deal will SendFailedExceptions
            // send to all legal addresses
            props.put("mail.smtp.sendpartial", "true");
            // set notification
            props.put("mail.smtp.dsn.notify", "FAILURE");
            // set amount of message to get returned
            props.put("mail.smtp.dsn.ret", "FULL");
            // create new piece of mail for the smtp mail session check if
            // authentication is required for the mail server

            if (authentication) {
                    // create the session with an authenticator object
                    // better way to do authentication
                    props.put("mail.smtp.auth", "true");
                    sessionobj = Session.getDefaultInstance(props,
                                         new MailAuthenticator(user, password));
            } else
                sessionobj = Session.getDefaultInstance(props, null);

            message = new MimeMessage(sessionobj);
        }
	return message;
    }

    /**
     * get the list of attachments
     *
     * @return - an arraylist of the mimebodyparts for attachments
     *
     */
    public ArrayList getBodyParts() {
	return bodyparts;
    }

    /**
     * get the attachments flat
     *
     * @return - the flag that lets the message know if there are attachments
     *
     */
    public boolean getAttachments() {
	return attachments;
    }

    /**
     * get the to address for this email
     *
     * @return - the address to whom this mail is to be sent
     *
     */
    public String getTo() {
        if (addTO.length() > 0 ) {
           return addTO.toString();
        }
        return null;
    }

    /**
     * get the Reply-to address for this email
     *
     * @return - the address to whom this mail is to be sent
     *
     */
    public String getReplyTo() {
        if (reset_replyto != null) {
            return reset_replyto;
        }
	return replyto;
    }

    /**
     * get the from address for this email
     *
     * @return - the address from whom this mail is to be sent
     *
     */
    public String getFrom() {
        if (reset_from != null) {
            return reset_from;
        }
	return from;
    }

    /**
     * get the cc address/addresses for this email
     *
     * @return - list of carbon copy addresses to recieve
     *               this email
     *
     */
    public String getCc() {
        if (addCC.length() > 0 ) {
           return addCC.toString();
        }
        return null;
    }

    /**
     * get the bcc address/addresses for this email
     *
     * @return - list of blind carbon copy addresses to
     *               recieve this email
     *
     */
    public String getBcc() {
        if (addBCC.length() > 0 ) {
	   return addBCC.toString();
        }
        return null;
    }

    /**
     * get the subject for this email
     *
     * @return - string that is the subject of this email
     *
     */
    public String getSubject() {
        if (reset_subject != null) {
            return reset_subject;
        }
	return subject;
    }

    /**
     * get the list names of extra headers to be set
     *
     * @return - list of names of extra headers to be set
     *
     */
    public ArrayList getHeaderName() {
	return name;
    }

    /**
     * get the list of values of extra headers to be set
     *
     * @return - list of values of extra headers to be set
     *
     */
    public ArrayList getHeaderValue() {
	return value;
    }

    /**
     * get the message for this email
     *
     * @return - string that is the body of this email
     *
     */
    public String getBody() {
	return body;
    }


    /**
     * get the jndi named session to be used to find the mail session
     *
     * @return - session object for this email
     *
     */
    public Session getSessionObj() {
	return sessionobj;
    }

    /**
     * get the mime type for this email text or html
     *
     * @return -  string that is the mime type for this email
     *
     */
    public String getType() {
	return type;
    }

    /**
     * get the character set for this email
     *
     * @return -  string that is the character set for this email
     *
     */
    public String getCharset() {
	return charset;
    }

    /**
     * set true if error has occured
     *
     * @param value  value that determines if an error has occured
     *
     */
    public void setError(boolean value) {
	error = value;
    }

    /**
     * set the to address for this email
     *
     * @param value  string that is the address to whom this mail is to be sent
     *
     */
    public void setTo(String value) {
	to = value;
    }

    /**
     * set the Reply-to address for this email
     *
     * @param value  string that is the address to whom this mail is to be sent
     *
     */
    public void setReplyTo(String value) {
	replyto = value;
    }

    /**
     * set the from address for this email
     *
     * @param value string that is the address from whom this mail is to be sent
     *
     */
    public void setFrom(String value) {
	from = value;
    }

    /**
     * set the cc address/addresses for this email
     *
     * @param value  string that is the list of carbon copy addresses to recieve
     *               this email
     *
     */
    public void setCc(String value) {
        cc = value;
    }

    /**
     * set the bcc address/addresses for this email
     *
     * @param value  string that is the list of blind carbon copy addresses to
     *               recieve this email
     *
     */
    public void setBcc(String value) {
	bcc = value;
    }

    /**
     * set the subject for this email
     *
     * @param value  string that is the subject of this email
     *
     */
    public void setSubject(String value) {
	subject = value;
    }

    /**
     * set the name and value of any extra headers to be sent
     *
     * @param name   string that is the name of an extra header to be sent
     * @param value  string that is the value of an extra header to be sent
     */
    protected void setHeader(String name, String value) {
	this.name.add(name);
        this.value.add(value);
    }

    /**
     * set the message for this email
     *
     * @param value  string that is the subject of this email
     *
     */
    public void setMessage(String value) {
	body = value;
    }

    /**
     * set attachments into an arraylist
     *
     * @param mbp  mimebodypart to be attached to the e-mail
     *
     */
    public void setBodyParts(MimeBodyPart mbp) {
	bodyparts.add(mbp);

	// set attachments to true since the possibility of multiple attachments
	// exists there is no sence in setting attachments to true everytime one
	// is set
	if (attachments == false)
	    attachments = true;
    }

    /**
     * set the login user name for basic smtp authentication
     *
     * @param value  login name
     *
     */
    public void setUser(String value) {
	user = value;
    }

    /**
     * set the password for basic smtp authentication
     *
     * @param value  Plain text password for authentication
     *
     */
    public void setPassword(String value) {
	password = value;
    }

    /**
     * set the server to be used to find the mail session if one is not provided
     *
     * @param value  string that is the server used to find the mail session
     *
     */
    public void setServer(String value) {
	server = value;
    }

    /**
     * set the server port to be used for the mail session
     *
     * @param value  string that is the server port to be used
     *
     */
    public void setPort(String value) {
	port = value;
    }

    /**
     * set the jndi named session to be used to find the mail session
     *
     * @param value  string that is the jndi named session
     *
     */
    public void setMimeMessage(String value) {
	mimemessage = value;
    }

    /**
     * set the jndi named session to be used to find the mail session
     *
     * @param value  string that is the jndi named session
     *
     */
    public void setSession(String value) {
	session = value;
    }

    /**
     * set authentication flag
     *
     * @param value  boolean value that marks if an authenticator object should
     *               be used in the creation of the session so that user
     *               authentication can be performed
     *
     */
    public void setAuthenticate(String value) {
	authentication = new Boolean(value).booleanValue();
    }

    /**
     * set the mime type for this email text or html
     *
     * @param value  string that is the mime type for this email
     *
     */
    public void setType(String value) {
	if (value.equalsIgnoreCase("html"))
	    type = "text/html";
	else
	    type = "text/plain";
    }

    /**
     * set the character set for this email
     *
     * @param value  string that is the character set for this email
     *
     */
    public void setCharset(String value) {
        charset = value;
    }

    /**
     * add a to address to the list of to addresses for this email
     *
     * @param value  string that is an address to whom this mail is to be sent
     *
     */
    protected void addTo(String value) {
        if (addTO.length() > 0) {
            addTO.append(",");
        }
	addTO.append(value);
    }

    /**
     * set a cc address to the list of cc addresses for this email
     *
     * @param value  string that is a cc address to be added to the list of cc
     *               addresses for this email
     *
     */
    protected void addCc(String value) {
        if (addCC.length() > 0) {
            addCC.append(",");
        }
        addCC.append(value);
    }

    /**
     * add a  bcc address to the list of bcc addresses for this email
     *
     * @param value  string that is a bcc address to be added to the list of bcc
     *               addresses for this email
     *
     */
    protected void addBcc(String value) {
        if (addBCC.length() > 0) {
            addBCC.append(",");
        }
        addBCC.append(value);
    }

    /**
     * reset the to address to a single email address
     *
     * @param value  string that is an address to whom this mail is to be sent
     */
    protected void resetTo(String value) {
        addTO.setLength(0);
        addTO.append(value);
    }

    /**
     * reset the cc address to a single email address
     *
     * @param value  string that is a cc address to be used for this email
     */
    protected void resetCc(String value) {
        addCC.setLength(0);
        addCC.append(value);
    }

    /**
     * reset the bcc address to a single email address
     *
     * @param value  string that is a bcc address to be used for this email
     */
    protected void resetBcc(String value) {
        addBCC.setLength(0);
        addBCC.append(value);
    }

    /**
     * reset the from address for this email
     *
     * @param value string that is the address from whom this mail is to be sent
     */
    protected void resetFrom(String value) {
        reset_from = value;
    }

    /**
     * reset the replyto address for this email
     *
     * @param value string that is the address for email reply to
     */
    protected void resetReplyTo(String value) {
        reset_replyto = value;
    }

    /**
     * reset the email subject
     *
     * @param value string that is the email subject
     */
    protected void resetSubject(String value) {
        reset_subject = value;
    }

    /**
     * reset the SMTP server hostname
     *
     * @param value string that is SMTP server hostname
     */
    protected void resetServer(String value) {
        reset_server = value;
    }

    /**
     * reset the SMTP server port
     *
     * @param value string that is SMTP server port
     */
    protected void resetPort(String value) {
        reset_port = value;
    }

}

