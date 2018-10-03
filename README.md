[![Build Status](https://travis-ci.org/ayaankazerouni/deveventtracker-feedback.svg?branch=master)](https://travis-ci.org/ayaankazerouni/deveventtracker-feedback)

# deveventtracker-feedback

Incremental development feedback provider for Web-CAT projects that collect Sensordata.

[Documentation](https://ayaankazerouni.github.io/deveventtracker-feedback)

Given an assignment, this package retrieves developer events from the database, calculates development-process scores, and places them in another table in the database. Web-CAT can simply query the new table and decide how to respond to the feedback.

Currently calculates:
* Early/Often Index (See the paper [here](https://people.cs.vt.edu/ayaan/assets/publications/p191-kazerouni.pdf)): A measure of procrastination -- the mean days-until-deadline for each unit of work on the project.
