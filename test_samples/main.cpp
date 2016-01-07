#include <iostream>

#include "gtest/gtest.h"

#include <windows.h>

TEST(SomeGroup, TestIsTrue) {
    ASSERT_TRUE(0 == 0);
}

TEST(SomeGroup, FailingTest) {
    ASSERT_FALSE(0 == 0);
}

TEST(OtherGroup, LongTest) {
    ASSERT_EQ(0, 0);
    Sleep(2000);
    ASSERT_EQ(1, 2);
}

int main(int argc, char *argv[])
{
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
