/*
 * Copyright (c) 2008-2016 Maxifier Ltd. All Rights Reserved.
 */
package gumanoid.event;

import com.google.common.base.Objects;

/**
 * Events parsed from GTest enumeration lines
 *
 * @author Kirill Gamazkov (kirill.gamazkov@maxifier.com) (2016-01-25 19:33)
 */
public class GTestListEvent {
    protected GTestListEvent() {}

    public static class GroupAnnounce extends GTestListEvent {
        public final String groupName;

        public GroupAnnounce(String groupName) {
            this.groupName = groupName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupAnnounce that = (GroupAnnounce) o;
            return Objects.equal(groupName, that.groupName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(groupName);
        }

        @Override
        public String toString() {
            return "GroupAnnounce{" +
                    "groupName='" + groupName + '\'' +
                    '}';
        }
    }

    public static class TestAnnounce extends GTestListEvent {
        public final String groupName;
        public final String testName;

        public TestAnnounce(String groupName, String testName) {
            this.groupName = groupName;
            this.testName = testName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestAnnounce that = (TestAnnounce) o;
            return Objects.equal(groupName, that.groupName) &&
                    Objects.equal(testName, that.testName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(groupName, testName);
        }

        @Override
        public String toString() {
            return "TestAnnounce{" +
                    "groupName='" + groupName + '\'' +
                    ", testName='" + testName + '\'' +
                    '}';
        }
    }
}
