/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smtp_client;

public class Mail {
    
    private String from;
    private String to;
    private String subject;
    private String date;
    private String id;
    private String content;

    public Mail(String from, String to, String subject, String date, String content) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.date = date;
        this.content = content;
    }

    public Mail() {
        this.from = "";
        this.to = "";
        this.subject = "";
        this.date = "";
        this.content = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content += content;
    }
    
    
    
}
