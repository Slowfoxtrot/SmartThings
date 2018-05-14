/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
/*	SmartThings Device handler for Leviton ZWave Smart Dimmer D6ZHD
 *  Based in part on the SmartThings Dimmer sample code.
 *	Based in part on code on github posted by "johnconstantelo", 3/27/2017
 *
 *	Copyright 2017 Jay R. Jaeger
 *
 *	NOTE:  on() and off() ignore the fade-on time (but for the operation only - the default stays set)
 *  I would love to "fix" that, but did not find a way to do it.
 *
 */
 
 
//	Define the device to SmartThings

metadata {

	//	Basic definition information, including what "capabilities" the device has, and what manufacturer
    //	devices it supports (in this case, just the Levigon DZ6HD).

	definition (name: "Leviton DZ6HD Dimmer", namespace: "cube1us", author: "Jay Jaeger", ocfDeviceType: "oic.d.light") {
		capability "Switch Level"		// Dimmer level
		capability "Actuator"			// "Tag" capability indicating that this device has commands
		capability "Indicator"			// Status indicator
		capability "Switch"				// On / Off Capability
		capability "Polling"			// Device implements poll()
		capability "Refresh"			// Device implements refresh()
		capability "Sensor"				// "Tag" capability indicating that this device has attributes
		capability "Health Check"		// This is in the SmartThings dimmer example - don't know what it does.
		capability "Light"				// Like "Switch", capability indicating this device can be on() or off()
        capability "Configuration"		// Indicate that this device is configurable.

        fingerprint mfr:"001D", prod:"3201", model:"0001", deviceJoinName: "Leviton DZ6HD Dimmer"
	}
    
    //	Simulator information.  Not a complete simulation of everything the DZ6HD can do.

	simulator {
    
    	//	Simulated status messages which specify actions that might happen from someone physically
        //	actuating a device.  Z-Wave, in this case.
    	
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// Simulated reply messages to some possible messages that might be received from a hub
        
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	//	Identify the various states and configuration options for the Leviton DZ6HD dimmer.

	preferences {
        input "fadeOnTime", "number", title: "Fade On Time", 
        	description: "0=Instant On, 1-127 Seconds, 128-253 => 1-126 Minutes, default 2", required: false,
            range: "0..253", defaulValue: 2
        input "fadeOffTime", "number", title: "Fade Off Time", 
        	description: "0=Instant Off, 1-127 Seconds, 128-253 => 1-126 Minutes, default 2", required: false,
            range: "0..253", defaultValue: 2
        input "minLightLevel", "number", title: "Minimum Light Level", description: "Minimum Light Level, 0-100, default 10",
        	required: false, range: "0..100", defaultValue: 10
        input "maxLightLevel", "number", title: "Maxiumum Light Level", description: "Maximum Light Level, 0-100, default 100",
        	required: false, range: "0..100", defaultValue: 100
        input "presetLightLevel", "number", title: "Preset Light Level", description: "0 = Last Dim State, 1-100",
        	required: false, range: "0..100", defaultValue: 0
        input "dimLevelTimeout", "number", title: "LED Dim Level Indicator Timeout", 
        	description: "0 = Off, 1-254 Seconds, 255 = Always On, default 3", 
            required: false, range: "0..255", defaultValue: 3
         
        /*
        	Removed unless/until I can find a way to sync this when the control in the tile makes a change
            
        input "locatorStatus", "enum", title: "Locator LED Status", 
        	description: "Status (Bottom) LED", required: false, 
            options: ["off": "Always Off", "statusMode": "Status Mode - On when Dimmer On", 
            "locatorMode": "Locator Mode - On when Dimmer Off"], defaultValue: "locatorMode"
        */
        
        input "loadType", "enum", title: "Load Type", description: "Type of Bulb in Use",
        	required: false, options: ["incandescent": "Incandescent", "led": "Dimmable LED", "cfl": "Dimmable Compact Flourescent"],
            defaultValue: incandescent
            
	}

	//	Identify how the screen should be layed out when displaying the dimmer.  Note that not all of the possible
    //	settings are displayed.

	//	Tile for the core function of the dimmer: on and off, and dimmer levels - it handles both on/off and the dimmer.

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		//	Tile for the state of the indicator.  The states come from the Indicator capability indicatorStatus enumeration
        //	and are not arbitrary.  Note that the actions identify the method to take the indicator to the NEXT possible state, 
        //	not the current state.

		standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}

		//	Tile where the users can request that the device status be refreshed.
		
		standardTile("refresh", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
        //	Identify which tiles are on the main and detailed displays (which are different from the settings)

		main(["switch"])

		details(["switch", "indicator", "refresh"])

	}
}

//	Methods to returun my Name and version for logging (as SmartThings use of
//	Groovy does not support static constants.)

def getMyName() {
	return "Leviton D6ZHD Dimmer"
}

def getVersion() {
	return "V0.94"
}


def installed() {


	log.debug "${getMyName()} Version ${getVersion()} installed"

	// Set the internval to ping if no device events received for 32min (checkInterval)
        
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, 
    	displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
}

def updated() {

    log.debug "${getMyName()} version ${getVersion()} updated()"

	// Set the internval to ping if no device events received for 32min (checkInterval)
    
	sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, 
    	displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    
    //	Configure the device with updated settings.
    
    response(configure())
   
}

def getCommandClassVersions() {
	[
		0x20: 1,  // Basic
		0x26: 1,  // SwitchMultilevel
		/* 0x56: 1,  // Crc16Encap */
		0x70: 1,  // Configuration
	]
}

//	The parse() method handles messages passed *from* this device.  It translates
//	those messages into a commands/events that SmartThings can execute/respond to, 
//	and returns the results.

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "${getMyName()} parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, commandClassVersions)
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "${getMyName()} was hailed: requesting state update"
	} else {
		log.debug "${getMyName()} parse returned ${result?.descriptionText}"
	}
	return result
}

//	The set of event methods that we respond to.  These are invoked from
//	parse(), above.  Each is selected at runtime based on its method
//	signature ("Groovy").  They all actually dovetail into the same
//	method, and exist only to discriminate between events we do handle,
//	and events we do not handle/expect (the catch-all zwaveEvent method
//	at the end.

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

//	Method that actually handles the zwave events for the above overloaded methods.  
//	It turns on or off the switch based on the value, and then, if approriate,
//	send a command to set the level.  This is the original SmartThings code.
//	It might actually be better to handle the BasicSet and SwitchMultilevelSet
//	differently.  Oh well.

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
    	// log.debug "${getMyName()} Sending event level with value ${cmd.value}%"
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}


def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "${getMyName()} configurationReport $cmd"
	def value = "when off"
	if (cmd.configurationValue[0] == getLocatorStatusValue("when on")) {value = "when on"}
	if (cmd.configurationValue[0] == getLocatorStatusValue("never")) {value = "never"}
	createEvent([name: "indicatorStatus", value: value])
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	log.debug "${getMyName()} Hail event"
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "${getMyName} Version ${getVersion()} Manufacturer Specific Report:"
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	updateDataValue("manufacturer", cmd.manufacturerName)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

/*

	Not present on the Leviton Dimmer??
    
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def versions = commandClassVersions
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		zwaveEvent(encapsulatedCommand)
	}
}

*/

//	The catch-all method that "handles" zwave events we don't care about...

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

//	Methods for Switch/Light capability

def on() {

	//	Note:  Changed from "basicSet" in the original SmartThings template dimmer
    //	to "MultiLevelSet" to preserve the last dimming level.
    
	delayBetween([
            zwave.switchMultilevelV1.switchMultilevelSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}


//	Methods for Switch Level (Dimmer) capability

def setLevel(value) {
	log.debug "${getMyName()} setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
    
    //	Set the level.
    
	delayBetween (
    	[zwave.basicV1.basicSet(value: level).format(), 
        zwave.switchMultilevelV1.switchMultilevelGet().format()], 
        5000)
}

//	The Version 2 setLevel includes a duration, apparently in seconds.
//	This is currently never actually called from the device handler itself.
   
def setLevel(value, duration) {
	log.debug "${getMyName()} setLevel V2 >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
    
    //	For the DZ6HD, Duration:  0 = instant, 0-127 is in seconds, 128-253 is 128 + minutes
    //	Incoming duration is, apparently, in seconds.
    
	def dimmingDuration = duration < 128 ? duration : 127 + Math.round(duration/60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
    
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], 
                   getStatusDelay)
}

//	Methods for the indicator state, so that this can be changed from the Tile, rather
//	than from configure().

void indicatorWhenOn() {
	sendEvent(name: "indicatorStatus", value: "when on", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
    	configurationValue: [getLocatorStatusValue("when on")], parameterNumber: 7, size: 1).format()))
}

void indicatorWhenOff() {
	sendEvent(name: "indicatorStatus", value: "when off", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
    	configurationValue: [getLocatorStatusValue("when off")], parameterNumber: 7, size: 1).format()))
}

void indicatorNever() {
	sendEvent(name: "indicatorStatus", value: "never", displayed: false)
	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
    	configurationValue: [getLocatorStatusValue("never")], parameterNumber: 7, size: 1).format()))
}

//	Method for Polling capability

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
 
def ping() {
	refresh()
}

//	Method for Refresh capability

def refresh() {
	log.debug("${getMyName()} refresh()")
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	delayBetween(commands,100)
}

//	Helper method to translate the string values for loadType 
//	into the numerical value expected by the hardware.

def getLoadTypeValue() {
        switch(loadType) {
            case "led":
            	return(1)
                break
			case "cfl":             
            	return(2)
                break
            default:
            	return(0)
                break
        }
}

//	Helper method to translate the string values for locatorStatus 
//	into the numerical value expected by the hardware.

def getLocatorStatusValue(String value) {
	switch(value) {
    	case "never":
        	return(0)
            break
        case "when on":
        	return(254)
            break
        default:  // "when off" is the default
        	return(255)
        	break
    }
}

//	Method to handle device settings / configuration:  Configure capability.
//	Returns a list of commands to be sent to configure the device.

def configure() {
	log.debug "${getMyName()} ${getVersion()} configuration change"
       
    // Send commands to configure the dimmer via the Hub (when I tried just using configurationSet()
    // the commands had no effect.)
        
    //	Now update the switch with the new parameters.  (Note:  The locator LED mode is set
    //	using the tile interface instead of via configure()
    
    delayBetween([
    	sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        	configurationValue: [fadeOnTime ?: 2], parameterNumber: 1, size: 1).format())),
        sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        	configurationValue: [fadeOffTime ?: 2], parameterNumber: 2, size: 1).format())),
        sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        	configurationValue: [minLightLevel ?: 10], parameterNumber: 3, size: 1).format())),
        sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        	configurationValue: [maxLightLevel ?: 100], parameterNumber: 4, size: 1).format())),
        sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        	configurationValue: [presetLightLevel ?: 0], parameterNumber: 5, size: 1).format())),
        sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        	configurationValue: [dimLevelTimeout ?: 3], parameterNumber: 6, size: 1).format())),
        // sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        //	configurationValue: [getLocatorStatusValue()], parameterNumber: 7, size: 1).format())),
        sendHubCommand(new physicalgraph.device.HubAction(zwave.configurationV1.configurationSet(
        	configurationValue: [getLoadTypeValue()], parameterNumber: 8, size: 1).format())),
	], 500 )
}