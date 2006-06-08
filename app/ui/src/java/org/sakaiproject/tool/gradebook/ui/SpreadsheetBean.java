package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.Date;

/**
 * User: louis
 * Date: Jun 6, 2006
 * Time: 12:11:01 PM
 */
public class SpreadsheetBean extends GradebookDependentBean implements Serializable {

    private String title;
    private Date date;
    private String userId;
    private String contents;
    private String displayName;
    private Long gradebookId;


    public SpreadsheetBean(String title, Date date, String userId, String contents) {
        this.title = title;
        this.date = date;
        this.userId = userId;
        this.contents = contents;
    }

    public SpreadsheetBean() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getGradebookId() {
        return gradebookId;
    }

    public void setGradebookId(Long gradebookId) {
        this.gradebookId = gradebookId;
    }

}
