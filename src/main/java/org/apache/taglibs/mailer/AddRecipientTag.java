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

import java.util.*;
import jakarta.servlet.jsp.*;
import jakarta.servlet.jsp.tagext.*;

/**
 * AddRecipientTag - JSP tag <b>addrecipient</b> is used to add any type of
 *                recipient to an already existant list of recipients in an
 *                e-mail message. Two attributes are required, type and address.
 *                Type may be either "to", "cc", or "bcc" and address should be
 *                a string representation of the recipients e-mail address.
 *
 * <tag>
 *        <name>addrecipient</name>
 *	  <tagclass>org.apache.taglibs.mailer.AddRecipientTag</tagclass>
 *	  <bodycontent>JSP</bodycontent>
 *	  <info>
 *          Append a recipient to the current recipients of the e-mail.
 *        </info>
 *
 *	  <attribute>
 *	     <name>type</name>
 *	     <required>true</required>
 *	    <rtexprvalue>false</rtexprvalue>
 *	  </attribute>
 *	  <attribute>
 *	     <name>address</name>
 *	     <required>false</required>
 *	    <rtexprvalue>false</rtexprvalue>
 *	  </attribute>
 * </tag>
 *
 * @author Rich Catlett Jayson Falkner
 *
 * @version 1.0
 *
 */

public class AddRecipientTag extends BodyTagSupport {

    /**
     * The type of address to add either to, cc, or bcc
     */
    private String type_string = null;
    private int type = 0;

    /**
     * The address to be added
     */
    private String address = null;

    /**
     *  implementation of the method from the tag interface that tells the JSP
     *  page what to do at the start of this tag
     *
     *  @throws JspTagException  thrown when an error occurs while processing the
     *                        body of this method
     *
     *  @return SKIP_BODY  int telling the tag handler to not evaluate the body
     *                     of this tag
     *          EVAL_BODY_TAG  int telling the tag handler to evaluate the body
     *                         of this tag
     *
     */
    public int doStartTag() throws JspException {
        if (address != null && address.length() > 0 ) {
            addToParent(address);
            return SKIP_BODY;
	}
	return EVAL_BODY_TAG;
    }


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
        BodyContent body = getBodyContent();
        String addr = body.getString();
        // Clear the body since we only used it as input for the email address
        body.clearBody();
        if (addr != null && addr.length() > 0 ) {
            addToParent(addr);
            return SKIP_BODY;
	} else {
	    throw new JspException("addrecpient tag could not find an email address. set " +
			     " the address attribute, or place the address in" +
			     " the body of the tag.");
        }
    }

    /**
     * set the type of recipient for the address
     *
     * @param type  string that is the type of the address either
     *   "to", "cc", or "bcc".
     */
    public void setType(String type) throws JspTagException {
      this.type_string = type.trim();
      if (type_string.equalsIgnoreCase(MailTag.TO_ADDRESS_STRING)) {
          this.type = MailTag.TO_ADDRESS;
      } else if (type_string.equalsIgnoreCase(MailTag.CC_ADDRESS_STRING)) {
          this.type = MailTag.CC_ADDRESS;
      } else if (type_string.equalsIgnoreCase(MailTag.BCC_ADDRESS_STRING)) {
          this.type = MailTag.BCC_ADDRESS;
      } else {
          throw new JspTagException
             ("addrecipient tag type attribute must be \"to\", \"cc\", or \"bcc\"");
      }
    }

    /**
     * set the value for an address to be later added to the email
     *
     * @param address  string that is an address to be added to the
     *   "to", "cc", or "bcc" lists of addresses.
     */
    public void setAddress(String address) {
      this.address = address.trim();
    }

    protected void addToParent(String addr) throws JspTagException {
      // parent tag must be a MailTag, gives access to methods in parent
      MailTag myparent =
          (MailTag)findAncestorWithClass
              (this, MailTag.class);

      if (myparent == null) {
          throw new JspTagException("addrecipient tag not nested within mail tag");
      }

      // Make sure type is set..either "to", "cc", or "bcc"
      switch (type) {
          case MailTag.TO_ADDRESS:
              // set to in the parent tag
              myparent.addTo(addr);
              break;
          case MailTag.CC_ADDRESS:
              // set cc in the parent tag
              myparent.addCc(addr);
              break;
          case MailTag.BCC_ADDRESS:
              // set bcc in the parent tag
              myparent.addBcc(addr);
              break;
          default:
              throw new JspTagException("addrecipient tag type attribute is not set. " +
                           " Specify either \"to\", \"cc\", or \"bcc\".");
      }
    }

}
