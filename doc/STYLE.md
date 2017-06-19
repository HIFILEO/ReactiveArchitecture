# STYLE.md

## About

This document describes the code style standards and static analysis tools configured for the project.

### Code Style

The general code style for this project can be read in detail on the [wiki](https://google.github.io/styleguide/javaguide.html).

### CheckStyle

The project has [checkstyle](http://checkstyle.sourceforge.net/) configured to ensure the code conforms to formatting and style standards.
The specific checkstyle rules and constraints for this project are found
in [config/checkstyle/checkstyle.xml](https://github.com/HIFILEO/MVPExample/blob/master/config/checkstyle/checkstyle.xml) file that is in git for this project.

To run checkstyle against your project

1. `./gradlew clean checkstyle`


### Lint

This project has [android lint](http://developer.android.com/tools/help/lint.html) configured to preform static code analysis which checks for potential bugs and optimization improvements for correctness, security, performance, usability, accessibility, and internationalization.
To review the specific constraints for this project see the **config/lint** folder for details.

To run lint against the project files:

1. Android Studio: Analyze->Inspect Code
2. Gradle: `./gradlew lintProdGoogleDebug`

Note - running `./gradlew lint` will run against all libraries as well.

### JACOCO

This project also has JACOCO configured to create code coverage reports for all classes created within the app module.

To run jacoco against your project:

1. `./gradlew jacocoTestReport`

Note - to review code coverage open the file at:
file:///**path/to/project**/app/build/reports/jacoco/test/html/index.html