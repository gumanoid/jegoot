TEMPLATE = app
CONFIG += console
CONFIG -= app_bundle
CONFIG -= qt

SOURCES += main.cpp \
    gtest/gtest-all.cc

include(deployment.pri)
qtcAddDeployment()

HEADERS += \
    gtest/gtest.h

