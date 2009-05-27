
var orbs = []; //array
var orbTimer = {}; //object
var ORB_INTERVAL = 1000;

var CURRENT_VOLUME = '';
var CURRENT_COLOR = '';
var LAST_VOLUME = '';
var LAST_COLOR = '';

$(document).ready(function() {
	swarmInit();
});



function swarmInit() {
	updateOrientation();
	trace('init');
	
	$('#tabs').tabs();
	
	$('#orbs a').click(function(event){
		event.preventDefault();
		
		var thisOrb = parseInt($(this).attr('id').substr(4));
		
		if ($(this).hasClass('on')) {
			$(this).removeClass('on');
			orbs.removeItems(thisOrb);

		} else {
			$(this).addClass('on');
			orbs.push(thisOrb);
		}
		
		trace(orbs)
		
	});
	
	$('#orb_all a').click(function(event){
		event.preventDefault();
	
		if ($(this).hasClass('on')) {
			$(this).removeClass('on');
			
			$('#orbs a').each(function(){
				$(this).removeClass('on');
			});
			orbs = [];
		} else {
			$(this).addClass('on');
			$('#orbs a').each(function(){
				$(this).addClass('on');
			});
			orbs = [0, 1, 2, 3, 4, 5];
		}
		trace(orbs);
		
	});
	
    $('#colorpicker').farbtastic(function(value){ 
		CURRENT_COLOR = value;
	});

	$("#volume").slider({ animate: true, orientation: 'horizontal', step: 10, option: 'value',
		change: function(event, ui) {
			
			var thisVol = toHex(Math.round((100 - ($('#volume').slider('option', 'value'))) * 2.54));
			CURRENT_VOLUME = thisVol;
			
			//sendVolume(thisVol);
			
		}
	 });

	$('#color').change(function(event) {
		trace('here');
		trace($(this).val());
	});

	$('#soundbank').change(function(event) {
		sendSoundbank($(this).val());
	});

	orbTimer = $.timer(ORB_INTERVAL, function (timer) {
	
		if (CURRENT_COLOR != LAST_COLOR) {
			sendColor();
		}
	
		if (CURRENT_VOLUME != LAST_VOLUME) {
			sendVolume();
		}
		
	});
	

}

function sendSoundbank(soundbank) {

	trace(soundbank);
	
	var outString = '';
	//var colorObj = {};
	
	if (orbs.length > 0) {
		for (var i=0; i < orbs.length; i++) {
			 // do everything twice for fault tolerance
      		        outString += '{6'+orbs[i]+' <M VST>}';
      		        outString += '{6'+orbs[i]+' <M VST>}';
			outString += '{6'+orbs[i]+' <M CD ..>}';
			outString += '{6'+orbs[i]+' <M CD ..>}';
			outString += '{6'+orbs[i]+' <M CD '+soundbank+'>}';
			outString += '{6'+orbs[i]+' <M CD '+soundbank+'>}';
		}
	}

	trace(outString);
	swarmWS.serviceCall("swarm", outString, soundSuccessCB, soundFailureCB);
}

function sendColor() {
	
	var value = CURRENT_COLOR;
	
	$('#color').val(value);
	$('#color').css('backgroundColor', value);
	
	var red = parseInt(value.substr(1, 2), 16);
	var green = parseInt(value.substr(3, 2), 16);
	var blue = parseInt(value.substr(5, 2), 16);	

	var outString = '';
	//var colorObj = {};
	
	if (orbs.length > 0) {
		for (var i=0; i < orbs.length; i++) {
			
			outString += '{6'+orbs[i]+' <LR'+red+'>}{6'+orbs[i]+' <LG'+green+'>}{6'+orbs[i]+' <LB'+blue+'>}{6'+orbs[i]+' <LF>}';
			
		}
	}
		
	trace(outString);
	//colorObj.data = outString;
	
	if (outString != '') {
		swarmWS.serviceCall("swarm", outString, colorSuccessCB, colorFailureCB);
	}
	
	LAST_COLOR = CURRENT_COLOR;
	
}

function soundSuccessCB(data) {
	trace('sound success');
}
function soundFailureCB(data) {
	trace('sound failure');	
}


function colorSuccessCB(data) {
	trace('color success');
}
function colorFailureCB(data) {
	trace('color failure');	
}

function sendVolume() {
	
	var volume = CURRENT_VOLUME;
	
	var outString = '';
	
	//var volObj = {};

	if (orbs.length > 0) {
		for (var i=0; i < orbs.length; i++) {
			
			outString += '{6'+orbs[i]+' <M VSV 0x'+volume+'>}';
			
		}
	}
	
	//volObj.data = outString;
	
	trace(outString);
	if (outString != '') {
		swarmWS.serviceCall("swarm", outString, volumeSuccessCB, volumeFailureCB);
	}
	
	LAST_VOLUME = CURRENT_VOLUME;
}
	
function volumeSuccessCB(data) {
	trace('volume success');
}
function volumeFailureCB(data) {
	trace('volume failure');	
}	

function updateOrientation(){
	var contentType = "show_";
	switch(window.orientation){
		
		
		case -90:
		contentType += "right";
		break;
		
		case 90:
		contentType += "left";
		break;
		
		case 180:
		contentType += "flipped";
		break;
		
		case 0:
		default:
		contentType += "normal";
		break;
	}
//document.getElementById("page_wrapper").setAttribute("class", contentType);
	$('#page_wrapper').addClass(contentType);
	trace('here '+contentType);
}