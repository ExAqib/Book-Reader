package com.example.bookreaders.ModelClasses;

public class Users
{
    private String  Name,Phone,Email,Password,ImageUrl;
    Users(){
    }

    public Users(String name, String phone, String email, String password, String imageUrl) {
        Name = name;
        Phone = phone;
        Email = email;
        Password = password;
        ImageUrl = imageUrl;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
