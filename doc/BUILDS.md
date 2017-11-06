# BUILDS.md

This project is built to use Java 8 and Android Studio 3.0 running on a Mac. 

### Usage

This document describes official test and production builds.
There are two types of signing, build types, and product environments inside the flavors.

#### Build Types

1. **Debug**
3. **Release**

**Debug** builds are intended to be used by the developers.
They have debug enabled while signing with the standard debug keystore.
They also have test coverage enabled for JACOCO reports.
These builds will also have developer tools integrated whenever possible. 

**Release** builds are intended to be published in the Android stores.
All debugging features are disabled.
The apk is signed with the release keystore. (Not this example app)
ProGuard is enabled.

#### Environment Flavors

The build's environment flavor specifies which set of endpoints the app will use for various services. For this 
example there are no flavors.

