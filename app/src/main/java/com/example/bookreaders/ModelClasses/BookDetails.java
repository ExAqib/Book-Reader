package com.example.bookreaders.ModelClasses;


public class BookDetails {
    String bookName;
    String authorName;
    String phone;
    String pdfUrl;
    String logoUrl;

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPdfUrl() {
        return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
        this.pdfUrl = pdfUrl;
    }

    public BookDetails() {
    }

    public BookDetails(String bookName, String authorName, String bookCost, String phone, String pdfUrl, String logoUrl) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.phone = phone;
        this.pdfUrl = pdfUrl;
        this.logoUrl = pdfUrl;
    }
}
