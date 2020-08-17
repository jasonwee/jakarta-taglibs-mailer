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

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyContent;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

/**
 * MessageTag - JSP tag <b>Message</b> is used to set the message in an e-mail.
 *
 * <tag>
 *      <name>message</name>
 *      <tagclass>org.apache.taglibs.mailer.MessageTag</tagclass>
 *      <bodycontent>JSP</bodycontent>
 *      <info>Set the Message of the email</info>
 *
 *      <attribute>
 *            <name>type</name>
 *            <required>false</required>
 *            <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 *      <attribute>
 *            <name>charset</name>
 *            <required>false</required>
 *            <rtexprvalue>false</rtexprvalue>
 *      </attribute>
 * </tag>
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 */

public class MessageTag extends BodyTagSupport {

    /**
     * can be "text" or "html" (otherwise text is default)
     */
    private String type = "text";

    /**
     * character set to be used (default is unspecified)
     */
    private String charset = null;

    /**
     *  implementation of the method from the tag interface that tells the JSP
     *  page what to do after the body of this tag
     *
     *  @throws JspException  thrown when an error occurs while processing the
     *                        body of this method
     *
     *  @return SKIP_BODY  int telling the tag handler to not evaluate the body
     *                     of this tag again
     *
     */
    public int doAfterBody() throws JspException {

	// parent tag must be a MailTag, gives access to methods in parent
	MailTag myparent = (MailTag)findAncestorWithClass(this, MailTag.class);
	if (myparent == null) {
	    throw new JspException("message tag not nested within mail tag");
        }

        BodyContent body = getBodyContent();
        String message = body.getString();
        // Clear the body since we only used it as input for the email address
        body.clearBody();
        if (message == null) {
            throw new JspException("The message tag is empty");
        }
	myparent.setMessage(message); // set message in the parent tag
	myparent.setType(type);  // set the mime type of the message
	myparent.setCharset(charset);  // set the character set of the message
	return SKIP_BODY;
    }

    /**
     * set the mime type for this email text or html
     *
     * @param value  string that is the mime type for this email
     *
     */
    public void setType(String value) {
	type = value;
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
}
