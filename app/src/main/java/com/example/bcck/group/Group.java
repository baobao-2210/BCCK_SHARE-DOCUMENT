package com.example.bcck.group;

public class Group {
    private String groupId;
    private String groupName;
    private int memberCount;

    public Group(String groupId, String groupName, int memberCount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.memberCount = memberCount;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getMemberCount() {
        return memberCount;
    }
}
