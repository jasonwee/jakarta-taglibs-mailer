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
 * HeaderTag - JSP tag <b>Header</b> is used to set headers in an e-mail message.
 *
 * <tag>
 *      <name>header</name>
 *      <tagclass>org.apache.taglibs.mailer.HeaderTag</tagclass>
 *      <bodycontent>JSP</bodycontent>
 *      <info>Set the To address of the email</info>
 *
 *      <attribute>
 *     	        <name>name</name>
 *		<required>true</required>
 *      	<rtexprval>false</rtexprval>
 *      </attribute>
 *      <attribute>
 *      	<name>value</name>
 *		<required>false</required>
 *		<rtexprval>false</rtexprval>
 *      </attribute>
 * </tag>
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 */

public class HeaderTag extends BodyTagSupport {

    /**
     * name of header to be set
     */
    String name;
    /**
     * value of header to be set
     */
    String value = null;

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
	    throw new JspException("header tag not nested within mail tag");
        }
        BodyContent body = getBodyContent();
        String valueStr = value;
        if (valueStr == null ) {
            valueStr = body.getString();
        }
        // Clear the body since we only used it as input for the header value
        body.clearBody();
        if (valueStr == null) {
            throw new JspException("The header tag is empty");
        }
        myparent.setHeader(name,valueStr); // set header in parent tag
	return SKIP_BODY;
    }

    /**
     * set the name of the header to be set
     *
     * @param value  string that is the name of the header to be set
     *
     */
    public void setName(String value) {
	name = value;
    }

    /**
     * set the value of the header to be set
     *
     * @param value string that the value of the header to be set
     *
     */
    public void setValue(String value) {
	this.value = value;
    }
}
