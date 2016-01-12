#include <iostream>

#include "gtest/gtest.h"

#include <iostream>

//TEST(SomeGroup, TestIsTrue) {
//    ASSERT_TRUE(0 == 0);
//}

//TEST(SomeGroup, FailingTest) {
//    ASSERT_FALSE(0 == 0);
//}

//TEST(OtherGroup, ExpectTest) {
//    EXPECT_EQ(0, 0);
//    EXPECT_EQ(1, 2);
//    EXPECT_EQ(2, 3);
//}

//class SomeFixture : public ::testing::Test {
//public:
//    SomeFixture() : someInt(3) {
//        std::cout << "Fixture constructor" << std::endl;
//    }
//    ~SomeFixture() {
//        std::cout << "Fixture destructor" << std::endl;
//    }

//protected:
//    void SetUp() override {
//        std::cout << "Fixture SetUp" << std::endl;
//    }
//    void TearDown() override {
//        std::cout << "Fixture TearDown" << std::endl;
//    }

//    int someInt;
//};

//TEST_F(SomeFixture, FixtureTest) {
//    ASSERT_EQ(someInt, 3);
//}

int main(int argc, char *argv[])
{
//    ::testing::GTEST_FLAG(print_time) = false;

    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
