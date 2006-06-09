package org.sakaiproject.tool.gradebook.ui;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import javax.faces.context.FacesContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * User: louis
 * Date: Apr 26, 2006
 * Time: 10:10:23 AM
 */
public class SpreadsheetUploadBean implements Serializable {


    private String title;
    private UploadedFile upFile;
    private static final Log logger = LogFactory.getLog(SpreadsheetUploadBean.class);
    private List contents;


    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UploadedFile getUpFile() {
        logger.debug("getUpFile()" +upFile);
        return upFile;
    }

    public void setUpFile(UploadedFile upFile) {
         logger.debug("setUpFile() " + upFile);
        if(upFile!=null) logger.debug("file name: " + upFile.getName() +"");
        this.upFile = upFile;
    }


    public List getContents() {
        return contents;
    }

    public void setContents(List contents) {
        this.contents = contents;
    }

    public String processFile() throws Exception {

        /**
         * TODO this needs revision espceially the session stuff next item online
         */

        logger.debug("processFile()-----------------------------------------");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

        logger.debug("file size " + upFile.getSize() + "");
        logger.debug("file name " + upFile.getName() + "");
        logger.debug("file Content Type " + upFile.getContentType() + "");


        InputStream inputStream = new BufferedInputStream(upFile.getInputStream());
        logger.debug("inputstream contents : "+inputStream.toString() );

        contents = csvtoArray(inputStream);

       logger.debug("total lines in file:" +contents.size());
       logger.debug("end processFile()----------------------------------------");

      session.setAttribute("filecontents",contents);
      session.setAttribute("filename",this.getTitle());

      return "spreadsheetUploadPreview";


    }

    /**
     * method converts an input stream to an List consist of strings
     * representing a line
     *
     * @param inputStream
     * @return contents
     */
    private List csvtoArray(InputStream inputStream) throws IOException{

        /**
         * TODO this well probably be removed
         */
    
        logger.debug("csvtoArray()");
        List contents = new ArrayList();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        contents = new ArrayList();
        String line;
        while((line = reader.readLine())!=null){
            logger.debug("contents of line: "+line);
            contents.add(line);
        }
        return contents;

    }



}



