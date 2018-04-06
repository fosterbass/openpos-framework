package org.jumpmind.pos.user.model;

import java.util.Date;

import org.jumpmind.pos.persist.Column;
import org.jumpmind.pos.persist.Entity;
import org.jumpmind.pos.persist.Table;

@Table
public class PasswordHistory extends Entity {
    
    @Column
    private String userRowId;
    @Column(naturalKey=true)
    private String username;
    @Column(naturalKey=true)
    private int passwordSequence;
    @Column(size="254")
    private String hashedPassword;
    @Column
    private Date expirationTime;
    
    public String getUserRowId() {
        return userRowId;
    }
    public void setUserRowId(String userRowId) {
        this.userRowId = userRowId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getPasswordSequence() {
        return passwordSequence;
    }
    public void setPasswordSequence(int passwordSequence) {
        this.passwordSequence = passwordSequence;
    }
    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public Date getExpirationTime() {
        return expirationTime;
    }
    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
    
}
