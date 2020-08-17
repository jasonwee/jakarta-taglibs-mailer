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
import java.util.Iterator;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.BodyTagSupport;

/**
 * ErrorTag - JSP tag <b>Error</b> is used to get the error message when creating
 *            an error page.
 *
 * <tag>
 *        <name>error</name>
 *	  <tagclass>org.apache.taglibs.mail.ErrorTag</tagclass>
 *	  <bodycontent>JSP</bodycontent>
 *	  <info>Get the error message if mail was unable to be sent</info>
 *
 *        <attribute>
 *                <name>id</name>
 *                <required>true</required>
 *                <rtexprvalue>false</rtexprvalue>
 *        </attribute>
 * </tag>
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 */

public class ErrorTag extends BodyTagSupport {

    // an iterator for the list of error messages
    private Iterator errorlist = null;
    private ArrayList  errors = null;  // the error message
    private String error = null;      // the current error from the list

    /**
     *  implementation of the method from the tag interface that tells the JSP
     *  page what to do when the start tag is encountered
     *
     *  @throws JspException  thrown when an error occurs while processing the
     *                        body of this method
     *
     *  @return - SKIP_BODY if no errors exist, EVAL_BODY_TAG if errors do exist
     *
     */
    public int doStartTag() throws JspException {

	// parent tag must be a MailTag, gives access to methods in parent
	SendTag myparent = (SendTag)findAncestorWithClass(this, SendTag.class);

	if (myparent == null)
	    throw new JspException("error tag not nested within send tag");
        else
	    if ((errors = myparent.getError()) == null)
		return SKIP_BODY;

	errorlist = errors.iterator();
	if (!errorlist.hasNext())
	    return SKIP_BODY;

	// get the next element in the list
	error = (String)errorlist.next();

	// final check to make sure property exists
	if (error == null)
	    return SKIP_BODY;

	pageContext.setAttribute(id, this, PageContext.PAGE_SCOPE);
	// taglibs 1.1
	return EVAL_BODY_TAG;
	// taglibs 1.2
	//return EVAL_BODY_BUFFERED;
    }

    /**
     * Implementation of the method from the tag interface that gets called at
     * end of each error tag.
     *
     * @return EVAL_BODY_TAG if there is another error, or SKIP_BODY if there
     *                      are no more errors 
     */
    public final int doAfterBody() throws JspException
    {
	// See if this is the last or a named parameter
	if (!errorlist.hasNext())
	    return SKIP_BODY;
	// There is another parameter, so loop again
	error = (String)errorlist.next();
	if (error == null)
	    return SKIP_BODY;
	// taglibs 1.1
	return EVAL_BODY_TAG;
	// taglibs 1.2
	// return EVAL_BODY_AGAIN;
    }

    /**
     *  implementation of the method from the tag interface that tells the JSP
     *  page what to do when the start tag is encountered
     *
     *  @throws JspException  thrown when an error occurs while processing the
     *                        body of this method
     *
     *  @return - EVAL_PAGE
     *
     */
    public int doEndTag() throws JspException {
	try {
	    if (bodyContent != null)
		bodyContent.writeOut(bodyContent.getEnclosingWriter());
	} catch (java.io.IOException e) {
	    throw new JspException("IO Error: " + e.getMessage());
	}
	return EVAL_PAGE;
    }

    /**
     * Returns the name of the current error.
     * <p>
     * &lt;jsp:getProperty name=<i>"id"</i> property="error"/&gt;
     *
     * @return String - the current error in the list
     */
    public String getError()
    {
	return error;
    }

    /**
     * Remove the script variable after error tag is closed out
     */
    public void release()
    {
	if( id != null && id.length() > 0 )
	    pageContext.removeAttribute(id,PageContext.PAGE_SCOPE);
    }
}
