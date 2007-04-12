-- Guard against data truncation.
-- See http://bugs.sakaiproject.org/jira/browse/SAK-9398 for details.
ALTER TABLE GB_SPREADSHEET_T MODIFY COLUMN CONTENT MEDIUMTEXT; 