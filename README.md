Intro
=====
Aim of this project is to give some basic understanding on how to implement
a facebook client application for Android. This application is made for
testing purposes only and there is no intention on making it work properly on actual
devices. It's emulator, emulator, emulator..

Getting started
===============
1. Get latest [Facebook Android SDK](http://developers.facebook.com/docs/guides/mobile#android) and create a project for it in Eclipse.
2. Download sources for Android Facebook Client and import project
   into Eclipse. This Facebook client utilizes Facebook Android SDK
   and it's required to have it for compilation.
3. Importing project is done e.g. from File menu.
   Select "New" --> "Android Project". And tap "Create project
   from existing source" radio button.
4. If you're seeing errors for missing Facebook class, make sure
   Client project properties contains Facebook Android SDK project
   under Android references. Sometimes it's requires to remove
   this reference and add it again for it to work.

Fetching sources is maybe easiest done using EGit Eclipse plugin but feel free to use Git client of your choice.

ToDo
====
1. Ponder over DAO implementation - and fix/improve it eventually.
  <br>- Getting there.
2. Start testing chat functionality.
  <br>- Partially done. There is some chat functionality added now.

Chat
====
* On chat activity press connect button to start login procedure.
* Once connection is established list of users/friends online should be updated properly.
* Clicking on user opens a separate chat/conversation view.
* For testing purposes there is connect, disconnect and show log buttons visible at all times.
* This is more of a proof of concept than proper implementation at the moment.

Thank you's
===========
Application icon [from here](http://www.iconarchive.com/show/circle-social-bookmark-icons-by-fasticon.html).
Main View icons [from here](http://brsev.deviantart.com/art/Token-128429570).
Base64 encoder/decoder [from here](http://migbase64.sourceforge.net/).
