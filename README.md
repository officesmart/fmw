This repo contains the sources for a mobile app to track and report the location of people in 
an indoor area.  A typical use case is to track employees in an office building.
The application performs two main functions:

1.  Track the location of people.  This is accomplished by users (people being tracked) running 
the application on their mobile device.  The application uses the indoo.rs (https://indoo.rs/)
SDK for Android in combination with BLE beacons installed in an indoor space to track and
report the location of users.  The locations of users are transmitted from the mobile device 
to the cloud and store in the cloud.  Each area of significance in an indoor space is 
designated as a "zon" in indoo.rs terminology, and each user may have a last known "zone" 
associated with it in the cloud.  This functionality is currently implemented by the app in 
Android only, but an iOS version will be required in the future.  To implement this functionality
in React Native, a bridge approach (bridge between React and native Android or iOS) code needs 
to be built because there is no Javascript SDK for indoo.rs.  Only Android and iOS SDKs are 
provided by indoo.rs.

2.  Display the list of users being tracked and their locations.  The app implements this by 
making a number of API calls to the back-end code to retrieve the list of users and their 
locations from the cloud.

The initial conversion effort will focus on implementing function #2.  Function #1 will require 
developers to have access to the indoo.rs beacons.






