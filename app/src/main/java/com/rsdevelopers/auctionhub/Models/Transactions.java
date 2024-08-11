package com.rsdevelopers.auctionhub.Models;

import java.util.Date;

public class Transactions {
    Date transDate;
    double transAmount;
    String transType, transReason, transId;

    public Transactions() {
    }

    public Transactions(Date transDate, double transAmount, String transType, String transReason, String transId) {
        this.transDate = transDate;
        this.transAmount = transAmount;
        this.transType = transType;
        this.transReason = transReason;
        this.transId = transId;
    }

    public Date getTransDate() {
        return transDate;
    }

    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

    public double getTransAmount() {
        return transAmount;
    }

    public void setTransAmount(double transAmount) {
        this.transAmount = transAmount;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTransReason() {
        return transReason;
    }

    public void setTransReason(String transReason) {
        this.transReason = transReason;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }
}
