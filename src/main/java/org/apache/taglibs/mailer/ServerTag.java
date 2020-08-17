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
 * ServerTag - JSP tag <b>server</b> is used for dynamically setting the mail
 *          server that is going to be used to send the mail the tag must be
 *          nested within a <b>mail</b> tag.
 *
 * <p>
 * JSP Tag Lib Descriptor
 * <p><pre>
 * &lt;name&gt;server&lt;/name&gt;
 * &lt;tagclass&gt;org.apache.taglibs.mailer.ServerTag&lt;/tagclass&gt;
 * &lt;bodycontent&gt;JSP&lt;/bodycontent&gt;
 * &lt;info&gt;
 *     Sets the mail server used to send the mail
 * &lt;/info&gt;
 * </pre></p></p>
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 */
public class ServerTag extends BodyTagSupport {


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
            throw new JspException("server tag not nested within mail tag");
        }

        BodyContent body = getBodyContent();
        String server = body.getString();
        // Clear the body since we only used it as input for the email address
        body.clearBody();
        if (server != null) {
            server.trim();
            if (server.length() > 0) {
                myparent.resetServer(server); // set server in the parent tag
                return SKIP_BODY;
            }
        }
        throw new JspException("The server tag is empty");
    }
}
