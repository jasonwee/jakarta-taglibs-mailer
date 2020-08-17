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
 * ReplyToTag - JSP tag <b>ReplyTo</b> is used to set the To address in an e-mail.
 *
 * <tag>
 *        <name>replyto</name>
 *	  <tagclass>org.apache.taglibs.ReplyToTag</tagclass>
 *	  <bodycontent>JSP</bodycontent>
 *	  <info>Set the Reply-To address of the email</info>
 * </tag>
 *
 * @author Rich Catlett
 *
 * @version 1.0
 *
 */

public class ReplyToTag extends BodyTagSupport {


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
            throw new JspException("replyto tag not nested within mail tag");
        }

        BodyContent body = getBodyContent();
        String replyto = body.getString();
        // Clear the body since we only used it as input for the email address
        body.clearBody();
        if (replyto != null) {
            replyto.trim();
            if (replyto.length() > 0) {
                myparent.resetReplyTo(replyto); // set replyto in the parent tag
                return SKIP_BODY;
            }
        }
        throw new JspException("The replyto tag is empty");
    }
}
