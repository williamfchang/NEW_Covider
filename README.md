# NEW_Covider
CSCI 310 Spring 2022, Project 2 (Elaine Yang, Nirav Adunuthula, William Chang)

# Setup App
- We are using a Pixel 4 on API 30

# Running App
- Create an account
- On future openings, login if prompted (you should stay logged in)

# Main App Behavior
Main navigation view: swap between Map, Visits, and Settings
- **From Map tab**: Can open Building List View, interact with building markers
- Building List view and Map view: Building selected will open up Building info View 
- Building Info View: Displays building information, can open Add Visit View
- **From Visits tab**: Can see list of user's Visits, and can open Add Visit View
- Add Visit View: Takes in visit information (building code, start/end time, if visit is for a class, etc.) and creates a Visit. Opens back to main navigation view on completion
- **From Settings tab**: Can view user profile information. Shows contact tracing notifications. Can open Health Report view
- Health Report view: Attestation form to mark symptoms and indicate latest COVID test

# Changes/Additions for 2.5 Sprint

- Added building entry requirements
- Added automatic logging of visits to recurring courses
- Modified Course object to store meeting days, start/end time, and building code
- Added and displayed calculation for building risk level
- Implemented user survey for building safety protocols (when user visits a building), to include in risk level calculation
- Modified map and building list view to prominently display the buildings for the courses the user is enrolled in
- Added ability for instructors to change class mode
- Added behavior where when an instructor tests positive, all courses they teach will automatically switch the class mode to online
- Added push notifications that notify students when the class mode of a course they are enrolled in is changed
- Added push notifications for close contacts
- Changed profile navbar icon to proper image
