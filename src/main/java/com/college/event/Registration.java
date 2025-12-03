package com.college.event;

public class Registration {
    public Integer id;
    public String studentName;
    public String eventName;
    public int tickets = 1;
    public String email;
    public String phone;
    public String createdAt;

    public Registration() {}

    public Registration(Integer id, String studentName, String eventName, int tickets, String email, String phone, String createdAt) {
        this.id = id;
        this.studentName = studentName;
        this.eventName = eventName;
        this.tickets = tickets;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }
}
