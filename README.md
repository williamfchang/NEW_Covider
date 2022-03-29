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
