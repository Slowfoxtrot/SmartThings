AEON-HEM-v2
===========
This is built off SANdood's version but reworked with GUI, color, and text updates.  Since I have a AEON Labs Energy Reader (gen1) I have made it backwards compatibile, but left all HEM v2 code there commented out.

The main difference in this version is the removal of the Volts and Amps tile display which were capabilities added to the Gen2 Energy Reader.

To use this code with your AEON Labs Energy Reader (gen1):
  1) Pair your AEON Labs Energy Reader (gen1) using the Smart App and the pair button on the Energy Reader (inside battery compartment)
  2) Login to the Web management interface for SmartThings (https://graph.api.smartthings.com/)
  3) Create a New Device Handler
    3.1) Click to the "My Device Handlers" tab
    3.2) Click "Create New Device Handler"
    3.3) Choose "From Code" and paste the code from "AEON HEM v1.groovy" file
    3.4) Click "Create" below the space where you pasted code
    3.5) Now you have a new device handler; click "Publish > For Me" to make this available for use on your SmartThings devices
  4) Tell SmartThings to use our new Device Handler for your paired AEON Labs Energy Reader
    4.1) In your My Devices in the SmartThings website, click on the newly paired device (likely named Aeon Home Energy Meter)
    4.2) Below the device information, click "Edit"
    4.3) In the Type field, choose "Aeon HEMv2"
    4.4) Click Update
  5) Update your kWh Cost
    5.1) Locate the "Preferences" row near the bottom of the device information, click the "edit" link in parenthesis
    5.2) Enter your cost of a kWh from your electricity bill (e.g. 0.090751 is 9.0751cents per kWh)
    5.3) Click Save
  6) Reset the stats in your SmartThings App
    6.1) On your SmartThings app, click to your list of things and click on the Energy Meter
    6.2) Click the Reset icon to clear existing stats
