package com.weefeesecure.wifisecure;

/**
 * Created by Jinchi on 4/20/2016.
 */
public class Info {
    private String title,description,detailedDescription;

    public Info(String title, String description, String detailedDescription) {
        this.title = title;
        this.description = description;
        this.detailedDescription = detailedDescription;
    }

    public String getTitle() {
        return title;
    }

    public boolean setTitle(String title) {
        if (title == null)
            return false;
        this.title = title;
        return true;
    }

    public String getDescription() {
        return description;
    }

    public boolean setDescription(String description) {
        if (description == null)
            return false;
        this.description = description;
        return true;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public boolean setDetailedDescription(String detailedDescription) {
        if (detailedDescription == null)
            return false;
        this.detailedDescription = detailedDescription;
        return true;
    }
}
