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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;


/**
 * AttachTag - JSP tag <b>Attach</b> is used to set the message in an email.
 *
 * <tag>
 *   <name>attach</name>
 *   <tagclass>org.apache.taglibs.mailer.AttachTag</tagclass>
 *   <bodycontent>JSP</bodycontent>
 *   <info>Adds attachments to email.</info>
 * </tag>
 *
 * @author Rich Catlett Jayson Falkner
 *
 * @version 1.0
 *
 */

public class AttachTag extends BodyTagSupport {

    /**
     * mime type of the attachment
     */
    private String type = null;

    /**
     * holds the value of body if the url is to be retrieved from the body of
     * tag
     */
    private String url = null;

    /**
     * holds the value of body if the path to the file is to be retrieved from
     * the body of the tag
     */
    private String file = null;

    /**
     * object in which the attachment is stored within the e-mail message
     */
    private MimeBodyPart mbp = null;

    /**
     * parent mail tag
     */
    private MailTag myparent = null;

    /**
     * String containing text in body of tag
     */
    private String body;

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

        myparent = (MailTag)findAncestorWithClass(this, MailTag.class);
        if (myparent == null) {
            throw new JspException("Attach tag not nested within mail tag.");
        }
        mbp = new MimeBodyPart();  // create the bodypart for this attachment
        body = null;
        if (type != null || (file != null && file.length() == 0) ||
                (url != null && url.length() == 0) ) {
            return EVAL_BODY_TAG;
        }
        return SKIP_BODY;
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

        body = bodyContent.getString();
        bodyContent.clearBody();
        if (body == null) {
            body = "";
        }
        return SKIP_BODY;
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

        if (type != null) {
            // the body of the tag is expected to be added as the attachment
            // create a mimebodypart from the body of the tag
            try {
                mbp.setDataHandler(new DataHandler(body, type));
            } catch (MessagingException me) {
                throw new JspException(
                    "The attachment named with the mimetype " + type +
                    " could not be attached.");
            }
        } else if (file != null) {
            if (file.length() == 0) {
                body.trim();
                // the file name is supposed to come from the body of the tag
                if (body.length() > 0 ) {
                    // prepare the file or url resource to be an attachment
                    setFileBodyPart(body);
                } else {
                    // body is empty throw error
                    throw new JspException(
                        "The file name must be given in the body of this tag.");
                }
            } else {
                // create the attachment with the file name in the file
                // attribute
                setFileBodyPart(file);
            }
        } else if (url != null) {
            if (url.length() == 0) {
                body.trim();
                // the url is supposed to come from the body of the tag
                if (body.length() > 0) {
                    // prepare the file or url resource to be an attachment
                    setUrlBodyPart(body);
                } else {
                    // body is empty throw error
                    throw new JspException(
                        "The url must be givenin the body of this tag.");
                }
            } else
                // create the attachment with the url in the url attribute
                setUrlBodyPart(url);
        }

        // Add the attachment to list of attachments
        myparent.setBodyParts(mbp);
        return EVAL_PAGE;
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
     * set the resource named by URL into a mimebodypart so that it can be added
     * to the list of attachments for this e-mail
     *
     * @param value  full url including http://, to the resource to be added as
     *               an attachment
     */
    public void setUrl(String value) {
        url = value;
    }

    /**
     * set the named file up as an attachment to be added to the list of
     * attachments for this e-mail
     *
     * @param value  name of the file to be added as an attachment
     *
     */
    public void setFile(String value) {
        file = value;
    }

    /**
     * wrap the url named attachment in the approiate datahandler and create a
     * mimebodypart to be added to the list of attachments
     *
     * @param value  string that represents a URL
     *
     */
    protected void setUrlBodyPart(String value) throws JspException {

// Added by Jayson Falkner - 5/8/2001

        try {
            URL url = new URL(value);
            mbp.setDataHandler(new jakarta.activation.DataHandler(url));
            if(url.getFile() != null)
                mbp.setFileName(url.getFile());
            else
                mbp.setFileName(value);

        } catch(MalformedURLException e) {
            throw new JspException("The URL entered as an attachment was " +
                        "incorrectly formatted please check it and try again.");
        } catch(MessagingException e) {
            throw new JspException("The Resource named by " + url + " could not"
                                   + " be attached.");
        }
// End of added
    }

    /**
     * wrap the file attachment in the approiate datahandler and create a
     * mimebodypart to be added to the list of attachments
     *
     * @param value  string that represents a file path
     *
     */
    protected void setFileBodyPart(String value)  throws JspException {

        // create a real path from the webapplication realative path given as
        // the name of the file to attach
        String rpath = pageContext.getServletContext().getRealPath(value);

// Added by Jayson Falkner - 5/8/2001
        try {
            File file = new File(rpath);
            if (file.exists()) {
                jakarta.activation.DataSource attachment = new jakarta.activation.FileDataSource(file);
                mbp.setDataHandler(new jakarta.activation.DataHandler(attachment));
                mbp.setFileName(file.getName());
            } else {
                // if the file does not exist it is probably an error in the way
                // the page author is adding the path throw an exception so this
                // can be discovered and fixed
                throw new JspException("File " + rpath + " does not exist or " +
                                       "the path to the file is incorrect.");
            }
        } catch(MessagingException e) {
            throw new JspException("The file named by " + file + " could not be"
                                   + " attached.");
        }

// End of added
    }
}
