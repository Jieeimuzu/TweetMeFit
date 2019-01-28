package com.example.james.tweetmefit;

public class tweetRetreive {

    private String profilePic;
    private String username;
    private String tweetText;
    private String date;
    private String imgUrls;

    public tweetRetreive(String profilePic, String username, String tweetText, String date, String imgUrls) {
        this.profilePic = profilePic;
        this.username = username;
        this.tweetText = tweetText;
        this.date = date;
        this.imgUrls = imgUrls;
    }


    public String getprofilePic() {
        return profilePic;
    }

    public void setProfilePic(String ProfilePic) {
        this.profilePic = profilePic;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTweetText() {
        return tweetText;
    }

    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(String imgUrls) {
        this.imgUrls = imgUrls;
    }
}
