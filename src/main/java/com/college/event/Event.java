package com.college.event;

public class Event {
    public Integer id;
    public String title;
    public String type;
    public String startDatetime;
    public String endDatetime;
    public String venue;
    public String description;
    public String rules;
    public String coordinators;
    public String prizes;
    public String fee;
    public String banner;

    public Event() {}

    public Event(Integer id, String title, String type, String startDatetime, String endDatetime, String venue, String description, String rules, String coordinators, String prizes, String fee, String banner) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.venue = venue;
        this.description = description;
        this.rules = rules;
        this.coordinators = coordinators;
        this.prizes = prizes;
        this.fee = fee;
        this.banner = banner;
    }
}
