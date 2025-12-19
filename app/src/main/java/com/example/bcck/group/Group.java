package com.example.bcck.group;

public class Group {
    private String groupName;
    private int memberCount;

    public Group(String groupName, int memberCount) {
        this.groupName = groupName;
        this.memberCount = memberCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getMemberCount() {
        return memberCount;
    }
}